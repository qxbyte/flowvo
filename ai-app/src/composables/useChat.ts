import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

export function useChat() {
  const router = useRouter()
  const messages = ref<Array<{role: string, content: string}>>([])
  const isLoading = ref(false)
  const chatRecords = ref<Array<{id: string, title: string}>>([])
  const currentChatId = ref('')

  // 发送消息
  const sendMessage = async (message: string) => {
    if (!message.trim() || isLoading.value) return
    isLoading.value = true
    let fullAiResponse = ''

    try {
      // 创建新对话，但不保存到数据库
      if (!currentChatId.value) {
        const response = await axios.post('/api/chat/new')
        const data = response.data
        currentChatId.value = data.id
      }

      // 获取AI回复，同时保存消息
      const formData = new FormData()
      formData.append('message', message)
      formData.append('chatId', currentChatId.value)
      const streamResponse = await axios.post('/api/chat/sendStream', formData, {
        responseType: 'stream'
      })

      if (streamResponse.status === 401) {
        router.push('/login')
        return
      }

      if (streamResponse.status !== 200) {
        throw new Error('Network response was not ok')
      }

      // 添加用户消息到界面
      messages.value.push({
        role: 'user',
        content: message
      })

      // 添加空的AI回复消息
      messages.value.push({
        role: 'assistant',
        content: ''
      })

      const reader = streamResponse.data?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      const lastMessage = messages.value[messages.value.length - 1]

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const text = new TextDecoder().decode(value)
        lastMessage.content += text
        fullAiResponse += text
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
    }
  }

  // 加载所有对话记录
  const loadChatRecords = async () => {
    try {
      const response = await axios.get('/api/chat/records')
      if (response.status === 401) {
        router.push('/login')
        return
      }
      chatRecords.value = response.data
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
      const response = await axios.get(`/api/chat/${chatId}`)
      if (response.status === 401) {
        router.push('/login')
        return
      }

      const data = response.data
      console.log('历史消息数据:', data)

      // 直接使用返回的数组数据
      messages.value = data.map(msg => ({
        role: msg.role,
        content: msg.content
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
    loadChatRecords,
    loadChat
  }
}
