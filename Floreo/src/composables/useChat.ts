import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

export function useChat() {
  const router = useRouter()
  const messages = ref<Array<{role: string, content: string, createTime?: string}>>([])
  const isLoading = ref(false)
  const chatRecords = ref<Array<{id: string, title: string}>>([])
  const currentChatId = ref('')
  // 用于存储当前的reader，以便可以中止流式响应
  let currentReader: ReadableStreamDefaultReader<Uint8Array> | null = null

  // 终止当前的流式响应
  const stopResponse = async () => {
    if (currentReader) {
      try {
        console.log('用户主动终止了响应')
        await currentReader.cancel('用户终止了响应')
        currentReader = null
        isLoading.value = false
        
        // 添加提示消息，表明响应被用户中断
        if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'assistant') {
          // 如果最后一条消息内容为空，添加提示
          const lastMsg = messages.value[messages.value.length - 1]
          if (!lastMsg.content || lastMsg.content.trim() === '') {
            lastMsg.content = '(响应已被用户中断)'
          } else {
            // 在现有内容后添加中断提示
            lastMsg.content += ' (响应已被用户中断)'
          }
        }
      } catch (error) {
        console.error('终止响应时出错:', error)
        isLoading.value = false // 确保状态被重置
      }
    } else {
      isLoading.value = false // 即使没有活跃的reader，也要确保加载状态被重置
    }
  }

  // 发送消息
  const sendMessage = async (message: string) => {
    // 如果正在加载中，则终止当前响应
    if (isLoading.value) {
      await stopResponse()
      // 允许UI更新后再继续
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    // 强制安全检查，确保之前的请求已完全结束
    if (isLoading.value) {
      console.error('上一个请求未正确结束，强制重置状态')
      isLoading.value = false
      currentReader = null
    }
    
    if (!message.trim()) return
    isLoading.value = true
    let fullAiResponse = ''

    try {
      // 创建新对话，但不保存到数据库
      if (!currentChatId.value) {
        // 获取授权token
        const token = localStorage.getItem('token')
        if (!token) {
          messages.value.push({
            role: 'system',
            content: '您需要登录才能使用聊天功能'
          })
          isLoading.value = false
          return
        }
        
        try {
          const response = await fetch('/api/chat/new', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          if (!response.ok) {
            if (response.status === 401) {
              messages.value.push({
                role: 'system',
                content: '登录已过期，请重新登录'
              })
              isLoading.value = false
              return
            }
            throw new Error(`创建对话失败，状态码: ${response.status}`)
          }
          const data = await response.json()
          currentChatId.value = data.id
        } catch (error) {
          console.error('创建新对话失败:', error)
          messages.value.push({
            role: 'system',
            content: '创建新对话失败，请重试'
          })
          isLoading.value = false
          return
        }
      }

      // 获取AI回复，同时保存消息
      const formData = new FormData()
      formData.append('message', message)
      formData.append('chatId', currentChatId.value)
      
      // 获取授权token
      const token = localStorage.getItem('token')
      if (!token) {
        messages.value.push({
          role: 'system',
          content: '您需要登录才能使用聊天功能'
        })
        isLoading.value = false
        return
      }
      
      const streamResponse = await fetch('/api/chat/sendStream', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      })

      if (!streamResponse.ok) {
        if (streamResponse.status === 401) {
          messages.value.push({
            role: 'system',
            content: '登录已过期，请重新登录'
          })
          // 可选：重定向到登录页
          // router.push('/login')
          isLoading.value = false
          return
        }
        throw new Error(`网络请求失败，状态码: ${streamResponse.status}`)
      }

      // 添加用户消息到界面
      messages.value.push({
        role: 'user',
        content: message,
        createTime: new Date().toISOString()
      })

      // 添加空的AI回复消息
      messages.value.push({
        role: 'assistant',
        content: '',
        createTime: new Date().toISOString()
      })

      currentReader = streamResponse.body?.getReader() || null
      if (!currentReader) {
        throw new Error('No reader available')
      }

      const lastMessage = messages.value[messages.value.length - 1]
      
      // 设置超时处理，防止流一直不结束
      let streamTimeout: number | null = setTimeout(() => {
        console.log('流响应超时，强制结束')
        isLoading.value = false
        if (currentReader) {
          currentReader.cancel('响应超时').catch(err => {
            console.error('取消流读取时出错:', err)
          })
          currentReader = null
        }
      }, 30000) // 30秒超时
      
      try {
        while (true) {
          const { done, value } = await currentReader.read()
          if (done) {
            console.log('流响应接收完成')
            // 清除超时定时器
            if (streamTimeout) {
              clearTimeout(streamTimeout)
              streamTimeout = null
            }
            break
          }

          const text = new TextDecoder().decode(value)
          lastMessage.content += text
          fullAiResponse += text
        }
      } catch (error: any) {
        if (error.name !== 'AbortError') {
          console.error('读取流时出错:', error)
        }
      } finally {
        // 确保无论如何都会重置加载状态和reader
        isLoading.value = false
        currentReader = null
        // 清除可能存在的超时定时器
        if (streamTimeout) {
          clearTimeout(streamTimeout)
          streamTimeout = null
        }
      }

      // 更新对话列表
      await loadChatRecords()

    } catch (error) {
      console.error('Error:', error)
      messages.value.push({
        role: 'system',
        content: '发生错误，请稍后重试'
      })
    } finally {
      // 双重保险：确保状态一定会被重置
      isLoading.value = false
      currentReader = null
    }
  }

  // 加载所有对话记录
  const loadChatRecords = async () => {
    try {
      // 获取授权token
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('未登录，无法加载对话记录')
        chatRecords.value = []
        return
      }
      
      const response = await fetch('/api/chat/records', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (!response.ok) {
        if (response.status === 401) {
          console.error('登录已过期，请重新登录')
          chatRecords.value = []
          return
        }
        throw new Error(`加载对话记录失败，状态码: ${response.status}`)
      }
      
      chatRecords.value = await response.json()
    } catch (error) {
      console.error('加载对话记录失败:', error)
      chatRecords.value = []
    }
  }

  // 加载特定对话的消息历史
  const loadChat = async (chatId: string) => {
    try {
      console.log('加载对话:', chatId)
      currentChatId.value = chatId
      
      // 获取授权token
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('未登录，无法加载对话')
        messages.value = [{
          role: 'system',
          content: '您需要登录才能查看对话记录'
        }]
        return
      }
      
      const response = await fetch(`/api/chat/${chatId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (!response.ok) {
        if (response.status === 401) {
          messages.value = [{
            role: 'system',
            content: '登录已过期，请重新登录'
          }]
          return
        }
        throw new Error(`加载对话失败，状态码: ${response.status}`)
      }

      const data = await response.json()
      console.log('历史消息数据:', data)

      // 直接使用返回的数组数据
      messages.value = data.map((msg: any) => ({
        role: msg.role,
        content: msg.content,
        createTime: msg.createTime
      }))

    } catch (error) {
      console.error('加载历史消息失败:', error)
      messages.value = [{
        role: 'system',
        content: '加载历史消息失败，请稍后重试'
      }]
    }
  }

  return {
    messages,
    isLoading,
    chatRecords,
    currentChatId,
    sendMessage,
    stopResponse,
    loadChatRecords,
    loadChat
  }
}