<template>
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
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, defineProps } from 'vue'
import { marked } from 'marked'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'
import {
  DocumentDuplicateIcon,
  HandThumbUpIcon,
  HandThumbDownIcon,
} from '@heroicons/vue/24/outline'

// Props
defineProps<{
  messages: Array<{role: string, content: string}>
}>()

// 消息容器引用
const messageContainer = ref<HTMLElement | null>(null)

// 修改 marked 配置
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(lang, code).value
    }
    return hljs.highlightAuto(code).value
  }
})

// 格式化消息内容
const formatMessage = (content: string): string => {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return content
  }
}

// 监听消息变化，滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

// 监听消息列表变化
watch(() => props.messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

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

/* 系统消息样式 */
.message.system {
  background-color: #ffebee;
  margin: 10px 10%;
  text-align: center;
}
</style>
