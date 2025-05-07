<template>
  <div class="floating-chat-container" :class="{ 'collapsed': isCollapsed }">
    <!-- 聊天图标按钮 (收起状态) -->
    <div class="chat-icon" v-if="isCollapsed" @click="toggleChat">
      <el-badge :value="unreadCount > 0 ? unreadCount : ''" :hidden="unreadCount === 0">
        <div class="chatgpt-icon">
          <svg width="41" height="41" viewBox="0 0 41 41" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M37.5324 16.8707C37.9808 15.5241 38.1363 14.0974 37.9886 12.6859C37.8409 11.2744 37.3934 9.91076 36.676 8.68622C35.6126 6.83404 33.9882 5.3676 32.0373 4.4985C30.0864 3.62941 27.9098 3.40259 25.8215 3.85078C24.8796 2.7893 23.7219 1.94125 22.4257 1.36341C21.1295 0.785575 19.7249 0.491269 18.3058 0.500197C16.1708 0.495044 14.0893 1.16803 12.3614 2.42214C10.6335 3.67624 9.34853 5.44666 8.6917 7.47815C7.30085 7.76286 5.98686 8.3414 4.8377 9.17505C3.68854 10.0087 2.73073 11.0782 2.02839 12.312C0.956464 14.1591 0.498905 16.2988 0.721698 18.4228C0.944492 20.5467 1.83612 22.5449 3.268 24.1293C2.81966 25.4759 2.66413 26.9026 2.81182 28.3141C2.95951 29.7256 3.40701 31.0892 4.12437 32.3138C5.18791 34.1659 6.8123 35.6322 8.76321 36.5013C10.7141 37.3704 12.8907 37.5973 14.9789 37.1492C15.9208 38.2107 17.0786 39.0587 18.3747 39.6366C19.6709 40.2144 21.0755 40.5087 22.4946 40.4998C24.6307 40.5054 26.7133 39.8321 28.4418 38.5772C30.1704 37.3223 31.4556 35.5506 32.1119 33.5179C33.5027 33.2332 34.8167 32.6547 35.9659 31.821C37.115 30.9874 38.0728 29.9178 38.7752 28.684C39.8458 26.8371 40.3023 24.6979 40.0789 22.5748C39.8556 20.4517 38.9639 18.4544 37.5324 16.8707ZM22.4978 37.8849C20.7443 37.8874 19.0459 37.2733 17.6994 36.1501C17.7601 36.117 17.8666 36.0586 17.936 36.0161L25.9004 31.4156C26.1003 31.3019 26.2663 31.137 26.3813 30.9378C26.4964 30.7386 26.5563 30.5124 26.5549 30.2825V19.0542L29.9213 20.998C29.9389 21.0068 29.9541 21.0198 29.9656 21.0359C29.977 21.052 29.9842 21.0707 29.9867 21.0902V30.3889C29.9842 32.375 29.1946 34.2791 27.7909 35.6841C26.3872 37.0892 24.4838 37.8806 22.4978 37.8849ZM6.39227 31.0064C5.51397 29.5047 5.19742 27.7511 5.49804 26.0429C5.55718 26.0832 5.66048 26.1425 5.73461 26.1839L13.699 30.7844C13.8975 30.8985 14.1233 30.9533 14.3532 30.9533C14.583 30.9533 14.8088 30.8985 15.0073 30.7844L24.731 25.1121V28.9979C24.7321 29.0177 24.7283 29.0376 24.7199 29.0556C24.7115 29.0736 24.6988 29.0893 24.6829 29.1012L16.6317 33.7497C14.9096 34.7416 12.8643 35.0097 10.9447 34.4954C9.02506 33.9811 7.38785 32.7263 6.39227 31.0064ZM4.29707 13.6194C5.17156 12.0998 6.55279 10.9364 8.19885 10.3327C8.19885 10.4013 8.19491 10.5228 8.19491 10.6071V19.808C8.19351 20.0378 8.25334 20.2638 8.36823 20.4629C8.48312 20.6619 8.64893 20.8267 8.84863 20.9404L18.5723 26.6127L15.206 28.5566C15.1894 28.5653 15.1703 28.5695 15.1505 28.5686C15.1307 28.5677 15.1119 28.5618 15.0965 28.5516L7.04532 23.9031C5.16375 22.8533 3.79597 21.1011 3.18553 19.0508C2.57509 17.0005 2.76235 14.7979 3.70216 12.8816C3.88761 12.5006 4.11225 12.1383 4.37099 11.7998L4.29707 13.6194ZM20.0522 24.6611L16.9738 22.8134V15.4047C16.9724 15.2311 16.9994 15.0589 17.0537 14.8963C17.108 14.7337 17.1884 14.5843 17.2903 14.4562C17.3922 14.328 17.5142 14.2242 17.6494 14.1509C17.7846 14.0777 17.9308 14.0366 18.0803 14.0298C18.5932 14.0158 19.0992 14.1511 19.5366 14.4211L26.935 18.7312L23.5687 20.6751C23.5377 20.6923 23.5126 20.7163 23.4964 20.7452C23.4803 20.7741 23.4738 20.8065 23.4779 20.8385V24.6611H20.0522ZM35.7031 26.3725C34.8282 27.8889 33.4455 29.0553 31.7991 29.6604C31.7991 29.5917 31.7991 29.4702 31.7991 29.386V20.1851C31.7991 19.9553 31.7393 19.7303 31.6244 19.5313C31.5096 19.3322 31.3437 19.1675 31.144 19.0538L21.4203 13.3815L24.7866 11.4376C24.8032 11.4288 24.8223 11.4247 24.8421 11.4256C24.8619 11.4264 24.8807 11.4324 24.8961 11.4425L32.9473 16.091C34.0602 16.7249 35.0096 17.6332 35.7025 18.7262C36.3955 19.8193 36.8109 21.0659 36.9073 22.3588C37.0037 23.6517 36.7779 24.9473 36.2522 26.1203C36.1032 26.5139 35.9176 26.8928 35.7031 27.2546V26.3725ZM37.5324 16.8707C37.9808 15.5241 38.1363 14.0974 37.9886 12.6859C37.8409 11.2744 37.3934 9.91076 36.676 8.68622C35.6126 6.83404 33.9882 5.3676 32.0373 4.4985C30.0864 3.62941 27.9098 3.40259 25.8215 3.85078C24.8796 2.7893 23.7219 1.94125 22.4257 1.36341C21.1295 0.785575 19.7249 0.491269 18.3058 0.500197C16.1708 0.495044 14.0893 1.16803 12.3614 2.42214C10.6335 3.67624 9.34853 5.44666 8.6917 7.47815C7.30085 7.76286 5.98686 8.3414 4.8377 9.17505C3.68854 10.0087 2.73073 11.0782 2.02839 12.312C0.956464 14.1591 0.498905 16.2988 0.721698 18.4228C0.944492 20.5467 1.83612 22.5449 3.268 24.1293C2.81966 25.4759 2.66413 26.9026 2.81182 28.3141C2.95951 29.7256 3.40701 31.0892 4.12437 32.3138C5.18791 34.1659 6.8123 35.6322 8.76321 36.5013C10.7141 37.3704 12.8907 37.5973 14.9789 37.1492C15.9208 38.2107 17.0786 39.0587 18.3747 39.6366C19.6709 40.2144 21.0755 40.5087 22.4946 40.4998C24.6307 40.5054 26.7133 39.8321 28.4418 38.5772C30.1704 37.3223 31.4556 35.5506 32.1119 33.5179C33.5027 33.2332 34.8167 32.6547 35.9659 31.821C37.115 30.9874 38.0728 29.9178 38.7752 28.684C39.8458 26.8371 40.3023 24.6979 40.0789 22.5748C39.8556 20.4517 38.9639 18.4544 37.5324 16.8707Z" fill="#10a37f"/>
          </svg>
        </div>
      </el-badge>
    </div>

    <!-- 展开的聊天窗口 -->
    <div class="chat-panel" v-else>
      <div class="chat-header">
        <h3>ChatOne</h3>
        <div class="chat-actions">
          <el-button type="text" @click="toggleChat">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
          {{ msg.content }}
        </div>
      </div>

      <div class="chat-input">
        <div class="input-container">
          <el-input 
            v-model="input" 
            placeholder="请输入内容..." 
            @keyup.enter="sendMessage"
            class="message-input"
          />
          <el-button 
            type="primary" 
            @click="sendMessage" 
            :disabled="!input.trim()"
            class="send-button"
            round
          >发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ChatDotRound, Close } from '@element-plus/icons-vue'

