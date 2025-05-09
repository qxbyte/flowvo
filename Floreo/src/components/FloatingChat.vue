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
        <div v-for="(msg, i) in renderedMessages" :key="i" :class="['message', msg.role]">
          {{ msg.content }}
          <span v-if="isLoading && i === renderedMessages.length - 1 && msg.role === 'assistant'" class="typing-cursor"></span>
        </div>
        <div v-if="isLoading && (!renderedMessages.length || renderedMessages[renderedMessages.length-1].role !== 'assistant')" class="message assistant loading">
          <span class="loading-dots"><span>.</span><span>.</span><span>.</span></span>
        </div>
      </div>

      <div class="chat-input">
        <div class="input-container">
          <el-input
            ref="inputRef"
            v-model="input"
            placeholder="请输入内容..."
            @keydown.enter.prevent="handleEnterKey"
            @compositionstart="handleCompositionStart"
            @compositionupdate="handleCompositionUpdate"
            @compositionend="handleCompositionEnd"
            class="message-input"
            :disabled="isLoading"
          />
          <el-button
            type="primary"
            @click="sendMessage"
            :disabled="!input.trim() || isLoading"
            class="send-button"
            round
          >{{ isLoading ? '停止' : '发送' }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount, onMounted, computed } from 'vue'
import { Close } from '@element-plus/icons-vue'

// 滚动到底部
function scrollToBottom() {
  // 如果聊天窗口已收起，不执行滚动操作
  if (isCollapsed.value) {
    return;
  }
  
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    logInfo('已滚动到底部，消息容器高度:', messagesContainer.value.scrollHeight)
  } else {
    logWarn('消息容器不存在')
  }
}

