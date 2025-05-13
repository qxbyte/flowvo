<template>
  <div class="chat-messages" ref="messageContainer">
    <div v-for="(message, index) in messages" :key="index" class="message-wrapper">
      <div class="message" :class="message.role">
        <div class="message-timestamp" v-if="message.createTime">
          {{ formatTime(message.createTime) }}
        </div>
        <div class="message-content" :class="{ 'loading': message.loading }">
          <div v-html="formatMessage(message.content)"></div>
          <div v-if="message.role === 'assistant'" class="message-actions">
            <button class="action-btn" title="复制" @click="copyMessage(message.content)">
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
import { ref, onMounted, nextTick, defineProps, watch } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js'
import {
  DocumentDuplicateIcon,
  HandThumbUpIcon,
  HandThumbDownIcon,
} from '@heroicons/vue/24/outline'

// Props
const props = defineProps<{
  messages: Array<{role: string, content: string, createTime?: string, loading?: boolean}>
}>()

// 消息容器引用
const messageContainer = ref<HTMLElement | null>(null)

// 格式化消息内容，简化marked调用，避免TypeScript错误
const formatMessage = (content: string): string => {
  if (!content) return '';
  try {
    // 处理可能的data:前缀
    let processedContent = content;
    
    // 检查内容是否包含多个"data:"前缀的行
    if (processedContent.includes('data:')) {
      // 按行分割
      const lines = processedContent.split('\n');
      const processedLines = lines.map(line => {
        if (line.startsWith('data:')) {
          return line.substring(5).trim();
        }
        return line;
      });
      processedContent = processedLines.join('\n');
    }
    
    // 简单处理markdown
    // 处理代码块
    let formatted = processedContent
      .replace(/```(\w*)([\s\S]*?)```/g, '<pre><code class="$1">$2</code></pre>')
      // 处理链接
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
      // 处理粗体
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      // 处理斜体
      .replace(/\*([^*]+)\*/g, '<em>$1</em>')
      // 处理单行代码
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // 处理换行
      .replace(/\n/g, '<br>');
    
    return formatted;
  } catch (error) {
    console.error('格式化消息失败:', error);
    return content;
  }
}

// 复制消息内容
const copyMessage = (content: string) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success({
      message: '已复制到剪贴板',
      plain: true
    })
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error({
      message: '复制失败',
      plain: true
    })
  })
}

// 格式化时间戳
const formatTime = (timeStr: string): string => {
  if (!timeStr) return '';
  try {
    const date = new Date(timeStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } catch (error) {
    console.error('格式化时间失败:', error);
    return '';
  }
}

// 监听消息变化，滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
}

// 组件挂载时滚动到底部
onMounted(() => {
  scrollToBottom();
})

// 监听消息列表变化
watch(() => props.messages, () => {
  scrollToBottom();
}, { deep: true })
</script>

<style scoped>
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 15%;
  background: #ffffff;
  border-radius: 8px;
  margin-bottom: 20px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.chat-messages::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

.message-wrapper {
  margin-bottom: 20px;
}

.message {
  display: flex;
  flex-direction: column;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.message.system {
  align-items: center;
}

.message-content {
  padding: 12px 16px;
  max-width: 90%;
  display: inline-block;
  margin-top: 4px;
  position: relative;
}

/* 保留用户消息气泡效果 */
.user .message-content {
  background: #007AFF;
  color: white;
  border-radius: 16px;
}

/* 移除助手回复的气泡效果 */
.assistant .message-content {
  color: #333;
  background: transparent;
  border-radius: 0;
  padding-left: 0;
}

.system .message-content {
  color: #d32f2f;
  font-style: italic;
  background: transparent;
  text-align: center;
}

.message-content.loading::after {
  content: "";
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-left: 8px;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-top-color: #000;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  justify-content: flex-start;
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

/* 时间戳样式 */
.message-timestamp {
  font-size: 12px;
  color: #9e9e9e;
  opacity: 0.5;
  margin-bottom: 4px;
  text-align: center;
}

.message.user .message-timestamp {
  margin-right: 8px;
}

.message.assistant .message-timestamp {
  margin-left: 8px;
}
</style>
