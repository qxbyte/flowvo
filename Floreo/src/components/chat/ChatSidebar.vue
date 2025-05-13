<template>
  <div class="sidebar" :class="{ 'collapsed': isSidebarCollapsed }" >
    <div class="sidebar-header">
      <div class="header-buttons">
        <button class="icon-button sidebar-toggle" @click="toggleSidebar" :title="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'">
          <Bars4Icon class="h-4 w-4" /><span class="ml-2"></span>
        </button>
        <button class="icon-button new-chat" @click="createNewChat" :title="'新建对话'">
          <PlusCircleIcon class="h-4 w-4" /><span class="ml-2"></span>
        </button>
      </div>
    </div>
    <div class="chat-list">
      <div v-for="record in chatRecords"
           :key="record.id"
           class="chat-item"
           :class="{ active: currentChatId === record.id }">
        <div class="chat-item-content" @click="loadChat(record.id)" v-if="editingChatId !== record.id">
          {{ record.title }}
        </div>
        <input 
          v-else
          ref="editInput"
          v-model="editingChatTitle"
          class="edit-input"
          @keyup.enter="confirmRenaming"
          @blur="cancelRenaming"
          @keyup.esc="cancelRenaming"
        />
        <button class="chat-item-menu" @click="openMenu($event, record)">
          <EllipsisHorizontalIcon class="h-3.5 w-3.5" />
        </button>
      </div>
    </div>

    <!-- 操作菜单 -->
    <div v-if="showMenu"
         class="chat-menu"
         :style="{ top: menuPosition.y + 'px', left: menuPosition.x + 'px' }">
      <button class="menu-item" @click="startEditing">
        <span>重命名</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="menu-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
        </svg>
      </button>
      <button class="menu-item delete" @click="showDeleteDialog">
        <span>删除</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="menu-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="3 6 5 6 21 6"></polyline>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
        </svg>
      </button>
    </div>

    <!-- 删除确认弹窗 -->
    <ConfirmDialog
      ref="deleteConfirmDialog"
      title="删除对话"
      message="删除后将无法恢复，是否继续？"
      confirmText="确认"
      cancelText="取消"
      type="danger"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits, nextTick } from 'vue'
import {
  Bars4Icon,
  PlusCircleIcon,
  EllipsisHorizontalIcon,

} from '@heroicons/vue/24/solid'
import {
  PencilIcon,
  TrashIcon
} from '@heroicons/vue/16/solid'
import { useChat } from '@/composables/useChat'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const emit = defineEmits(['update:currentChatId', 'loadChat'])
const chatApi = useChat()
const router = useRouter()

// 状态变量声明
const showMenu = ref(false)
const selectedChatId = ref('')
const selectedChatTitle = ref('')
const menuPosition = ref({ x: 0, y: 0 })
const isSidebarCollapsed = ref(false)
const editingChatId = ref('')
const editingChatTitle = ref('')
const editInput = ref<HTMLInputElement | null>(null)
const deleteConfirmDialog = ref<any>(null)

// Props
const props = defineProps<{
  chatRecords: Array<{id: string, title: string}>
  currentChatId: string
}>()

// 切换侧边栏状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 打开操作菜单
const openMenu = (event: MouseEvent, record: { id: string, title: string }) => {
  event.stopPropagation()
  selectedChatId.value = record.id
  selectedChatTitle.value = record.title
  
  // 设置菜单位置，右侧显示
  menuPosition.value = {
    x: event.clientX + 10, // 向右偏移
    y: event.clientY - 10  // 向上偏移一点，使其更好地对齐
  }
  
  showMenu.value = true

  // 点击其他地方关闭菜单
  const closeMenu = (e: MouseEvent) => {
    if (!e.target || !(e.target as Element).closest('.chat-menu')) {
      showMenu.value = false
      document.removeEventListener('click', closeMenu)
    }
  }
  setTimeout(() => {
    document.addEventListener('click', closeMenu)
  }, 0)
}

// 开始编辑对话
const startEditing = () => {
  showMenu.value = false
  editingChatId.value = selectedChatId.value
  editingChatTitle.value = selectedChatTitle.value
  
  // 自动聚焦输入框
  nextTick(() => {
    if (editInput.value) {
      editInput.value.focus()
      editInput.value.select()
    }
  })
}

// 取消重命名
const cancelRenaming = () => {
  editingChatId.value = ''
}

// 确认重命名
const confirmRenaming = async () => {
  if (editingChatTitle.value && editingChatTitle.value !== selectedChatTitle.value) {
    try {
      console.log('调用重命名对话API, ID:', selectedChatId.value, '新标题:', editingChatTitle.value)
      const success = await chatApi.renameChat(selectedChatId.value, editingChatTitle.value)
      
      if (success) {
        console.log('重命名成功，刷新对话列表')
        // 重新加载对话列表，向父组件发送refresh信号
        emit('loadChat', 'refresh')
      }
    } catch (error) {
      console.error('重命名失败:', error)
      alert('重命名失败，请稍后重试')
    }
  }
  editingChatId.value = ''
}