// 调试功能 - 手动修复空消息
function fixEmptyMessages() {
  logInfo('尝试修复空消息...')
  if (messages.value.length > 0) {
    messages.value.forEach((msg, index) => {
      if (msg.role === 'assistant' && (!msg.content || msg.content.trim() === '')) {
        logInfo(`修复第${index}条消息`)
        msg.content = '响应内容已丢失，这是一个替代消息。可能是由于数据传输或解析问题导致。'
      }
    })
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 检查和修复AI消息
function fixAiMessages() {
  logInfo('检查AI消息状态...')

  // 移除所有空的AI消息，除了最后一条
  let lastAiIndex = -1;
  for (let i = messages.value.length - 1; i >= 0; i--) {
    if (messages.value[i].role === 'assistant') {
      if (lastAiIndex === -1) {
        lastAiIndex = i;
      } else if (!messages.value[i].content || messages.value[i].content.trim() === '') {
        logInfo(`删除空AI消息 index=${i}`);
        messages.value.splice(i, 1);
      }
    }
  }

  // 如果最后一条AI消息是空的，补充内容
  if (lastAiIndex !== -1 && (!messages.value[lastAiIndex].content || messages.value[lastAiIndex].content.trim() === '')) {
    logInfo('最后一条AI消息是空的，添加默认回复');
    messages.value[lastAiIndex].content = '服务器返回了空响应，请重试或联系管理员。';
  }

  // 确保所有AI消息都与其完整回复一致
  for (let i = 0; i < messages.value.length; i++) {
    if (messages.value[i].role === 'assistant' && fullResponses.value[i] && 
        messages.value[i].content !== fullResponses.value[i]) {
      logInfo(`修复AI消息内容不完整 index=${i}`);
      messages.value[i].content = fullResponses.value[i];
    }
  }

  // 打印当前消息状态
  logInfo('修复后的消息列表:', JSON.stringify(messages.value))

  // 确保UI更新
  nextTick(() => {
    scrollToBottom()
  })
}

// 添加日志控制函数，只在聊天窗口打开时输出日志
function logInfo(message, ...args) {
  // 如果聊天窗口打开或者这是重要的消息，才输出日志
  if (!isCollapsed.value || message.includes('错误') || message.includes('失败')) {
    console.log(message, ...args);
  }
}

function logWarn(message, ...args) {
  // 如果聊天窗口打开或者这是重要的消息，才输出警告
  if (!isCollapsed.value || message.includes('错误') || message.includes('失败')) {
    console.warn(message, ...args);
  }
}

function logError(message, ...args) {
  // 错误消息始终输出
  console.error(message, ...args);
}

// 状态管理
const isCollapsed = ref(true) // 默认收起
const input = ref('')
const messages = ref([])
const unreadCount = ref(0)
const messagesContainer = ref(null)
const isLoading = ref(false)
const currentChatId = ref('')
const inputRef = ref(null) // 添加对输入框的引用
let controller = null
// 添加响应等待状态，用于处理关闭窗口后的消息接收
const pendingResponse = ref(false)
// 添加本地存储键前缀
const MESSAGES_STORAGE_KEY_PREFIX = 'floating_chat_messages_'

// 打字机效果相关变量
const fullResponses = ref({}) // 保存完整回复内容
const typingSpeed = ref(30) // 打字速度(毫秒/字符)
const typingTimeouts = ref({}) // 保存打字定时器

// 获取当前用户ID
function getCurrentUserId() {
  // 从token中获取用户信息，这里假设token是JWT格式
  try {
    const token = localStorage.getItem('token')
    if (!token) return 'guest'
    
    // 尝试从token中解析用户ID
    // JWT格式: header.payload.signature
    const payload = token.split('.')[1]
    if (!payload) return 'guest'
    
    // Base64解码
    const decodedPayload = JSON.parse(atob(payload))
    const userId = decodedPayload.sub || decodedPayload.userId || decodedPayload.id || 'guest'
    
    return userId
  } catch (e) {
    logError('获取用户ID失败:', e)
    return 'guest' // 默认返回guest
  }
}

// 获取存储键
function getStorageKey() {
  const userId = getCurrentUserId()
  return `${MESSAGES_STORAGE_KEY_PREFIX}${userId}`
}

// 保存消息到本地存储
function saveMessagesToStorage() {
  try {
    const storageKey = getStorageKey()
    
    // 在保存前确保所有AI消息的内容都是完整的
    const messagesForStorage = messages.value.map((msg, index) => {
      if (msg.role === 'assistant' && fullResponses.value[index] && msg.content !== fullResponses.value[index]) {
        return {...msg, content: fullResponses.value[index]}
      }
      return {...msg}
    })
    
    localStorage.setItem(storageKey, JSON.stringify(messagesForStorage))
    logInfo('消息已保存到本地存储:', storageKey)
  } catch (e) {
    logError('保存消息到本地存储失败:', e)
  }
}

// 从本地存储加载消息
function loadMessagesFromStorage() {
  try {
    const storageKey = getStorageKey()
    const savedMessages = localStorage.getItem(storageKey)
    if (savedMessages) {
      const parsedMessages = JSON.parse(savedMessages)
      messages.value = parsedMessages.map(msg => ({...msg})) // 保留完整内容，避免闪烁
      logInfo('从本地存储加载了', parsedMessages.length, '条消息, 键:', storageKey)
      
      // 确保所有消息都正确显示
      nextTick(() => {
        // 滚动到底部
        scrollToBottom()
      })
    } else {
      // 如果没有找到当前用户的消息，清空消息列表
      messages.value = []
      logInfo('未找到当前用户的消息记录，已清空消息列表')
    }
  } catch (e) {
    logError('从本地存储加载消息失败:', e)
  }
}

// 添加输入法状态跟踪
const isComposing = ref(false)
const compositionText = ref('') // 存储输入法组合文本

// 处理输入法组合开始
function handleCompositionStart(event) {
  console.log('输入法组合开始:', event.data)
  isComposing.value = true
  compositionText.value = event.data || ''
}

// 处理输入法组合更新
function handleCompositionUpdate(event) {
  console.log('输入法组合更新:', event.data)
  compositionText.value = event.data || ''
}

// 处理输入法组合结束
function handleCompositionEnd(event) {
  console.log('输入法组合结束:', event.data)
  isComposing.value = false
  compositionText.value = ''

  // 自动聚焦输入框，以确保用户可以继续输入
  focusInput()
}

// 聚焦输入框
function focusInput() {
  // 如果聊天窗口已收起，不执行聚焦操作
  if (isCollapsed.value) {
    return;
  }
  
  nextTick(() => {
    if (inputRef.value && inputRef.value.input) {
      inputRef.value.input.focus()
      logInfo('输入框已聚焦')
    } else {
      logWarn('无法找到输入框元素')
    }
  })
}

// 处理回车键 - 使用keydown而不是keyup，并阻止默认行为
function handleEnterKey(event) {
  // 如果正在输入法组合状态，不处理回车键
  if (isComposing.value) {
    logInfo('输入法组合中，忽略回车键')
    // 不阻止默认行为，让输入法完成其选词过程
    return
  }

  // 在非组合状态下，回车键触发发送
  sendMessage()
}

// 模拟打字机效果，逐字显示内容
function typeMessage(messageIndex, fullContent, skipTypingEffect = false) {
  // 保存完整回复
  if (!fullResponses.value[messageIndex]) {
    fullResponses.value[messageIndex] = fullContent
  } else {
    // 如果已有内容，更新完整回复
    fullResponses.value[messageIndex] = fullContent
  }

  // 如果需要跳过打字机效果，直接设置完整内容并返回
  if (skipTypingEffect) {
    if (messages.value[messageIndex]) {
      messages.value[messageIndex].content = fullContent
      // 确保立即更新UI并滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
    return
  }

  // 取消之前的打字定时器
  if (typingTimeouts.value[messageIndex]) {
    clearTimeout(typingTimeouts.value[messageIndex])
  }

  // 重置消息内容为空，准备开始打字
  if (messages.value[messageIndex]) {
    messages.value[messageIndex].content = ''
  }

  // 记录当前打字位置
  let currentIndex = 0

  // 定义逐字打字函数
  function typeNextChar() {
    if (currentIndex < fullContent.length) {
      // 添加下一个字符
      if (messages.value[messageIndex]) {
        // 每次添加1-3个字符，使打字速度不那么机械
        const charsToAdd = Math.min(Math.floor(Math.random() * 3) + 1, fullContent.length - currentIndex)
        messages.value[messageIndex].content += fullContent.substring(currentIndex, currentIndex + charsToAdd)
        currentIndex += charsToAdd

        // 安排下一个字符的添加
        const nextDelay = Math.max(10, Math.floor(typingSpeed.value * (Math.random() * 0.5 + 0.8)))
        typingTimeouts.value[messageIndex] = setTimeout(typeNextChar, nextDelay)

        // 滚动到底部
        scrollToBottom()
      }
    } else {
      // 打字结束，清除定时器引用
      typingTimeouts.value[messageIndex] = null
      
      // 确保消息内容完全与完整回复一致
      if (messages.value[messageIndex] && messages.value[messageIndex].content !== fullContent) {
        messages.value[messageIndex].content = fullContent
        scrollToBottom()
      }
    }
  }

  // 开始打字
  typeNextChar()
}

// 切换聊天窗口状态
function toggleChat() {
  isCollapsed.value = !isCollapsed.value
  if (!isCollapsed.value) {
    unreadCount.value = 0 // 打开聊天窗口时清除未读消息
    nextTick(() => {
      scrollToBottom() // 滚动到底部
      focusInput() // 自动聚焦输入框
    })
  } else {
    // 如果关闭窗口，但仍在等待响应，标记为需要增加未读消息
    if (isLoading.value) {
      pendingResponse.value = true
      logInfo('关闭聊天窗口时仍在等待响应')
    }
    
    // 保存当前消息到本地存储
    saveMessagesToStorage()
  }
}

// 发送消息
async function sendMessage() {
  if (isLoading.value) {
    // 如果正在加载中，则停止响应
    if (controller) {
      controller.abort()
      isLoading.value = false
    }
    return
  }

  if (!input.value.trim()) return

  // 添加用户消息
  const userMessage = input.value
  messages.value.push({ role: 'user', content: userMessage })
  logInfo('添加用户消息:', userMessage)

  // 注意：不再立即添加AI消息占位符，而是等待第一个有效响应时再添加

  // 暂存用户输入并清空输入框，提供更好的体验
  input.value = ''

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })

  // 定义变量跟踪是否已添加AI消息，每次新请求都要重置
  let aiMessageAdded = false;
  // 声明一个lastAiMessageIndex变量，用于在updateAiMessage函数中跟踪当前AI消息索引
  let lastAiMessageIndex = -1;

  try {
    isLoading.value = true

    // 设置超时保护，60秒后如果还在加载则强制结束
    const loadingTimeout = setTimeout(() => {
      if (isLoading.value) {
        logInfo('请求超时，强制结束加载状态')
        isLoading.value = false
        if (controller) {
          controller.abort()
          controller = null
        }

        // 如果聊天窗口已收起，增加未读消息计数
        if (isCollapsed.value) {
          unreadCount.value++
        }

        // 超时后也聚焦输入框
        focusInput()
      }
    }, 60000) // 延长到60秒

    // 准备请求参数
    const params = new URLSearchParams()
    params.append('question', userMessage)
    if (currentChatId.value) {
      params.append('chatId', currentChatId.value)
    }

    console.log(`发送请求: /api/function-call/invoke-stream?${params.toString()}`)

    // 处理流式响应
    const url = `/api/function-call/invoke-stream?${params.toString()}`
    console.log('请求URL:', url)

    // 创建一个 AbortController 实例，用于在需要时中断请求
    const abortController = new AbortController()
    controller = abortController

    // 定义更新消息内容的函数
    const updateAiMessage = (text) => {
      // 检查是否是控制指令
      if (!text || text === '[DONE]' || text.includes('data:[DONE]')) {
        logInfo('收到控制指令或完成标记，忽略: ', text)
        isLoading.value = false
        
        // 确保消息内容与完整回复一致
        if (lastAiMessageIndex !== -1 && fullResponses.value[lastAiMessageIndex]) {
          messages.value[lastAiMessageIndex].content = fullResponses.value[lastAiMessageIndex]
          nextTick(() => {
            scrollToBottom()
          })
        }
        
        // 检查是否需要处理未读消息通知（窗口关闭后收到回复）
        if (pendingResponse.value && isCollapsed.value) {
          unreadCount.value++
          pendingResponse.value = false
          logInfo('消息接收完毕，未读计数增加到:', unreadCount.value)
          
          // 保存更新后的消息
          saveMessagesToStorage()
        }
        return
      }

      // 移除可能的data:前缀
      if (text.startsWith('data:')) {
        text = text.substring(5).trim()
      }

      // 再次检查处理后的文本是否是结束标记
      if (text === '[DONE]' || text.includes('[DONE]')) {
        logInfo('处理后发现完成标记，忽略并结束加载状态')
        isLoading.value = false
        
        // 确保消息内容与完整回复一致
        if (lastAiMessageIndex !== -1 && fullResponses.value[lastAiMessageIndex]) {
          messages.value[lastAiMessageIndex].content = fullResponses.value[lastAiMessageIndex]
          nextTick(() => {
            scrollToBottom()
          })
        }
        
        // 检查是否需要处理未读消息通知
        if (pendingResponse.value && isCollapsed.value) {
          unreadCount.value++
          pendingResponse.value = false
          logInfo('消息接收完毕，未读计数增加到:', unreadCount.value)
          
          // 保存更新后的消息
          saveMessagesToStorage()
        }
        return
      }

      // 清除文本中可能包含的[DONE]标记
      text = text.replace(/\[DONE\]/g, '').trim()
      if (!text) {
        logInfo('清除[DONE]后文本为空，忽略此更新')
        return
      }

      // 确保有AI消息可以更新
      let lastAiMessage = null

      // 只在当前请求还没有创建AI消息时才查找或创建
      if (!aiMessageAdded) {
        // 查找最后一条AI消息，确保它是当前对话的响应
        // 如果已经有其他AI消息，我们应该创建新的，而不是更新旧的
        for (let i = messages.value.length - 1; i >= 0; i--) {
          // 在本次请求中，找到用户消息后就停止向前查找
          if (messages.value[i].role === 'user') {
            break;
          }

          if (messages.value[i].role === 'assistant') {
            // 不使用已有的AI消息，确保总是创建新的响应
            break;
          }
        }

        // 创建一个新的AI消息
        const newMessage = { role: 'assistant', content: '' }
        messages.value.push(newMessage)
        lastAiMessageIndex = messages.value.length - 1
        logInfo('创建新的AI消息')

        // 标记已添加消息
        aiMessageAdded = true

        // 使用打字机效果显示新消息
        typeMessage(lastAiMessageIndex, text)
      } else {
        // 查找本次请求创建的AI消息
        for (let i = messages.value.length - 1; i >= 0; i--) {
          if (messages.value[i].role === 'assistant') {
            lastAiMessageIndex = i
            lastAiMessage = messages.value[i]
            break
          }
        }

        // 如果找到了此次请求创建的消息，更新它
        if (lastAiMessageIndex !== -1) {
          typeMessage(lastAiMessageIndex, text)
          logInfo('更新AI消息')
        }
      }

      // 确保UI更新
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 定义变量跟踪是否收到了任何实际内容
    let receivedAnyContent = false

    try {
      // 添加简单的重试机制
      let maxRetries = 3
      let retryCount = 0
      let success = false

      while (!success && retryCount <= maxRetries) {
        try {
          logInfo(`尝试请求 (${retryCount + 1}/${maxRetries + 1})`)

          const response = await fetch(url, {
            method: 'GET',
            headers: {
              'Accept': 'text/event-stream',
              'Cache-Control': 'no-cache',
              'Connection': 'keep-alive',  // 添加持久连接
              'Authorization': `Bearer ${localStorage.getItem('token')}` // 添加认证令牌
            },
            signal: abortController.signal,
            credentials: 'same-origin'  // 添加凭证
          })

          if (!response.ok) {
            const errorText = await response.text().catch(() => '')
            logError(`请求失败: 状态码 ${response.status}, 响应内容: ${errorText}`)

            if (response.status === 401) {
              throw new Error(`网络请求失败，状态码: ${response.status}`)
            }

            retryCount++
            if (retryCount > maxRetries) {
              if (messages.value.some(msg => msg.role === 'assistant' && (!msg.content || msg.content === ''))) {
                updateAiMessage(`网络请求失败，状态码: ${response.status}。请检查网络连接或联系管理员。`)
              }
              isLoading.value = false // 确保加载状态结束
              throw new Error(`网络请求失败，状态码: ${response.status}`)
            }

            // 根据错误类型增加等待时间
            const waitTime = response.status >= 500 ? 2000 : 1000
            await new Promise(resolve => setTimeout(resolve, waitTime))
            continue
          }

          // 标记为成功，跳出重试循环
          success = true

          // 处理成功的响应
          const reader = response.body.getReader()
          const decoder = new TextDecoder('utf-8')

          // 收集完整响应用于调试
          let fullResponse = '';
          let lastUpdateTime = Date.now();

          // 超时检测
          const connectionTimeoutId = setTimeout(() => {
            if (isLoading.value && Date.now() - lastUpdateTime > 15000) {
              logInfo('数据流超时，可能是网络中断');

              // 更新消息显示网络问题
              if (messages.value.some(msg => msg.role === 'assistant' && (!msg.content || msg.content === ''))) {
                updateAiMessage('与服务器的连接中断，请检查您的网络连接或代理设置。')
              }

              // 结束加载
              isLoading.value = false;
              reader.cancel('连接超时');
            }
          }, 15000);

          while (true) {
            try {
              const { done, value } = await reader.read()

              if (done) {
                logInfo('读取完成')
                clearTimeout(connectionTimeoutId);

                // 结束加载状态
                isLoading.value = false
                controller = null

                // 确保最后一条消息完整显示
                for (let i = messages.value.length - 1; i >= 0; i--) {
                  if (messages.value[i].role === 'assistant') {
                    if (fullResponses.value[i] && messages.value[i].content !== fullResponses.value[i]) {
                      logInfo('确保最后一条消息完整显示');
                      messages.value[i].content = fullResponses.value[i];
                    }
                    break;
                  }
                }

                // 立即检查AI消息
                fixAiMessages()
                
                // 保存消息到本地存储
                saveMessagesToStorage()
                
                // 检查是否需要处理未读消息通知
                if (pendingResponse.value && isCollapsed.value) {
                  unreadCount.value++
                  pendingResponse.value = false
                  logInfo('流程结束时，检测到未读消息需要增加，未读计数:', unreadCount.value)
                }

                // 5秒后再次检查消息是否显示
                setTimeout(() => {
                  if (messages.value.some(msg => msg.role === 'assistant' && (!msg.content || msg.content === ''))) {
                    logInfo('检测到空消息，自动修复')
                    fixEmptyMessages()
                  }
                }, 5000)

                break
              }

              // 更新最后接收数据的时间
              lastUpdateTime = Date.now();

              // 解码数据
              const text = decoder.decode(value, { stream: true })
              logInfo('收到数据块:', text)

              // 收集完整响应
              fullResponse += text;

              // 处理不同格式的响应数据
              const lines = text.trim().split('\n');

              for (const line of lines) {
                if (!line.trim()) continue; // 跳过空行

                let processedText = line.trim();

                // 处理包含data:前缀的情况
                if (processedText.startsWith('data:')) {
                  processedText = processedText.substring(5).trim()
                  logInfo('移除data:前缀后:', processedText)
                }

                // 检查是否是结束标记
                if (processedText === '[DONE]' || processedText.includes('[DONE]')) {
                  logInfo('收到完成标记，结束加载状态')
                  isLoading.value = false

                  // 如果文本只包含[DONE]，则直接跳过
                  if (processedText === '[DONE]' || processedText.replace(/\[DONE\]/g, '').trim() === '') {
                    continue
                  }

                  // 如果文本包含其他内容，则清除[DONE]并处理剩余内容
                  processedText = processedText.replace(/\[DONE\]/g, '').trim()
                  if (!processedText) {
                    continue
                  }
                }

                // 忽略空响应
                if (!processedText) {
                  logInfo('收到空数据块，跳过处理')
                  continue
                }

                // 尝试解析JSON (有些SSE返回JSON格式)
                try {
                  const jsonData = JSON.parse(processedText);
                  // 如果成功解析JSON并包含内容字段
                  if (jsonData.content) {
                    processedText = jsonData.content;
                    logInfo('从JSON提取内容:', processedText);
                  }
                } catch (e) {
                  // 不是JSON，继续处理原始文本
                }

                // 标记已收到内容
                receivedAnyContent = true

                logInfo('处理后的响应内容:', processedText)

                // 特殊消息处理（如错误提示）
                if (processedText.includes('无法连接到AI服务') ||
                    processedText.includes('连接AI服务失败') ||
                    processedText.includes('无法连接到OpenAI')) {
                  logInfo('检测到连接错误消息');
                  isLoading.value = false;
                }

                // 更新消息
                updateAiMessage(processedText)
              }

              // 滚动到底部
              scrollToBottom()
            } catch (error) {
              logError('读取数据块时出错:', error);

              // 如果是网络连接中断，显示友好消息
              if (error.message.includes('network') || error.message.includes('connection')) {
                updateAiMessage('与服务器的连接中断，请检查您的网络连接。');
              }

              clearTimeout(connectionTimeoutId);
              isLoading.value = false;
              break;
            }
          }

        } catch (error) {
          if (error.name === 'AbortError') {
            logInfo('请求被中止')
            isLoading.value = false
            break
          }

          logError(`尝试 ${retryCount + 1} 失败:`, error)
          retryCount++

          // 检查是否是网络连接错误
          const isNetworkError = error.message.includes('network') ||
            error.message.includes('连接') ||
            error.message.includes('connect') ||
            error.name === 'TypeError';

          // 最后一次重试也失败
          if (retryCount > maxRetries) {
            // 在错误处理中，总是创建新的错误消息，不修改已有的消息
            const errorMessage = isNetworkError
              ? '无法连接到服务器，请检查您的网络连接或代理设置。'
              : '网络请求失败: ' + error.message;

            // 添加新的错误消息，无论是否已添加AI消息
            const newMessage = { role: 'assistant', content: '' };
            messages.value.push(newMessage);
            const msgIndex = messages.value.length - 1;
            // 使用打字机效果显示错误消息
            typeMessage(msgIndex, errorMessage);

            isLoading.value = false // 确保加载状态结束
            throw error
          }

          // 网络错误需要更长的等待时间
          const waitTime = isNetworkError ? 2000 : 1000;
          // 等待一会再重试
          await new Promise(resolve => setTimeout(resolve, waitTime))
        }
      }

      // 清理超时定时器
      clearTimeout(loadingTimeout)

      logInfo('响应处理完成')
      isLoading.value = false
      controller = null

      // 确保最后一条消息完整显示
      for (let i = messages.value.length - 1; i >= 0; i--) {
        if (messages.value[i].role === 'assistant') {
          if (fullResponses.value[i] && messages.value[i].content !== fullResponses.value[i]) {
            logInfo('响应处理完成后，确保消息完整显示');
            messages.value[i].content = fullResponses.value[i];
          }
          break;
        }
      }

      // 立即检查AI消息
      fixAiMessages()
      
      // 保存消息到本地存储
      saveMessagesToStorage()
      
      // 检查是否需要处理未读消息通知
      if (pendingResponse.value && isCollapsed.value) {
        unreadCount.value++
        pendingResponse.value = false
        logInfo('流程结束时，检测到未读消息需要增加，未读计数:', unreadCount.value)
      }

      // 自动聚焦输入框，让用户可以继续输入
      focusInput()

    } catch (error) {
      logError('流处理错误:', error)

      // 清理超时定时器
      clearTimeout(loadingTimeout)

      // 如果是用户主动取消请求，不显示错误
      if (error.name !== 'AbortError') {
        // 总是添加新的错误消息，不替换已有消息
        const newMessage = { role: 'assistant', content: '' };
        messages.value.push(newMessage);
        const msgIndex = messages.value.length - 1;
        // 使用打字机效果显示错误消息
        typeMessage(msgIndex, '接收响应时出错: ' + error.message);
      }

      isLoading.value = false
      controller = null

      // 自动聚焦输入框，即使发生错误
      focusInput()
    }

    // 最终安全检查，确保加载状态被关闭
    setTimeout(() => {
      if (isLoading.value) {
        logInfo('检测到加载状态未正确关闭，强制关闭')
        isLoading.value = false
      }

      // 再次尝试聚焦输入框
      focusInput()
    }, 1000)

    // 如果聊天窗口已收起，增加未读消息计数
    if (isCollapsed.value) {
      unreadCount.value++
    }

  } catch (error) {
    logError('流处理错误:', error)

    // 清理超时定时器
    clearTimeout(loadingTimeout)

    // 如果是用户主动取消请求，不显示错误
    if (error.name !== 'AbortError') {
      // 总是添加新的错误消息，不替换已有消息
      const newMessage = { role: 'assistant', content: '' };
      messages.value.push(newMessage);
      const msgIndex = messages.value.length - 1;
      // 使用打字机效果显示错误消息
      typeMessage(msgIndex, '接收响应时出错: ' + error.message);
    }

    isLoading.value = false
    controller = null

    // 自动聚焦输入框，即使发生错误
    focusInput()
  }
}

// 清除旧格式的消息存储
function clearOldMessageStorage() {
  try {
    // 旧的存储键
    const oldKey = 'floating_chat_messages'
    if (localStorage.getItem(oldKey)) {
      localStorage.removeItem(oldKey)
      logInfo('已清除旧格式的消息存储')
    }
  } catch (e) {
    logError('清除旧消息存储失败:', e)
  }
}

// 添加渲染优化：使用计算属性确保显示完整消息
const renderedMessages = computed(() => {
  return messages.value.map((msg, index) => {
    if (msg.role === 'assistant' && fullResponses.value[index] && msg.content !== fullResponses.value[index]) {
      return { ...msg, content: fullResponses.value[index] };
    }
    return msg;
  });
});

// 添加确保消息显示完整的函数
function ensureMessagesComplete() {
  logInfo('确保所有消息显示完整');
  let hasUpdated = false;
  
  messages.value.forEach((msg, index) => {
    if (msg.role === 'assistant' && fullResponses.value[index] && msg.content !== fullResponses.value[index]) {
      messages.value[index].content = fullResponses.value[index];
      hasUpdated = true;
    }
  });
  
  if (hasUpdated) {
    logInfo('已更新部分消息内容为完整版本');
    nextTick(() => {
      scrollToBottom();
    });
  }
}

// 定时检查消息完整性
function setupMessagesCheck() {
  // 每隔5秒检查一次消息完整性
  const checkInterval = setInterval(() => {
    if (!isCollapsed.value) {  // 只在聊天窗口打开时检查
      ensureMessagesComplete();
    }
  }, 5000);
  
  // 在组件销毁时清理定时器
  onBeforeUnmount(() => {
    clearInterval(checkInterval);
  });
}

// 组件挂载完成后执行
onMounted(() => {
  logInfo('FloatingChat组件已挂载')

  // 清除旧格式的消息存储
  clearOldMessageStorage()

  // 从本地存储加载消息
  loadMessagesFromStorage()
  
  // 确保所有AI消息内容完整显示
  setTimeout(() => {
    fixAiMessages()
    ensureMessagesComplete() // 额外检查
  }, 500)
  
  // 设置定时检查机制
  setupMessagesCheck()
  
  // 添加用户登录状态变化监听
  window.addEventListener('storage', (event) => {
    if (event.key === 'token') {
      // 检测到token变化，可能是用户登录/登出/切换
      logInfo('检测到用户登录状态变化，重新加载消息')
      // 清空现有消息和聊天ID
      messages.value = []
      currentChatId.value = ''
      // 清空完整回复缓存
      fullResponses.value = {}
      // 加载新用户的消息
      loadMessagesFromStorage()
    }
  })

  // 监听window.onerror，捕获任何JS错误
  window.onerror = function(message, source, lineno, colno, error) {
    logError('全局JS错误:', message, error)
    return false
  }

  // 为了解决iOS中文输入法问题，添加全局事件处理
  document.addEventListener('keydown', (event) => {
    // 特殊处理iOS上的中文输入法回车问题
    if (event.key === 'Enter' && isComposing.value) {
      logInfo('全局捕获：输入法组合中的回车键，阻止传播')
      event.stopPropagation()
    }
  }, true)  // 使用捕获阶段

  // 5秒后检查消息，防止有空消息没有正确显示
  setTimeout(() => {
    if (messages.value.some(msg => msg.role === 'assistant' && (!msg.content || msg.content === ''))) {
      logInfo('初始化检测到空消息，自动修复')
      fixEmptyMessages()
    }
  }, 5000)
})

// 组件销毁时清理
onBeforeUnmount(() => {
  // 清理所有打字机定时器
  Object.values(typingTimeouts.value).forEach(timeout => {
    if (timeout) clearTimeout(timeout)
  })

  // 取消正在进行的请求
  if (controller) {
    controller.abort()
  }
  
  // 移除storage事件监听
  window.removeEventListener('storage', () => {
    logInfo('已移除storage事件监听')
  })
})

// 监听消息变化，自动滚动到底部并确保消息完整
watch(
  () => messages.value.length,
  () => {
    nextTick(() => {
      scrollToBottom()
      // 确保新消息内容完整
      ensureMessagesComplete()
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
  display: inline-block;
  max-width: 80%;
  word-break: break-word;
  white-space: pre-wrap; /* 保留换行和空格 */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  clear: both;
}

.user {
  float: right;
  text-align: left;
  margin-left: auto;
  background-color: #d9ecff;
  color: #2b5998;
  border-bottom-right-radius: 4px;
}

.assistant {
  float: left;
  text-align: left;
  margin-right: auto;
  background-color: #e4f2e4;
  color: #1a1a1a;
  border-bottom-left-radius: 4px;
}

.loading-dots span {
  animation: loading 1.4s infinite both;
  display: inline-block;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loading {
  0% {
    opacity: 0.2;
    transform: translateY(0);
  }
  20% {
    opacity: 1;
    transform: translateY(-3px);
  }
  40% {
    opacity: 0.2;
    transform: translateY(0);
  }
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

.message-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 0 15px;
}

.message-input :deep(.el-input__wrapper.is-focus) {
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

.typing-cursor::after {
  content: '|';
  animation: blink 1s step-start infinite;
  font-weight: normal;
  margin-left: 2px;
  opacity: 0.7;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>
