<template>
  <div class="chat-layout">
    <div class="sidebar" :class="{ 'collapsed': isSidebarCollapsed }">
      <div class="sidebar-header">
        <div class="header-buttons">
          <button class="icon-button" @click="toggleSidebar" :title="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd" />
            </svg>
          </button>
          <button class="new-chat" @click="createNewChat">
            <span>+</span>
            <span class="button-text">新对话</span>
          </button>
        </div>
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
              <div v-if="message.role === 'assistant'" class="message-actions">
                <button class="action-btn" title="复制">
                  <DocumentDuplicateIcon class="h-4 w-4" />
                </button>
                <button class="action-btn" title="点赞">
                  <HandThumbUpIcon class="h-4 w-4" />
                </button>
                <button class="action-btn" title="点踩">
                  <HandThumbDownIcon class="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-container">
          <textarea
            v-model="userInput"
            @keydown.enter.prevent="sendMessage"
            @keydown.enter.ctrl.prevent="() => userInput += '\n'"
            placeholder="询问任何问题... (Enter 发送, Ctrl + Enter 换行)"
            rows="1"
            class="message-input"
            @input="autoResize"
            ref="inputElement"
          ></textarea>
          <button class="send-button" @click="sendMessage" :disabled="isLoading">
            <PaperAirplaneIcon v-if="!isLoading" class="h-5 w-5" />
            <div v-else class="loading-spinner"></div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'
import {
  DocumentDuplicateIcon,
  HandThumbUpIcon,
  HandThumbDownIcon,
  PaperAirplaneIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
// 状态变量声明
const messages = ref<Array<{role: string, content: string}>>([])
const isSidebarCollapsed = ref(false)

// 切换侧边栏状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
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

// 添加自动调整文本框高度的方法
const inputElement = ref<HTMLTextAreaElement | null>(null)
const autoResize = () => {
  if (inputElement.value) {
    inputElement.value.style.height = 'auto'
    inputElement.value.style.height = inputElement.value.scrollHeight + 'px'
  }
}
</script>

<style scoped>
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
  background-color: #f0f0f0;
  display: flex;
  flex-direction: column;
  padding: 8px;
  transition: all 0.3s ease;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar.collapsed .button-text,
.sidebar.collapsed .chat-list {
  display: none;
}

.header-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.icon-button {
  padding: 8px;
  background: transparent;
  border: 1px solid rgba(0,0,0,.1);
  border-radius: 4px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-button:hover {
  background-color: rgba(0,0,0,0.05);
}

.chat-list {
  margin-top: 8px;
  overflow-y: auto;
  flex: 1;
}

.chat-item {
  padding: 12px;
  color: #333;
  cursor: pointer;
  border-radius: 4px;
  margin: 2px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item:hover {
  background-color: rgba(0,0,0,0.05);
}

.chat-item.active {
  background-color: rgba(0,0,0,0.1);
}

.new-chat {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(0,0,0,.1);
  border-radius: 4px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  overflow: hidden;
   max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.message-wrapper {
  padding: 4px 0;
}

.message {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: flex-start;
  width: 100%;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  padding: 8px 12px;
  border-radius: 12px;
  line-height: 1.5;
  font-size: 15px;
  max-width: 80%;
  width: fit-content;
}

.user .message-content {
  background: #f0f0f0;
  color: #333;
  border-radius: 18px;
}

.assistant .message-content {
  background: transparent;
  box-shadow: none;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message:hover .message-actions {
  opacity: 1;
}

.action-btn {
  padding: 4px;
  border-radius: 4px;
  color: #666;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f0f0f0;
  color: #2196f3;
}

.input-area {
  padding: 24px;
  background: white;
  border-top: 1px solid #e5e5e5;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.message-input {
  width: 100%;
  min-height: 44px;
  max-height: 200px;
  padding: 12px 44px 12px 16px;
  font-size: 16px;
  line-height: 1.5;
  border: 1px solid #e5e5e5;
  border-radius: 24px;
  resize: none;
  outline: none;
  box-shadow: 0 0 10px rgba(0,0,0,0.05);
  transition: border-color 0.3s, box-shadow 0.3s;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  background-color: #ffffff;
}

.message-input::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

.message-input:focus {
  border-color: #2196f3;
  box-shadow: 0 0 2px rgba(33,150,243,0.3);
}

.send-button {
  position: absolute;
  right: 8px;
  bottom: 6px;
  width: 32px;
  height: 32px;
  padding: 6px;
  background: #000000;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.send-button:hover {
  background: #000000;
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

.system {
  text-align: center;
  color: #ff5252;
  padding: 12px;
}
</style>

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
  background: #000000;
}

.send-button svg {
  width: 16px;
  height: 16px;
}
</style>

<style scoped>
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  margin-bottom: 20px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.chat-messages::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
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
</style>
