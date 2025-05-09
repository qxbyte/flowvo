<template>
  <div class="floating-chat-container" :class="{ 'collapsed': isCollapsed }">
    <!-- 聊天图标按钮 (收起状态) -->
    <div class="chat-icon" v-if="isCollapsed" @click="toggleChat">
      <el-badge :value="unreadCount > 0 ? unreadCount : ''" :hidden="unreadCount === 0">
        <div class="chatgpt-icon">
          <svg width="41" height="41" viewBox="0 0 41 41" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M37.5324 16.8707C37.9808 15.5241 38.1363 14.0974 37.9886 12.6859C37.8409 11.2744 37.3934 9.91076 36.676 8.68622C35.6126 6.83404 33.9882 5.3676 32.0373 4.4985C30.0864 3.62941 27.9098 3.40259 25.8215 3.85078C24.8796 2.7893 23.7219 1.94125 22.4257 1.36341C21.1295 0.785575 19.7249 0.491269 18.3058 0.500197C16.1708 0.495044 14.0893 1.16803 12.3614 2.42214C10.6335 3.67624 9.34853 5.44666 8.6917 7.47815C7.30085 7.76286 5.98686 8.3414 4.8377 9.17505C3.68854 10.0087 2.73073 11.0782 2.02839 12.312C0.956464 14.1591 0.498905 16.2988 0.721698 18.4228C0.944492 20.5467 1.83612 22.5449 3.268 24.1293C2.81966 25.4759 2.66413 26.9026 2.81182 28.3141C2.95951 29.7256 3.40701 31.0892 4.12437 32.3138C5.18791 34.1659 6.8123 35.6322 8.76321 36.5013C10.7141 37.3704 12.8907 37.5973 14.9789 37.1492C15.9208 38.2107 17.0786 39.0587 18.3747 39.6366C19.6709 40.2144 21.0755 40.5087 22.4946 40.4998C24.6307 40.5054 26.7133 39.8321 28.4418 38.5772C30.1704 37.3223 31.4556 35.5506 32.1119 33.5179C33.5027 33.2332 34.8167 32.6547 35.9659 31.821C37.115 30.9874 38.0728 29.9178 38.7752 28.684C39.8458 26.8371 40.3023 24.6979 40.0789 22.5748C39.8556 20.4517 38.9639 18.4544 37.5324 16.8707Z" fill="#10a37f"/>
          </svg>
        </div>
      </el-badge>
    </div>

    <!-- 展开的聊天窗口 -->
    <div class="chat-panel" v-else>
      <div class="chat-header">
        <div class="chat-header-left">
          <el-dropdown @command="handleChatSelect" trigger="click">
            <span class="chat-title-dropdown">
              <h3>{{ currentChatTitle || 'AI对话' }}</h3>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="chat in userChats" :key="chat.id" :command="chat.id">
                  <span :class="{'current-chat': chat.id === currentChatId}" style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                    <span>{{ chat.title || '未命名对话' }}</span>
                    <span class="chat-actions" v-if="chat.id === currentChatId">
                      <el-icon @click.stop="renameChat(chat)" style="margin-left: 8px; cursor: pointer;"><Edit /></el-icon>
                      <el-icon @click.stop="deleteChat(chat)" style="margin-left: 8px; cursor: pointer; color: #F56C6C;"><Delete /></el-icon>
                    </span>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item v-if="userChats.length === 0" disabled>无对话记录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="chat-actions">
          <el-tooltip content="新建对话" placement="bottom" :enterable="false">
            <el-button type="text" @click="createNewChat" :loading="isCreatingChat">
              <el-icon><Plus /></el-icon>
            </el-button>
          </el-tooltip>
          <el-button type="text" @click="toggleChat">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-for="(msg, i) in renderedMessages" :key="i" :class="['message', msg.role]">
          {{ msg.content }}
          <span v-if="msg.isTyping" class="typing-cursor"></span>
        </div>
        <div class="clearfix"></div>
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
            class="message-input rounded-input"
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
import { Close, ArrowDown, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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
const currentChatTitle = ref('') // 当前对话标题
const inputRef = ref(null) // 添加对输入框的引用
const userChats = ref([]) // 用户AIPROCESS类型的所有对话记录
const isCreatingChat = ref(false) // 是否正在创建新对话
let controller = null
// 添加响应等待状态，用于处理关闭窗口后的消息接收
const pendingResponse = ref(false)
// 添加本地存储键前缀
const MESSAGES_STORAGE_KEY_PREFIX = 'floating_chat_messages_'

// 打字机效果相关变量
const fullResponses = ref({}) // 保存完整回复内容
const typingSpeed = ref(50) // 打字速度(毫秒/字符)，增加到50ms使效果更明显
const typingTimeouts = ref({}) // 保存打字机定时器

// 定义更新消息内容的函数 - 作为全局函数定义，而不是sendMessage内的局部函数
const updateAiMessage = (text) => {
  // 检查是否是控制指令
  if (!text || text === '[DONE]' || text.includes('data:[DONE]')) {
    console.log('收到控制指令或完成标记，忽略: ', text);
    isLoading.value = false;
    return;
  }

  // 移除可能的data:前缀
  if (text.startsWith('data:')) {
    text = text.substring(5).trim();
  }

  // 再次检查处理后的文本是否是结束标记
  if (text === '[DONE]' || text.includes('[DONE]')) {
    console.log('处理后发现完成标记，忽略并结束加载状态');
    isLoading.value = false;
    return;
  }

  // 清除文本中可能包含的[DONE]标记
  text = text.replace(/\[DONE\]/g, '').trim();
  if (!text) {
    console.log('清除[DONE]后文本为空，忽略此更新');
    return;
  }

  console.log('处理收到的AI响应:', text.substring(0, 30) + (text.length > 30 ? '...' : ''));

  // 如果还没有创建AI消息，创建一个新的
  if (!aiMessageAdded) {
    // 创建一个新的AI消息，初始为空字符串
    const newMessage = { role: 'assistant', content: '' };
    messages.value.push(newMessage);
    lastAiMessageIndex = messages.value.length - 1;
    console.log('创建新的AI消息，索引:', lastAiMessageIndex);

    // 将字符直接添加到消息中
    messages.value[lastAiMessageIndex].content = text;

    // 保存完整回复，以便后续可以确保内容完整
    fullResponses.value[lastAiMessageIndex] = text;

    // 标记已添加消息
    aiMessageAdded = true;

    // 滚动到底部
    nextTick(scrollToBottom);
    return;
  }

  // 如果已经创建了AI消息，找到它并更新
  if (lastAiMessageIndex === -1) {
    // 查找最后一条AI消息
    for (let i = messages.value.length - 1; i >= 0; i--) {
      if (messages.value[i].role === 'assistant') {
        lastAiMessageIndex = i;
        break;
      }
    }
  }

  // 找到了AI消息，更新它
  if (lastAiMessageIndex !== -1) {
    // 获取当前内容
    const currentContent = fullResponses.value[lastAiMessageIndex] || '';
    // 添加新内容
    const newContent = currentContent + text;

    // 更新完整回复记录
    fullResponses.value[lastAiMessageIndex] = newContent;

    // 直接更新DOM中的消息内容
    messages.value[lastAiMessageIndex].content = newContent;

    console.log('更新AI消息内容，当前长度:', newContent.length);

    // 滚动到底部
    nextTick(scrollToBottom);
  } else {
    console.error('找不到要更新的AI消息');
  }
}

// 添加全局变量存储当前AI消息状态
let aiMessageAdded = false;
let lastAiMessageIndex = -1;

// 检查token是否有效
async function checkTokenValidity() {
  const token = localStorage.getItem('token')
  if (!token) {
    logWarn('未找到用户令牌')
    return false
  }

  // 增加token格式检查
  try {
    // 检查token格式：应为JWT格式 (header.payload.signature)
    const parts = token.split('.')
    if (parts.length !== 3) {
      logError('令牌格式不正确，不是有效的JWT格式')
      return false
    }
    
    // 尝试解析payload
    try {
      const payload = JSON.parse(atob(parts[1]))
      logInfo('令牌payload解析成功，用户ID:', payload.sub || payload.userId || payload.id)
    } catch (e) {
      logError('令牌payload解析失败:', e)
    }
    
    logInfo('开始验证令牌有效性，令牌长度:', token.length)
    
    // 进行一个简单的API调用来验证token
    const response = await fetch('/api/function-call/user-chats', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    })
    
    logInfo('令牌验证响应状态:', response.status)

    if (response.status === 401) {
      // 尝试读取错误消息
      let errorMessage = '令牌已过期或无效'
      try {
        const errorText = await response.text()
        // 处理中文编码问题
        logError('服务器返回401错误:', errorText)
      } catch (e) {
        logError('无法读取401错误响应内容:', e)
      }
      
      logError(errorMessage)
      
      // 显示登录过期消息
      ElMessage.error({
        message: '登录已过期，请刷新页面重新登录',
        duration: 5000,
        showClose: true
      })
      
      // 清除无效的token
      localStorage.removeItem('token')
      localStorage.removeItem('isAuthenticated')
      return false
    } else if (!response.ok) {
      logError('API调用失败，状态码:', response.status)
      try {
        const errorText = await response.text()
        logError('错误响应内容:', errorText)
      } catch (e) {
        logError('无法读取错误响应内容')
      }
      return false
    }

    logInfo('令牌验证成功')
    return true
  } catch (error) {
    logError('验证令牌时出错:', error)
    return false
  }
}

