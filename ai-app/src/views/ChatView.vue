<template>
  <div class="chat-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <button class="new-chat" @click="createNewChat">
          <span>+</span>
          新对话
        </button>
      </div>
      <div class="chat-list">
        <div v-for="record in chatRecords"
             :key="record.id"
             class="chat-item"
             :class="{ active: currentChatId === record.id }"
             @click="loadChat(record.id)">
          {{ record.title }}
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="chat-messages" ref="messageContainer">
        <div v-for="(message, index) in messages" :key="index" class="message-wrapper">
          <div class="message" :class="message.role">
            <div class="message-content">
              <div v-html="formatMessage(message.content)"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-container">
          <textarea
            v-model="userInput"
            @keyup.enter.ctrl="sendMessage"
            placeholder="询问任何问题..."
            rows="1"
            class="message-input"
          ></textarea>
          <button class="send-button" @click="sendMessage" :class="{ loading: isLoading }">
            <svg v-if="!isLoading" class="send-icon" viewBox="0 0 24 24" fill="none">
              <path d="M22 2L11 13M22 2L15 22L11 13M11 13L2 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <div v-else class="loading-spinner"></div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.send-button {
  position: absolute;
  right: 32px;
  bottom: 14px;
  width: 32px;
  height: 32px;
  padding: 6px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.send-icon {
  width: 16px;
  height: 16px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.send-button.loading {
  cursor: wait;
  opacity: 0.8;
}
</style>

<style scoped>
/* 移除重复的样式定义，只保留一个 */
.input-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 8px 24px;
  position: relative;
}

.message-input {
  width: 100%;
  min-height: 44px;
  padding: 12px 40px 12px 16px;
  font-size: 16px;
  line-height: 1.5;
  border: 1px solid #e5e5e5;
  border-radius: 24px;
  resize: none;
  outline: none;
  box-shadow: 0 0 10px rgba(0,0,0,0.05);
  transition: border-color 0.3s, box-shadow 0.3s;
}

.message-input:focus {
  border-color: #2196f3;
  box-shadow: 0 0 2px rgba(33,150,243,0.3);
}

.send-button {
  position: absolute;
  right: 32px;
  bottom: 14px;
  width: 32px;
  height: 32px;
  padding: 8px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.send-button:hover {
  background: #1976d2;
}

.send-button svg {
  width: 16px;
  height: 16px;
}
</style>

<style scoped>
/* 保留所有样式定义，但只使用一个 style 标签 */
.chat-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
}

.sidebar {
  width: 260px;
  background-color: #202123;
  display: flex;
  flex-direction: column;
  padding: 8px;
}

.new-chat {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(255,255,255,.2);
  border-radius: 4px;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.new-chat span {
  font-size: 20px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
}

.message-wrapper {
  padding: 20px 0;
  width: 100%;
}

.message {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.message.user {
  display: flex;
  justify-content: flex-end;
}

.message.user .message-content {
  background-color: #2196f3;
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  max-width: fit-content;
}

.message.assistant {
  padding: 12px 0;
}

.message.assistant .message-content {
  padding: 0;
  background: none;
  box-shadow: none;
  color: #000;
}

/* 移除第二个样式块中的重复定义 */
.message-content {
  max-width: 800px;
  width: 100%;
  line-height: 1.6;
}

.input-area {
  border-top: 1px solid rgba(0,0,0,.1);
  padding: 24px 0;
  margin-top: auto;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.message-input {
  width: 100%;
  min-height: 24px;
  padding: 12px;
  font-size: 14px;
  line-height: 1.5;
  border: 1px solid rgba(0,0,0,.1);
  border-radius: 4px;
  resize: none;
  outline: none;
}

.send-button {
  padding: 12px 24px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  white-space: nowrap;
}

.send-button:hover {
  background: #1976d2;
}
</style>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'  // 添加 nextTick
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'

const router = useRouter()
// 状态变量声明
const messages = ref<Array<{role: string, content: string}>>([])
const userInput = ref('')
const messageContainer = ref<HTMLElement | null>(null)
const isLoading = ref(false)
const chatRecords = ref<Array<{id: string, title: string}>>([])
const currentChatId = ref('')  // 添加这行

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return
  isLoading.value = true
  let fullAiResponse = ''

  try {
    const userMessage = userInput.value
    userInput.value = ''

    // 创建新对话，但不保存到数据库
    if (!currentChatId.value) {
      const response = await fetch('/chat/new', {
        method: 'POST'
      })
      const data = await response.json()
      currentChatId.value = data.id
    }

    // 获取AI回复，同时保存消息
    const formData = new FormData()
    formData.append('message', userMessage)
    formData.append('chatId', currentChatId.value)
    const streamResponse = await fetch('/chat/sendStream', {
      method: 'POST',
      body: formData
    })

    if (streamResponse.status === 401) {
      router.push('/login')
      return
    }

    if (!streamResponse.ok) {
      throw new Error('Network response was not ok')
    }

    // 添加用户消息到界面
    messages.value.push({
      role: 'user',
      content: userMessage
    })

    // 添加空的AI回复消息
    messages.value.push({
      role: 'assistant',
      content: ''
    })

    const reader = streamResponse.body?.getReader()
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

      await import('vue').then(vue => vue.nextTick())
      if (messageContainer.value) {
        messageContainer.value.scrollTop = messageContainer.value.scrollHeight
      }
    }

    // 一次性保存用户消息和AI回复
    // await fetch('/chat/saveMessages', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json'
    //   },
    //   body: JSON.stringify({
    //     chatId: currentChatId.value,
    //     messages: [
    //       {
    //         role: 'user',
    //         content: userMessage
    //       },
    //       {
    //         role: 'assistant',
    //         content: fullAiResponse
    //       }
    //     ]
    //   })
    // })

    // 移除重复的保存消息请求
    // await fetch('/chat/saveMessages', {...})

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

// 修改 marked 配置
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(lang, code).value
    }
    return hljs.highlightAuto(code).value
  }
})

