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
        <div class="chat-item-content" @click="loadChat(record.id)">
          {{ record.title }}
        </div>
        <button class="chat-item-menu" @click="openMenu($event, record)">
          <EllipsisHorizontalIcon class="h-3.5 w-3.5" />
        </button>
      </div>
    </div>

    <!-- 操作菜单 -->
    <div v-if="showMenu"
         class="chat-menu"
         :style="{ top: menuPosition.y + 'px', left: menuPosition.x + 'px' }">
      <button class="menu-item" @click="renameChat">
        <PencilIcon class="h-3.5 w-3.5 mr-2" />
        重命名
      </button>
      <button class="menu-item delete" @click="deleteChat">
        <TrashIcon class="h-3.5 w-3.5 mr-2" />
        删除
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue'
import {
  Bars4Icon,
  PlusCircleIcon,
  EllipsisHorizontalIcon,

} from '@heroicons/vue/24/solid'
import {
  PencilIcon,
  TrashIcon
} from '@heroicons/vue/16/solid'

const emit = defineEmits(['update:currentChatId', 'loadChat'])

// 状态变量声明
const showMenu = ref(false)
const selectedChatId = ref('')
const selectedChatTitle = ref('')
const menuPosition = ref({ x: 0, y: 0 })
const isSidebarCollapsed = ref(false)

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
  menuPosition.value = {
    x: event.clientX,
    y: event.clientY
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

// 重命名对话
const renameChat = async () => {
  const newTitle = prompt('请输入新的对话名称', selectedChatTitle.value)
  if (newTitle && newTitle !== selectedChatTitle.value) {
    try {
      const response = await fetch(`/api/chat/${selectedChatId.value}/rename`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ title: newTitle })
      })

      if (response.ok) {
        // 重命名成功后立即刷新聊天列表
        const recordsResponse = await fetch('/api/chat/records')
        if (recordsResponse.ok) {
          const updatedRecords = await recordsResponse.json()
          props.chatRecords.splice(0, props.chatRecords.length, ...updatedRecords)
        }
      } else {
        throw new Error('重命名失败')
      }
    } catch (error) {
      console.error('重命名失败:', error)
      alert('重命名失败，请稍后重试')
    }
  }
  showMenu.value = false
}

// 删除对话
const deleteChat = async () => {
  if (confirm('确定要删除这个对话吗？')) {
    try {
      const response = await fetch(`/api/chat/${selectedChatId.value}`, {
        method: 'DELETE'
      })

      if (response.ok) {
        if (props.currentChatId === selectedChatId.value) {
          emit('update:currentChatId', '')
        }
        // 删除成功后立即刷新聊天列表
        const recordsResponse = await fetch('/api/chat/records')
        if (recordsResponse.ok) {
          const updatedRecords = await recordsResponse.json()
          props.chatRecords.splice(0, props.chatRecords.length, ...updatedRecords)
        }
      } else {
        throw new Error('删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      alert('删除失败，请稍后重试')
    }
  }
  showMenu.value = false
}

// 创建新对话
const createNewChat = async () => {
  const response = await fetch('/api/chat/new', {
    method: 'POST'
  })
  if (response.ok) {
    const data = await response.json()
    // 将新对话添加到本地chatRecords数组
    props.chatRecords.push({
      id: data.id,
      title: '新的对话'
    })
    emit('update:currentChatId', data.id)
    emit('loadChat')
  }
}

// 加载对话
const loadChat = (chatId: string) => {
  emit('update:currentChatId', chatId)
  emit('loadChat', chatId)
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
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  padding: 4px;
  z-index: 1000;
  min-width: 120px;
  margin-top: 4px;
  transform: translateX(-90%);
}

.menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 16px;
  text-align: left;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
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
Bars3Icon {
  color: #333;
}
</style>
