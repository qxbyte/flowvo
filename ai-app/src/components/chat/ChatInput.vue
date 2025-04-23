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
      <button class="send-button" @click="handleSend" :disabled="isLoading">
        <PaperAirplaneIcon v-if="!isLoading" class="h-5 w-5" />
        <div v-else class="loading-spinner"></div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue'
import { PaperAirplaneIcon } from '@heroicons/vue/24/outline'

const emit = defineEmits(['send'])

// Props
defineProps<{
  isLoading: boolean
}>()

// 输入框状态
const inputValue = ref('')
const inputElement = ref<HTMLTextAreaElement | null>(null)

// 自动调整文本框高度
const autoResize = () => {
  if (inputElement.value) {
    inputElement.value.style.height = 'auto'
    inputElement.value.style.height = inputElement.value.scrollHeight + 'px'
  }
}

// 处理发送消息
const handleSend = () => {
  if (!inputValue.value.trim()) return
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