// 修改 formatMessage 函数定义
const formatMessage = (content: string): string => {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return content
  }
}

// 删除这里重复声明的变量
// const chatRecords = ref<Array<{id: string, title: string}>>([])
// const currentChatId = ref('')

// 加载所有对话记录
const loadChatRecords = async () => {
  const response = await fetch('/chat/records')
  if (response.ok) {
    chatRecords.value = await response.json()
  }
}

// 加载特定对话的消息历史
const loadChat = async (chatId: string) => {
  try {
    console.log('加载对话:', chatId)
    currentChatId.value = chatId
    const response = await fetch(`/chat/${chatId}`)
    if (!response.ok) {
      throw new Error('加载历史消息失败')
    }

    const data = await response.json()
    console.log('历史消息数据:', data)

    // 直接使用返回的数组数据
    messages.value = data.map(msg => ({
      role: msg.role,
      content: msg.content
    }))

    // 滚动到底部
    await nextTick()
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    messages.value = [{
      role: 'system',
      content: '加载历史消息失败，请稍后重试'
    }]
  }
}

// 创建新对话
const createNewChat = async () => {
  const response = await fetch('/chat/new', {
    method: 'POST'
  })
  if (response.ok) {
    const data = await response.json()
    currentChatId.value = data.id
    messages.value = []
    await loadChatRecords()
  }
}

// 页面加载时获取对话记录列表
onMounted(async () => {
  await loadChatRecords()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.message {
  margin-bottom: 20px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
}

.message.assistant {
  margin-right: auto;
}

.message-content {
  padding: 12px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user .message-content {
  background: #007AFF;
  color: white;
}

.chat-input {
  display: flex;
  gap: 10px;
}

textarea {
  flex: 1;
  height: 80px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
}

button {
  padding: 0 20px;
  background: #007AFF;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

button:hover {
  background: #0056b3;
}

/* 添加系统消息样式 */
.message.system {
  background-color: #ffebee;
  margin: 10px 10%;
  text-align: center;
}

/* 将文件末尾未包含在style标签中的样式移到这里 */
.loading-dots {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #999;
  animation: dot-flashing 1s infinite linear alternate;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dot-flashing {
  0% {
    opacity: 0.2;
  }
  100% {
    opacity: 1;
  }
}

/* 添加聊天记录列表样式 */
.chat-list {
  margin-top: 8px;
  overflow-y: auto;
  flex: 1;
}

.chat-item {
  padding: 12px;
  color: white;
  cursor: pointer;
  border-radius: 4px;
  margin: 2px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item:hover {
  background-color: rgba(255,255,255,0.1);
}

.chat-item.active {
  background-color: rgba(255,255,255,0.2);
  font-weight: bold;
}

.chat-item.active {
  background-color: rgba(255,255,255,0.9);
}
</style>