// 获取当前用户ID
function getCurrentUserId() {
  // 从token中获取用户信息，这里假设token是JWT格式
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      logWarn('获取用户ID失败: 未找到token')
      return 'guest'
    }

    // 检查token格式
    const parts = token.split('.')
    if (parts.length !== 3) {
      logError('获取用户ID失败: token不是有效的JWT格式')
      return 'guest'
    }

    // 尝试解码和解析payload
    try {
      const base64Url = parts[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      }).join(''))

      const payload = JSON.parse(jsonPayload)
      
      // 尝试从payload中提取userId (可能是sub, userId, id等字段)
      const userId = payload.sub || payload.userId || payload.id || 'guest'
      logInfo('从JWT解析出用户ID:', userId)
      
      return userId
    } catch (e) {
      logError('解析JWT payload失败:', e)
      return 'guest'
    }
  } catch (e) {
    logError('获取用户ID失败:', e)
    return 'guest' // 默认返回guest
  }
}

// 获取存储键
function getStorageKey(chatId = null) {
  const userId = getCurrentUserId()
  const id = chatId || currentChatId.value || 'default'
  return `${MESSAGES_STORAGE_KEY_PREFIX}${userId}_${id}`
}

// 保存消息到本地存储
function saveMessagesToStorage() {
  try {
    const storageKey = getStorageKey()

    // 如果没有当前对话ID，不保存
    if (!currentChatId.value) {
      logWarn('保存消息到本地存储失败: 没有当前对话ID')
      return
    }

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
function loadMessagesFromStorage(chatId = null) {
  try {
    const storageKey = getStorageKey(chatId)
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
      // 如果没有找到当前对话的消息，清空消息列表
      messages.value = []
      logInfo('未找到当前对话的消息记录，已清空消息列表')
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
  console.log(`打字机效果开始: 消息索引=${messageIndex}, 内容长度=${fullContent.length}, 跳过效果=${skipTypingEffect}`);

  // 直接保存完整回复，这样后续能确保内容一致
  fullResponses.value[messageIndex] = fullContent;

  // 如果需要跳过打字机效果，直接设置完整内容并返回
  if (skipTypingEffect) {
    if (messages.value[messageIndex]) {
      messages.value[messageIndex].content = fullContent;
      console.log('跳过打字机效果，直接显示完整内容');
      // 确保立即更新UI并滚动到底部
      nextTick(scrollToBottom);
    }
    return;
  }

  // 取消之前的打字定时器
  if (typingTimeouts.value[messageIndex]) {
    clearTimeout(typingTimeouts.value[messageIndex]);
    console.log(`取消之前的打字定时器: 消息索引=${messageIndex}`);
  }

  // 确保消息存在
  if (!messages.value[messageIndex]) {
    console.error(`找不到索引为 ${messageIndex} 的消息`);
    return;
  }

  // 完全重置内容为空，从头开始打字
  messages.value[messageIndex].content = '';
  console.log('重置消息内容为空，准备开始打字');

  // 当前显示的字符位置
  let currentPos = 0;
  const totalLength = fullContent.length;

  // 打字函数 - 循环添加字符
  function type() {
    // 检查消息是否还存在
    if (!messages.value[messageIndex]) {
      console.error('打字过程中消息被删除');
      return;
    }

    // 如果已经完成，清理并返回
    if (currentPos >= totalLength) {
      // 确保最终内容与完整内容一致
      messages.value[messageIndex].content = fullContent;
      console.log('打字完成，内容已完整显示');

      // 滚动到底部
      scrollToBottom();

      // 清除打字定时器
      typingTimeouts.value[messageIndex] = null;
      return;
    }

    // 确定此次要添加多少字符 (1-2个)
    const charsToAdd = Math.min(1, totalLength - currentPos);

    // 添加字符
    messages.value[messageIndex].content += fullContent.substring(currentPos, currentPos + charsToAdd);
    currentPos += charsToAdd;

    // 每20字符记录一次进度
    if (currentPos % 20 === 0 || currentPos === totalLength) {
      console.log(`打字进度: ${currentPos}/${totalLength} (${Math.round(currentPos/totalLength*100)}%)`);
    }

    // 滚动到底部
    scrollToBottom();

    // 设置下一次打字的延迟，控制打字速度
    const delay = 30 + Math.floor(Math.random() * 20); // 30-50ms，稍微随机化以看起来更自然
    typingTimeouts.value[messageIndex] = setTimeout(type, delay);
  }

  // 开始打字过程
  console.log('开始打字过程');
  // 延迟一帧再开始，确保UI已更新
  nextTick(() => {
    type();
  });
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

    // 保存当前对话列表到本地存储
    try {
      localStorage.setItem('user_chat_list', JSON.stringify(userChats.value))
    } catch (e) {
      logError('保存对话列表失败:', e)
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

  // 如果没有当前对话ID，创建一个新的对话
  if (!currentChatId.value) {
    try {
      await createNewChat()
    } catch (error) {
      logError('创建对话失败，无法发送消息:', error)
      return
    }
  }

  // 添加用户消息
  const userMessage = input.value
  messages.value.push({ role: 'user', content: userMessage })
  console.log('添加用户消息:', userMessage)

  // 暂存用户输入并清空输入框，提供更好的体验
  input.value = ''

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })

  // 定义变量跟踪是否已添加AI消息，每次新请求都要重置
  aiMessageAdded = false;
  lastAiMessageIndex = -1;

  try {
    isLoading.value = true

    // 设置超时保护，60秒后如果还在加载则强制结束
    const loadingTimeout = setTimeout(() => {
      if (isLoading.value) {
        console.log('请求超时，强制结束加载状态')
        isLoading.value = false
        if (controller) {
          controller.abort()
          controller = null
        }
      }
    }, 60000) // 60秒超时

    // 准备请求参数
    const params = new URLSearchParams()
    params.append('question', userMessage)
    if (currentChatId.value) {
      params.append('chatId', currentChatId.value)
    }

    console.log(`发送请求: /api/function-call/invoke-stream?${params.toString()}`)

    // 处理流式响应
    const url = `/api/function-call/invoke-stream?${params.toString()}`

    // 创建一个 AbortController 实例，用于在需要时中断请求
    const abortController = new AbortController()
    controller = abortController

    // 尝试请求API
    try {
      console.log('正在发送API请求...');

      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('未找到用户令牌');
      }

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'text/event-stream',
          'Cache-Control': 'no-cache',
          'Connection': 'keep-alive',
          'Authorization': `Bearer ${token}`
        },
        signal: abortController.signal,
        credentials: 'same-origin'
      });

      if (!response.ok) {
        if (response.status === 401) {
          ElMessage.error({
            message: '登录已过期，请刷新页面重新登录',
            duration: 5000,
            showClose: true
          });
          throw new Error('登录已过期');
        }
        throw new Error(`请求失败: ${response.status}`);
      }

      // 获取响应流读取器
      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');

      console.log('开始读取响应流...');

      // 为了调试，显示一个即时的AI消息
      messages.value.push({ role: 'assistant', content: '' });
      lastAiMessageIndex = messages.value.length - 1;
      aiMessageAdded = true;

      // 循环读取流数据
      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          console.log('读取完成，流已关闭');
          break;
        }

        // 解码数据
        const chunk = decoder.decode(value, { stream: true });
        console.log('收到数据块长度:', chunk.length);

        // 处理收到的数据块
        if (chunk) {
          // 处理SSE格式的数据，按行分割
          const lines = chunk.trim().split('\n');

          for (const line of lines) {
            if (!line.trim()) continue;

            let processedLine = line.trim();

            // 处理SSE格式 "data:" 前缀
            if (processedLine.startsWith('data:')) {
              processedLine = processedLine.substring(5).trim();
            }

            // 忽略[DONE]标记
            if (processedLine === '[DONE]') {
              continue;
            }

            // 如果有内容，直接更新
            updateAiMessage(processedLine);
          }
        }
      }

      console.log('流处理完成');

    } catch (error) {
      console.error('流处理错误:', error);

      // 如果处理过程中没有添加AI消息，添加一个错误提示
      if (!aiMessageAdded) {
        messages.value.push({
          role: 'assistant',
          content: `发生错误: ${error.message}。请稍后重试或联系管理员。`
        });
      } else if (lastAiMessageIndex !== -1) {
        // 如果已经添加了消息但内容为空，添加错误提示
        if (!messages.value[lastAiMessageIndex].content) {
          messages.value[lastAiMessageIndex].content = `发生错误: ${error.message}。请稍后重试或联系管理员。`;
        }
      }
    } finally {
      // 确保加载状态被重置
      isLoading.value = false;
      controller = null;

      // 清除超时计时器
      clearTimeout(loadingTimeout);

      // 自动聚焦输入框
      nextTick(() => {
        focusInput();
        scrollToBottom();
      });
    }

  } catch (error) {
    console.error('消息发送失败:', error);

    // 显示错误消息
    ElMessage.error('发送消息失败，请稍后重试');

    // 清理资源
    isLoading.value = false;
    if (controller) {
      controller.abort();
      controller = null;
    }
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
    // 如果是AI消息，检查是否需要应用完整响应
    if (msg.role === 'assistant') {
      // 检查是否有完整响应和是否是最后一条消息正在加载
      const isLastMessageLoading = index === messages.value.length - 1 && isLoading.value;

      // 确保使用最新完整响应，添加打字光标标记
      if (fullResponses.value[index] && msg.content !== fullResponses.value[index]) {
        return {
          ...msg,
          content: fullResponses.value[index],
          isTyping: isLastMessageLoading
        };
      } else {
        // 如果内容已匹配完整响应，只添加打字标记
        return {
          ...msg,
          isTyping: isLastMessageLoading
        };
      }
    }

    // 对于非AI消息，直接返回
    return msg;
  });
});