// 显示删除确认对话框
const showDeleteDialog = () => {
  showMenu.value = false
  if (deleteConfirmDialog.value) {
    deleteConfirmDialog.value.show()
  }
}

// 确认删除
const confirmDelete = async () => {
  try {
    console.log('调用删除对话API, ID:', selectedChatId.value)
    const success = await chatApi.deleteChat(selectedChatId.value)
    
    if (success) {
      console.log('删除成功，更新currentChatId和刷新对话列表')
      
      // 如果删除的是当前对话，重置currentChatId
      if (props.currentChatId === selectedChatId.value) {
        emit('update:currentChatId', '')
      }
      
      // 重新加载对话列表，向父组件发送refresh信号
      emit('loadChat', 'refresh')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

// 取消删除
const cancelDelete = () => {
  console.log('用户取消了删除操作')
}

// 创建新对话
const createNewChat = async () => {
  try {
    console.log('开始创建新对话...')
    console.log('调用创建新对话API...')
    const chatId = await chatApi.createNewChat()
    
    if (chatId) {
      console.log('创建新对话成功，ID:', chatId)
      
      // 更新当前聊天ID
      emit('update:currentChatId', chatId)
      
      // 重新加载对话列表，向父组件发送refresh信号
      emit('loadChat', 'refresh')
    } else {
      console.error('创建对话失败: 未返回有效的对话ID')
    }
  } catch (error) {
    console.error('创建新对话失败:', error)
    alert('创建新对话失败，请稍后重试')
  }
}

// 加载对话
const loadChat = (chatId: string) => {
  console.log('开始加载对话, ID:', chatId)
  emit('update:currentChatId', chatId)
  emit('loadChat', chatId)
  console.log('已触发loadChat事件')
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  background-color: #f0f0f0;
  display: flex;
  flex-direction: column;
  padding: 8px;
  transition: all 0.3s ease;
  border-right: 1px solid #e5e7eb;
}

.sidebar.collapsed {
  width: 60px;
  padding: 8px 4px;
  background-color: transparent;
  border-right: none;
}

.sidebar.collapsed .chat-list,
.sidebar.collapsed .icon-button span {
  display: none;
}

.header-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.icon-button {
  padding: 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  min-width: 36px;
  min-height: 36px;
}

.icon-button svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.icon-button.sidebar-toggle {
  margin-right: auto;
}

.icon-button.new-chat {
  margin-left: auto;
}

.icon-button:hover {
  background-color: rgba(0,0,0,0.05);
}

.chat-list {
  margin-top: 8px;
  overflow-y: auto;
  flex: 1;
}

.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px;
  color: #333;
  border-radius: 4px;
  margin: 1px 0;
}

.chat-item-content {
  flex: 1;
  padding: 0 6px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-menu {
  width: 28px;
  height: 28px;
  padding: 4px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  opacity: 0.2;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 2px;
}

.chat-item:hover .chat-item-menu {
  opacity: 0.8;
}

.chat-item-menu:hover {
  opacity: 1 !important;
  background-color: rgba(0,0,0,0.05);
}

.chat-menu {
  position: fixed;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  padding: 2px;
  z-index: 1000;
  min-width: 100px;
  margin-top: 4px;
  transform: none; /* 移除之前的translateX(-90%) */
}

.menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 5px 10px;
  text-align: left;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
  justify-content: space-between;
  font-size: 13px;
}

.menu-icon {
  margin-left: 8px;
  color: #666;
}

.menu-item.delete .menu-icon {
  color: #dc2626;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.menu-item.delete {
  color: #dc2626;
}

.menu-item.delete:hover {
  background-color: #fee2e2;
}

.chat-item:hover {
  background-color: rgba(0,0,0,0.05);
}

.chat-item.active {
  background-color: rgba(0,0,0,0.1);
}

/* 编辑框样式 */
.edit-input {
  flex: 1;
  background-color: #ffffff;
  border: 1px solid #4399ff;
  border-radius: 4px;
  padding: 4px 6px;
  font-size: 13px;
  margin-right: 4px;
  outline: none;
  box-shadow: 0 0 0 2px rgba(67, 153, 255, 0.2);
}

.edit-input:focus {
  border-color: #4399ff;
}

/* 重命名对话框样式 */
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.rename-dialog {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 420px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: relative;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
}

.close-button:hover {
  color: #333;
}

.rename-dialog h3 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  font-weight: 500;
  text-align: center;
}

.rename-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 20px;
  font-size: 16px;
}

.rename-input:focus {
  outline: none;
  border-color: #4399ff;
  box-shadow: 0 0 0 2px rgba(67, 153, 255, 0.2);
}

.confirm-dialog-buttons {
  display: flex;
  justify-content: space-between;
}

.cancel-button, .confirm-button {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.cancel-button {
  background-color: #f5f5f5;
  color: #333;
  margin-right: 10px;
}

.confirm-button {
  background-color: #4399ff;
  color: white;
}

.cancel-button:hover {
  background-color: #e5e5e5;
}

.confirm-button:hover {
  background-color: #3385e5;
}
</style>
