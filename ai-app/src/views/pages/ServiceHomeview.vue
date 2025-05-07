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
          <el-menu-item index="other-modules">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 中间展示区 -->
      <el-container>
        <el-header style="background: #fff; border-bottom: 1px solid #eee; padding: 0 20px; height: 48px; display: flex; align-items: center;">
          <h2 style="margin: 0; padding-top: 4px;">{{ titleMap[activeMenu] }}</h2>
        </el-header>

        <el-main style="background-color: #fff; padding-top: 10px;">
          <component :is="currentComponent" />
        </el-main>
      </el-container>
    </el-container>
  </div>

  <!-- 悬浮聊天组件 -->
  <FloatingChat />
</template>

<script setup>
import { ref, computed } from 'vue'
import { Briefcase, TrendCharts, Setting } from '@element-plus/icons-vue'
import BusinessList from './business/BusinessList.vue'
import OrderList from './business/OrderList.vue'
import CustomerList from './business/CustomerList.vue'
import FloatingChat from '@/components/FloatingChat.vue'

const activeMenu = ref('business-list')

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
  padding-top: 60px; /* 为固定导航栏留出空间 */
}
</style>