// 添加一个确保打字机效果正确运行的测试函数
function testTypingEffect() {
  const testMsg = "这是一个测试消息，用于验证打字机效果是否正常工作。你应该能看到这段文字逐字显示出来。"

  // 创建一个测试消息
  const newIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  // 应用打字机效果
  typeMessage(newIndex, testMsg)

  logInfo('已启动打字机效果测试')
}

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

// 从API获取用户的所有AIPROCESS对话
async function fetchUserChats() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      logWarn('获取用户对话记录失败: 未找到用户令牌')
      userChats.value = [] // 确保清空对话列表
      return
    }

    // 检查登录状态
    const isLoggedIn = localStorage.getItem('isAuthenticated') === 'true'
    if (!isLoggedIn) {
      logWarn('用户未登录或登录状态无效')
      userChats.value = []
      return
    }

    logInfo('开始获取用户对话记录...')
    
    // 获取用户的所有AIPROCESS类型对话记录
    const response = await fetch('/api/function-call/user-chats', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin' // 确保发送凭证（cookies等）
    })

    logInfo('获取用户对话响应状态:', response.status)

    if (!response.ok) {
      if (response.status === 401) {
        // 会话过期，显示登录过期消息
        logError('登录已过期，状态码:', response.status)
        
        // 尝试读取响应内容
        let errorMessage = '登录已过期，请刷新页面重新登录'
        try {
          const errorBody = await response.text()
          logError('服务器返回错误:', errorBody)
          if (errorBody) {
            try {
              const errorJson = JSON.parse(errorBody)
              if (errorJson.message) {
                errorMessage = errorJson.message
              }
            } catch (e) {
              // 不是JSON格式，使用原始文本
              if (errorBody.length < 100) {
                errorMessage = errorBody
              }
            }
          }
        } catch (e) {
          logError('无法读取错误响应内容')
        }
        
        ElMessage.error({
          message: errorMessage,
          duration: 5000,
          showClose: true
        });
        
        // 清除无效的token
        localStorage.removeItem('token')
        localStorage.removeItem('isAuthenticated')
        userChats.value = []
        logError('登录已过期，请重新登录')
        return
      }
      
      // 尝试读取其他错误响应
      try {
        const errorBody = await response.text()
        logError(`获取用户对话记录失败(${response.status}):`, errorBody)
      } catch (e) {
        logError('无法读取错误响应内容')
      }
      
      throw new Error(`获取用户对话记录失败: ${response.status}`)
    }

    // 解析响应数据
    let chatList = []
    try {
      chatList = await response.json()
      if (!Array.isArray(chatList)) {
        logError('获取的对话记录格式错误，预期是数组但收到了:', typeof chatList)
        chatList = []
      }
    } catch (e) {
      logError('解析对话记录响应失败:', e)
      chatList = []
    }
    
    userChats.value = chatList
    logInfo('成功获取用户对话记录, 共', chatList.length, '条记录')

    // 如果当前没有选择对话但有对话记录，选择第一个
    if (!currentChatId.value && chatList.length > 0) {
      const firstChat = chatList[0]
      if (firstChat && firstChat.id) {
        logInfo('自动选择第一个对话:', firstChat.id)
        selectChat(firstChat.id, firstChat.title)
      } else {
        logError('无法自动选择对话，第一个对话数据无效:', firstChat)
      }
    } else if (chatList.length === 0) {
      // 如果没有对话记录，清空当前对话ID
      logInfo('没有找到任何对话记录')
      if (currentChatId.value) {
        currentChatId.value = ''
        currentChatTitle.value = ''
        messages.value = [{
          role: 'system',
          content: '您还没有任何对话记录，请开始新对话'
        }]
      }
    }
  } catch (error) {
    logError('获取用户对话记录失败:', error)
    userChats.value = [] // 确保设置为空数组
    
    // 显示错误通知
    ElMessage.error({
      message: '获取对话列表失败: ' + (error.message || '未知错误'),
      duration: 3000
    })
  }
}

