import NavBar from '@/components/NavBar.vue'

<template>
  <NavBar />
  <div class="chat-layout">
    <ChatSidebar
      v-model:currentChatId="currentChatId"
      :chatRecords="chatRecords"
      @loadChat="loadChat"
    />
    <div class="main-content">
      <ChatMessages :messages="messages" />
      <ChatInput
        :isLoading="isLoading"
        @send="sendMessage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatMessages from '@/components/chat/ChatMessages.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import { useChat } from '@/composables/useChat'

// 使用组合式函数
const {
  messages,
  isLoading,
  chatRecords,
  currentChatId,
  sendMessage,
  loadChatRecords,
  loadChat
} = useChat()

// 页面加载时获取对话记录列表
onMounted(async () => {
  await loadChatRecords()
})
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  background-color: #ffffff;
  padding-top: 5vh; /* 为固定导航栏留出空间 */
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
</style>
