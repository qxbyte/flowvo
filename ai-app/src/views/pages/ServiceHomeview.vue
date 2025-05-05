import NavBar from '@/components/NavBar.vue'

<template>
  <NavBar />
  <div class="service-layout">
    <el-container style="height: 100vh">
      <!-- 左侧菜单 -->
      <el-aside width="180px" style="background-color: #fff; border-right: 1px solid #eee">
        <el-menu :default-active="activeMenu" @select="handleMenuClick" style="border-right: none">
          <el-sub-menu index="business">
            <template #title>
              <el-icon><Briefcase /></el-icon>
              <span>业务管理</span>
            </template>
            <el-menu-item index="business-list">业务列表</el-menu-item>
            <el-menu-item index="order-list">订单管理</el-menu-item>
            <el-menu-item index="customer-list">客户管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="business-system">
            <el-icon><Monitor /></el-icon>
            <span>业务系统</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 中间展示区 -->
      <el-container>
        <el-header style="background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
          <h3 style="margin: 0;">{{ titleMap[activeMenu] }}</h3>
        </el-header>

        <el-main style="background-color: #fafafa;">
          <component :is="currentComponent" />
        </el-main>
      </el-container>

      <!-- 右侧聊天框 -->
      <el-aside width="350px" style="background-color: #fff; border-left: 1px solid #eee; padding: 10px;">
        <div class="chat-wrapper">
          <div class="chat-messages">
            <div v-for="(msg, i) in messages" :key="i" :class="msg.role">
              {{ msg.content }}
            </div>
          </div>

          <div class="chat-input">
            <el-input v-model="input" placeholder="请输入内容..." @keyup.enter="sendMessage" />
            <el-button type="primary" @click="sendMessage" style="margin-top: 10px">发送</el-button>
          </div>
        </div>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Briefcase, TrendCharts, Monitor } from '@element-plus/icons-vue'
import BusinessList from './business/BusinessList.vue'
import OrderList from './business/OrderList.vue'
import CustomerList from './business/CustomerList.vue'

const activeMenu = ref('business-list')
const input = ref('')
const messages = ref([])

const titleMap = {
  'business-list': '业务列表',
  'order-list': '订单管理',
  'customer-list': '客户管理',
  'statistics': '数据统计',
  'settings': '系统设置'
}

const componentMap = {
  'business-list': BusinessList,
  'order-list': OrderList,
  'customer-list': CustomerList
}

const currentComponent = computed(() => componentMap[activeMenu.value])

function handleMenuClick(index) {
  activeMenu.value = index
}

function sendMessage() {
  if (!input.value.trim()) return
  messages.value.push({ role: 'user', content: input.value })
  // 模拟 AI 回复，可替换为真实 API
  setTimeout(() => {
    messages.value.push({ role: 'assistant', content: '这是AI的回复：' + input.value })
  }, 600)
  input.value = ''
}
</script>

<style scoped>
.service-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  background-color: #ffffff;
  padding-top: 60px; /* 为固定导航栏留出空间 */; /* 为固定导航栏留出空间 */
}
.chat-wrapper {
  display: flex;
  flex-direction: column;
  top: 0;
  height: 90vh;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #eaeaea;
  border-radius: 5px;
}
.chat-input {
  display: flex;
  flex-direction: column;
}
.user {
  text-align: right;
  color: #409eff;
}
.assistant {
  text-align: left;
  color: #67c23a;
}
</style>
