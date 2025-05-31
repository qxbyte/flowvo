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
        @stop="stopResponse"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatMessages from '@/components/chat/ChatMessages.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import { useChat } from '@/composables/useChat'

const router = useRouter()

// 使用组合式函数
const {
  messages,
  isLoading,
  chatRecords,
  currentChatId,
  sendMessage,
  stopResponse,
  loadChatRecords,
  loadChat: loadChatMessages
} = useChat()

// 定时刷新变量
let refreshTimer: number | null = null

// 页面加载时获取对话记录列表
onMounted(async () => {
  console.log('ChatView 组件已挂载，检查用户认证状态')

  // 检查用户是否已登录
  const token = localStorage.getItem('token')
  const isAuthenticated = token && localStorage.getItem('isAuthenticated') === 'true'

  if (!isAuthenticated) {
    console.log('用户未登录，重定向到登录页面')
    // 清除可能存在的无效认证数据
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')

    // 重定向到登录页
    router.push('/login')
    return // 防止继续执行后续代码
  }

  console.log('用户已登录，准备加载对话记录...')
  console.log('当前登录用户:', localStorage.getItem('username'))

  await loadChatRecords()
  console.log('对话记录加载完成，记录数量:', chatRecords.value.length, '详细数据:', chatRecords.value)

  // 设置定时刷新对话列表（每30秒刷新一次）
  refreshTimer = window.setInterval(async () => {
    console.log('定时刷新对话列表...')
    await loadChatRecords()
  }, 30000)
})

// 组件卸载前清除定时器
onBeforeUnmount(() => {
  if (refreshTimer !== null) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 处理对话加载
const loadChat = async (chatId: string) => {
  console.log('ChatView收到loadChat事件，chatId:', chatId)

  // 当收到'refresh'参数时，刷新全部对话列表
  if (chatId === 'refresh') {
    console.log('正在刷新对话列表...')
    await loadChatRecords()
    console.log('对话列表刷新完成, 记录数量:', chatRecords.value.length)
    return
  }

  // 加载特定对话
  if (chatId) {
    await loadChatMessages(chatId)
  }
}

// 监视当前聊天ID变化，自动刷新对话列表
watch(currentChatId, async (newId, oldId) => {
  if (newId !== oldId) {
    console.log('当前对话ID变更，刷新对话列表')
    await loadChatRecords()
  }
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
  padding-top: 60px; /* 为固定导航栏留出空间 */
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #1f2023;
  overflow: hidden;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style>
