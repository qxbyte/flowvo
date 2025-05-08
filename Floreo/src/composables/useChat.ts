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
        await currentReader.cancel('用户终止了响应')
        currentReader = null
        isLoading.value = false
      } catch (error) {
        console.error('终止响应时出错:', error)
      }
    }
  }

  // 发送消息
  const sendMessage = async (message: string) => {
    // 如果正在加载中，则终止当前响应
    if (isLoading.value) {
      await stopResponse()
      return
    }
    
    if (!message.trim()) return
    isLoading.value = true
    let fullAiResponse = ''

    try {
      // 创建新对话，但不保存到数据库
      if (!currentChatId.value) {
        const response = await fetch('/api/chat/new', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        const data = await response.json()
        currentChatId.value = data.id
      }

      // 获取AI回复，同时保存消息
      const formData = new FormData()
      formData.append('message', message)
      formData.append('chatId', currentChatId.value)
      const streamResponse = await fetch('/api/chat/sendStream', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      })

      if (!streamResponse.ok) {
        throw new Error('Network response was not ok')
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

      try {
        while (true) {
          const { done, value } = await currentReader.read()
          if (done) break

          const text = new TextDecoder().decode(value)
          lastMessage.content += text
          fullAiResponse += text
        }
      } catch (error: any) {
        if (error.name !== 'AbortError') {
          console.error('读取流时出错:', error)
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
      isLoading.value = false
      currentReader = null
    }
  }

  // 加载所有对话记录
  const loadChatRecords = async () => {
    try {
      const response = await fetch('/api/chat/records', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })
      if (!response.ok) {
        throw new Error('Network response was not ok')
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
      const response = await fetch(`/api/chat/${chatId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })
      
      if (!response.ok) {
        throw new Error('Network response was not ok')
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