// 获取指定对话的历史消息
async function fetchChatHistory(chatId) {
  // 严格检查chatId有效性
  if (!chatId || chatId === 'undefined' || chatId === 'null') {
    logError('获取对话历史消息失败: 提供的对话ID无效:', chatId)
    messages.value = [{
      role: 'system',
      content: '无法加载对话历史，对话ID无效'
    }]
    return
  }

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      logWarn('获取对话历史消息失败: 未找到用户令牌')
      messages.value = [{
        role: 'system',
        content: '请先登录后再查看对话历史'
      }]
      return
    }

    logInfo(`正在获取对话(${chatId})的历史消息...`)
    
    // 获取指定对话的历史消息
    const response = await fetch(`/api/function-call/chat-history?chatId=${chatId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    })

    logInfo('获取对话历史响应状态:', response.status)

    if (!response.ok) {
      if (response.status === 401) {
        // 尝试读取响应内容，了解更多错误信息
        let errorMessage = '登录已过期，请刷新页面重新登录'
        try {
          const errorBody = await response.text()
          logError('获取历史消息失败, 服务器返回:', errorBody)
          if (errorBody) {
            try {
              const errorJson = JSON.parse(errorBody)
              if (errorJson.message) {
                errorMessage = errorJson.message
              }
            } catch (e) {
              // 不是JSON格式，使用原始文本
              if (errorBody.length < 100) { // 避免过长的错误信息
                errorMessage = errorBody
              }
            }
          }
        } catch (e) {
          logError('无法读取错误响应内容')
        }
        
        // 会话过期，显示登录过期消息
        ElMessage.error({
          message: errorMessage,
          duration: 5000,
          showClose: true
        });
        
        messages.value = [{
          role: 'system',
          content: errorMessage
        }]
        
        // 清除无效的token
        localStorage.removeItem('token')
        localStorage.removeItem('isAuthenticated')
        
        logError('登录已过期，请重新登录')
        return
      } else if (response.status === 404) {
        logError('对话不存在或已被删除')
        messages.value = [{
          role: 'system',
          content: '找不到指定的对话，可能已被删除'
        }]
        return
      }
      
      throw new Error(`获取对话历史消息失败: ${response.status}`)
    }

    const messageList = await response.json()
    logInfo('成功获取对话历史消息, 共', messageList.length, '条消息')

    // 清空当前消息列表
    messages.value = []
    fullResponses.value = {}

    if (messageList.length === 0) {
      // 如果没有消息，添加一条欢迎消息
      messages.value.push({
        role: 'system',
        content: '欢迎开始新的对话！'
      })
    } else {
      // 加载历史消息
      messageList.forEach((msg, index) => {
        if (!msg.role || !msg.content) {
          logWarn('发现无效消息:', msg)
          return // 跳过无效消息
        }
        
        // 添加到消息列表，不使用打字机效果
        messages.value.push({
          role: msg.role,
          content: msg.content
        })

        // 保存完整响应
        if (msg.role === 'assistant') {
          fullResponses.value[index] = msg.content
        }
      })
    }

    // 保存到本地存储
    saveMessagesToStorage()

    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    logError('获取对话历史消息失败:', error)
    messages.value = [{
      role: 'system',
      content: `加载历史消息失败: ${error.message || '未知错误'}`
    }]
  }
}

// 选择对话
function selectChat(chatId, chatTitle) {
  // 先检查chatId是否有效
  if (!chatId || chatId === 'undefined') {
    logError('无效的对话ID:', chatId)
    return
  }

  if (chatId === currentChatId.value) {
    return // 已经是当前对话，不需要切换
  }

  logInfo('切换到对话:', chatId)
  currentChatId.value = chatId
  currentChatTitle.value = chatTitle || 'AI对话'

  // 获取对话历史消息
  fetchChatHistory(chatId)

  // 更新本地存储的当前对话ID
  localStorage.setItem('current_chat_id', chatId)
}

// 处理对话选择
function handleChatSelect(chatId) {
  // 防止传递undefined或null值
  if (!chatId || chatId === 'undefined' || chatId === 'null') {
    logError('尝试选择无效对话ID:', chatId)
    return
  }
  
  const selectedChat = userChats.value.find(chat => chat.id === chatId)
  if (selectedChat) {
    selectChat(chatId, selectedChat.title)
  } else {
    logError('无法找到ID为', chatId, '的对话')
  }
}

// 创建新对话
async function createNewChat() {
  try {
    isCreatingChat.value = true
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('未找到用户令牌，请先登录')
      isCreatingChat.value = false
      return
    }

    // 验证登录状态
    const isLoggedIn = localStorage.getItem('isAuthenticated') === 'true'
    if (!isLoggedIn) {
      ElMessage.error('用户未登录或登录状态无效，请刷新页面重新登录')
      isCreatingChat.value = false
      return
    }

    logInfo('开始创建新对话...')

    // 创建新的对话
    const response = await fetch('/api/function-call/create-chat', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    })

    logInfo('创建新对话响应状态:', response.status)

    if (!response.ok) {
      if (response.status === 401) {
        // 会话过期，显示登录过期消息
        // 尝试读取响应内容
        let errorMessage = '登录已过期，请刷新页面重新登录'
        try {
          const errorBody = await response.text()
          logError('创建对话失败, 服务器返回:', errorBody)
          if (errorBody && errorBody.length < 100) {
            errorMessage = errorBody
          }
        } catch (e) {
          logError('无法读取错误响应内容')
        }
        
        ElMessage.error({
          message: errorMessage,
          duration: 5000,
          showClose: true
        });
        
        // 清除登录状态
        localStorage.removeItem('token')
        localStorage.removeItem('isAuthenticated')
        
        logError('登录已过期，请重新登录')
        isCreatingChat.value = false
        return
      }
      
      // 尝试读取错误内容
      let errorMsg = '创建对话失败'
      try {
        const errorText = await response.text()
        logError(`创建对话失败(${response.status}):`, errorText)
        if (errorText && errorText.length < 100) {
          errorMsg += `: ${errorText}`
        } else {
          errorMsg += `: 状态码 ${response.status}`
        }
      } catch (e) {
        logError('无法读取错误响应')
        errorMsg += `: 状态码 ${response.status}`
      }
      
      ElMessage.error(errorMsg)
      isCreatingChat.value = false
      return
    }

    // 解析响应
    let data
    try {
      data = await response.json()
      if (!data || !data.id) {
        throw new Error('响应数据中没有对话ID')
      }
    } catch (e) {
      logError('解析创建对话响应失败:', e)
      ElMessage.error('创建对话失败: 无法解析服务器响应')
      isCreatingChat.value = false
      return
    }

    logInfo('创建新对话成功, ID:', data.id)

    // 更新当前对话ID和标题
    currentChatId.value = data.id
    currentChatTitle.value = 'AI对话'
    
    // 清空消息列表并添加欢迎消息
    messages.value = []
    fullResponses.value = {}
    
    // 添加欢迎消息
    messages.value.push({
      role: 'system',
      content: '新对话已创建，开始输入您的问题吧！'
    })
    
    // 确保系统消息显示
    nextTick(() => {
      scrollToBottom()
    })

    // 获取最新的对话列表
    await fetchUserChats()

    // 聚焦输入框
    focusInput()

    ElMessage.success('新对话已创建')
  } catch (error) {
    logError('创建新对话失败:', error)
    // 显示错误消息
    ElMessage.error('创建新对话失败: ' + (error.message || '未知错误'))
  } finally {
    isCreatingChat.value = false
  }
}

// 重命名对话
async function renameChat(chat) {
  try {
    // 显示重命名对话框
    const { value: newTitle } = await ElMessageBox.prompt(
      '请输入新的对话名称',
      '重命名对话',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputValue: chat.title || 'AI对话',
        inputValidator: (value) => {
          if (!value) {
            return '标题不能为空'
          }
          return true
        }
      }
    )

    if (newTitle && newTitle !== chat.title) {
      const token = localStorage.getItem('token')
      if (!token) {
        throw new Error('未找到用户令牌')
      }

      // 调用重命名对话API，使用现有的ChatController接口
      const response = await fetch(`/api/chat/${chat.id}/rename`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ title: newTitle })
      })

      if (!response.ok) {
        if (response.status === 401) {
          // 会话过期，显示登录过期消息
          ElMessage.error({
            message: '登录已过期，请刷新页面重新登录',
            duration: 5000,
            showClose: true
          });
          logError('登录已过期，请重新登录')
          return
        }
        throw new Error(`重命名失败: ${response.status}`)
      }

      // 更新本地对话列表
      const index = userChats.value.findIndex(c => c.id === chat.id)
      if (index !== -1) {
        userChats.value[index].title = newTitle
      }

      // 如果当前正在查看该对话，更新标题
      if (currentChatId.value === chat.id) {
        currentChatTitle.value = newTitle
      }

      ElMessage.success('重命名成功')

      // 刷新对话列表
      await fetchUserChats()
    }
  } catch (error) {
    logError('重命名对话失败:', error)
    ElMessage.error('重命名失败，请稍后重试')
  }
}

// 删除对话
async function deleteChat(chat) {
  try {
    // 显示删除确认对话框
    await ElMessageBox.confirm(
      '删除后将无法恢复，是否继续？',
      '删除对话',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('未找到用户令牌')
    }

    // 调用删除对话API，使用现有的ChatController接口
    const response = await fetch(`/api/chat/${chat.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) {
      if (response.status === 401) {
        // 会话过期，显示登录过期消息
        ElMessage.error({
          message: '登录已过期，请刷新页面重新登录',
          duration: 5000,
          showClose: true
        });
        logError('登录已过期，请重新登录')
        return
      }
      throw new Error(`删除失败: ${response.status}`)
    }

    // 从对话列表中移除
    const index = userChats.value.findIndex(c => c.id === chat.id)
    if (index !== -1) {
      userChats.value.splice(index, 1)
    }

    // 如果当前正在查看该对话，切换到另一个对话或清空
    if (currentChatId.value === chat.id) {
      if (userChats.value.length > 0) {
        selectChat(userChats.value[0].id, userChats.value[0].title)
      } else {
        currentChatId.value = ''
        currentChatTitle.value = ''
        messages.value = []
        fullResponses.value = {}
      }
    }

    ElMessage.success('删除成功')

    // 刷新对话列表
    await fetchUserChats()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    logError('删除对话失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

// 组件挂载完成后执行
onMounted(async () => {
  logInfo('FloatingChat组件已挂载')

  try {
    // 验证登录状态
    const isLoggedIn = localStorage.getItem('isAuthenticated') === 'true'
    const token = localStorage.getItem('token')
    
    if (!isLoggedIn || !token) {
      logWarn('用户未登录或登录状态已失效')
      messages.value = [{
        role: 'system',
        content: '请先登录以使用聊天功能'
      }]
      return
    }

    // 验证token有效性
    logInfo('验证用户令牌有效性...')
    const isTokenValid = await checkTokenValidity()
    if (!isTokenValid) {
      logError('用户令牌无效')
      messages.value = [{
        role: 'system',
        content: '登录已过期，请刷新页面重新登录'
      }]
      return
    }

    logInfo('用户登录状态有效，开始加载数据')

    // 清除旧格式的消息存储
    clearOldMessageStorage()

    // 解决可能出现的currentChatId为undefined的问题
    if (currentChatId.value === 'undefined' || currentChatId.value === 'null') {
      logWarn('发现无效的currentChatId，重置为空')
      currentChatId.value = ''
    }

    // 获取用户的所有对话记录
    await fetchUserChats()

    // 确保所有AI消息内容完整显示
    setTimeout(() => {
      fixAiMessages()
      ensureMessagesComplete() // 额外检查
    }, 500)

    // 设置定时检查机制
    setupMessagesCheck()

    // 定期检查token有效性
    const tokenCheckInterval = setInterval(async () => {
      if (isCollapsed.value) return // 聊天窗口折叠时不检查

      const isStillValid = await checkTokenValidity()
      if (!isStillValid) {
        logWarn('定期检查发现用户令牌已失效')
        clearInterval(tokenCheckInterval)
        
        messages.value = [{
          role: 'system',
          content: '登录已过期，请刷新页面重新登录'
        }]
        
        ElMessage.warning({
          message: '登录已过期，请刷新页面重新登录',
          duration: 5000,
          showClose: true
        })
      }
    }, 5 * 60 * 1000) // 每5分钟检查一次

    // 在组件销毁时清理定时器
    onBeforeUnmount(() => {
      clearInterval(tokenCheckInterval)
    })

    // 添加用户登录状态变化监听
    window.addEventListener('storage', (event) => {
      if (event.key === 'token' || event.key === 'isAuthenticated') {
        // 检测到token或登录状态变化
        logInfo('检测到用户登录状态变化，重新加载消息')
        // 清空现有消息和聊天ID
        messages.value = []
        currentChatId.value = ''
        // 清空完整回复缓存
        fullResponses.value = {}

        const isLoggedIn = localStorage.getItem('isAuthenticated') === 'true'
        if (isLoggedIn) {
          // 用户仍然登录，加载新用户的消息
          loadMessagesFromStorage()
          // 重新获取对话列表
          fetchUserChats()
        } else {
          // 用户已登出
          messages.value = [{
            role: 'system',
            content: '请先登录以使用聊天功能'
          }]
        }
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

    // 在窗口打开时自动测试打字机效果
    watch(isCollapsed, (newVal) => {
      if (!newVal && messages.value.length === 0) {
        // 如果窗口打开且没有消息，延迟1秒后运行测试
        setTimeout(() => {
          testTypingEffect()
        }, 1000)
      }
    })
  } catch (error) {
    logError('组件初始化出错:', error)
    messages.value = [{
      role: 'system',
      content: '聊天组件初始化失败: ' + (error.message || '未知错误')
    }]
  }
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
  max-height: 650px;
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

.chat-header-left {
  display: flex;
  align-items: center;
}

.chat-title-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.chat-title-dropdown:hover {
  background-color: #f5f7fa;
}

.chat-title-dropdown h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.dropdown-icon {
  margin-left: 5px;
  font-size: 12px;
  color: #909399;
}

.current-chat {
  color: #409EFF;
  font-weight: bold;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 5px;
}

.chat-actions .el-button {
  padding: 5px;
  color: #606266;
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
  font-size: 14px;
  line-height: 1.5;
}

.message.user {
  float: right;
  text-align: left;
  margin-left: auto;
  background-color: #d9ecff;
  color: #2b5998;
  border-bottom-right-radius: 4px;
}

.message.assistant {
  float: left;
  text-align: left;
  margin-right: auto;
  background-color: #e4f2e4;
  color: #1a1a1a;
  border-bottom-left-radius: 4px;
}

.message.system {
  float: none;
  margin: 10px auto;
  text-align: center;
  background-color: #f4f4f5;
  color: #909399;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 12px;
  max-width: 90%;
  display: block; /* 确保系统消息显示为块级元素，占据整行 */
  text-align: center; /* 文本居中 */
}

.message.assistant.loading {
  padding: 8px 14px;
  float: left;
  display: inline-block;
  min-width: 40px;
  text-align: center;
  background-color: #e4f2e4; /* 改为与AI回复相同的绿色背景 */
  border-radius: 16px; /* 更圆润的形状 */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); /* 更轻的阴影 */
}

.loading-dots span {
  animation: loading 1.4s infinite both;
  display: inline-block;
  font-size: 16px; /* 减小点的大小 */
  opacity: 0.2;
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

.typing-cursor::after {
  content: '|';
  display: inline-block;
  animation: blink 1s step-start infinite;
  font-weight: bold;
  margin-left: 2px;
  font-size: 16px;
  color: #000;
  opacity: 0.8;
  height: 16px;
  line-height: 16px;
  vertical-align: middle;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
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
  border-radius: 20px;
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

.clearfix {
  clear: both;
  height: 1px;
}
</style>
