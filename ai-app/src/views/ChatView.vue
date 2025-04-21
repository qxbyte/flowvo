<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messageContainer">
      <div v-for="(message, index) in messages" :key="index" class="message" :class="message.role">
        <div class="message-content">
          <div v-html="formatMessage(message.content)"></div>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <textarea
        v-model="userInput"
        @keyup.enter.ctrl="sendMessage"
        placeholder="输入消息，Ctrl + Enter 发送"
      ></textarea>
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

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

const sendMessage = async () => {
  if (!userInput.value.trim()) return

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
    const response = await fetch('/api/chat/send-1', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      },
      credentials: 'include',
      body: JSON.stringify({
        prompt: userMessage
      })
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
