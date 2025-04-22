<template>
  <div class="chat-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <button class="new-chat">
          <span>+</span>
          新对话
        </button>
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
        <template>
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
      </div>
    </div>
  </div>
</template>

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
import { ref } from 'vue'
import { useRouter } from 'vue-router'  // 添加这行
import { marked } from 'marked'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'

const router = useRouter()  // 添加这行
// 定义消息类型接口，用于类型检查
export interface Message {
  role: 'user' | 'assistant'
  content: string
}

// 删除此处的 messages 声明，因为在下方已经有了完整的声明
// const inputMessage = ref('')  // 删除这行

const messages = ref<Array<{role: string, content: string}>>([])
const userInput = ref('')  // 只保留这个输入变量
const messageContainer = ref<HTMLElement | null>(null)
// 删除未使用的变量

const isLoading = ref(false)

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return
  isLoading.value = true

  try {
    // 先保存用户输入
    const userMessage = userInput.value
    userInput.value = ''

    // 添加用户消息
    messages.value.push({
      role: 'user',
      content: userMessage
    })

    // 添加一个空的 AI 回复消息
    messages.value.push({
      role: 'assistant',
      content: ''
    })

    try {
      const formData = new FormData();
      formData.append('message', userMessage);
      
      const response = await fetch('/chat/sendStream', {
        method: 'POST',
        body: formData
      })

      if (response.status === 401) {
        router.push('/login')
        return
      }

      if (!response.ok) {
        throw new Error('Network response was not ok')
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No reader available')
      }

      // 获取最后一条消息的引用（AI 回复）
      const lastMessage = messages.value[messages.value.length - 1]

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        // 将 Uint8Array 转换为文本
        const text = new TextDecoder().decode(value)
        // 更新最后一条消息的内容
        lastMessage.content += text

        // 滚动到底部
        await import('vue').then(vue => vue.nextTick())
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight
        }
      }
    } catch (error) {
      console.error('Error:', error)
      // 添加错误提示消息
      messages.value.push({
        role: 'system',
        content: '发生错误，请稍后重试'
      })
    }
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
</style>

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