// 状态管理
const isCollapsed = ref(true) // 默认收起
const input = ref('')
const messages = ref([])
const unreadCount = ref(0)
const messagesContainer = ref(null)

// 切换聊天窗口状态
function toggleChat() {
  isCollapsed.value = !isCollapsed.value
  if (!isCollapsed.value) {
    unreadCount.value = 0 // 打开聊天窗口时清除未读消息
    nextTick(() => {
      scrollToBottom() // 滚动到底部
    })
  }
}

// 发送消息
function sendMessage() {
  if (!input.value.trim()) return
  
  messages.value.push({ role: 'user', content: input.value })
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
  
  // 模拟 AI 回复，可替换为真实 API
  setTimeout(() => {
    messages.value.push({ role: 'assistant', content: '这是AI的回复：' + input.value })
    
    // 如果聊天窗口已收起，增加未读消息计数
    if (isCollapsed.value) {
      unreadCount.value++
    } else {
      // 如果聊天窗口已展开，滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
  }, 600)
  
  input.value = ''
}

// 滚动到底部
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化，自动滚动到底部
watch(
  () => messages.value.length,
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  }
)
</script>

<style scoped>
.floating-chat-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.chat-icon {
  cursor: pointer;
  transition: transform 0.3s;
}

.chat-icon:hover {
  transform: scale(1.1);
}

.chatgpt-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 41px;
  height: 41px;
  border-radius: 50%;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-panel {
  width: 350px;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background-color: #f9f9f9;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #eaeaea;
}

.chat-header h3 {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #fff;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.05);
}

.message {
  padding: 10px 14px;
  margin-bottom: 10px;
  border-radius: 18px;
  max-width: 80%;
  word-break: break-word;
}

.user {
  text-align: right;
  margin-left: auto;
  background-color: #ecf5ff;
  color: #409eff;
  border-bottom-right-radius: 4px;
}

.assistant {
  text-align: left;
  margin-right: auto;
  background-color: #f0f9eb;
  color: #67c23a;
  border-bottom-left-radius: 4px;
}

.chat-input {
  padding: 12px;
  background-color: #fff;
  border-top: 1px solid #eaeaea;
}

.input-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.message-input {
  flex: 1;
}

.send-button {
  flex-shrink: 0;
}

.rounded-input :deep(.el-input__wrapper) {
  border-radius: 18px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.rounded-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.rounded-input :deep(.el-input-group__append) {
  padding: 0;
  background-color: transparent;
  border: none;
}

.send-button {
  margin-left: 8px;
  transition: all 0.3s;
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 动画效果 */
.collapsed-enter-active,
.collapsed-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.collapsed-enter-from,
.collapsed-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>