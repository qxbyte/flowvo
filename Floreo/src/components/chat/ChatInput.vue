<template>
  <div class="input-area">
    <div class="input-container">
      <textarea
        v-model="inputValue"
        @keydown.enter.prevent="handleSend"
        @keydown.enter.ctrl.prevent="() => inputValue += '\n'"
        placeholder="询问任何问题..."
        rows="1"
        class="message-input"
        @input="autoResize"
        ref="inputElement"
      ></textarea>
      <button 
        class="send-button" 
        @click="handleSend"
        :class="{ 'loading': props.isLoading }"
        :title="props.isLoading ? '点击停止生成' : '发送'"
      >
        <PaperAirplaneIcon v-if="!props.isLoading" class="h-5 w-5" />
        <div v-else class="loading-spinner" @click.stop="handleStop"></div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits, watch } from 'vue'
import { PaperAirplaneIcon } from '@heroicons/vue/24/outline'

const emit = defineEmits(['send', 'stop'])

// Props
const props = defineProps<{
  isLoading: boolean
}>()

// 输入框状态
const inputValue = ref('')
const inputElement = ref<HTMLTextAreaElement | null>(null)

// 监听isLoading状态变化
watch(() => props.isLoading, (newValue, oldValue) => {
  console.log('ChatInput组件检测到isLoading状态变化:', oldValue, '->', newValue)
  
  // 如果状态从loading变为非loading，确保UI更新
  if (oldValue && !newValue) {
    console.log('按钮状态已从加载中恢复为正常')
  }
  
  // 移除所有自动停止的定时逻辑，让响应流自然结束
})

// 自动调整文本框高度
const autoResize = () => {
  if (inputElement.value) {
    inputElement.value.style.height = 'auto'
    inputElement.value.style.height = inputElement.value.scrollHeight + 'px'
  }
}

// 处理停止响应
const handleStop = () => {
  console.log('用户点击停止生成按钮')
  emit('stop')
}

// 处理发送消息或终止响应
const handleSend = () => {
  // 如果正在加载中，则触发终止操作
  if (props.isLoading) {
    console.log('正在生成中，用户点击按钮终止操作')
    emit('stop')
    return
  }
  
  // 否则发送新消息
  if (!inputValue.value.trim()) return
  console.log('发送新消息:', inputValue.value)
  emit('send', inputValue.value)
  inputValue.value = ''
  if (inputElement.value) {
    inputElement.value.style.height = 'auto'
  }
}
</script>

<style scoped>
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
  cursor: pointer;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.send-button.loading {
  background-color: #ff5722;
  cursor: pointer;
}
</style>
