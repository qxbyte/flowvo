<template>
  <div class="nav-container">
    <nav class="navbar">
      <div class="status-bar">
        <!-- 左侧区域：Logo和导航链接 -->
        <div class="left-section">
          <div class="nav-left">
            <router-link to="/" class="nav-logo">
              <img src="@/assets/logo.png" alt="AI Logo" class="logo-image" />
            </router-link>
            <ul class="nav-links">
              <li><router-link to="/documents" class="nav-link">文档管理</router-link></li>
              <li><router-link to="/chat" class="nav-link">知识库Ask</router-link></li>
              <li><router-link to="/service" class="nav-link">业务系统</router-link></li>
            </ul>
          </div>
        </div>

        <!-- 右侧区域：通知、语言切换和用户头像 -->
        <div class="right-section">
          <!-- 通知图标 -->
          <el-badge :value="3" class="notification-badge">
            <el-button class="notification-btn" :icon="Bell" circle />
          </el-badge>

          <!-- 浅色/深色模式 -->


          <!-- 用户头像和下拉菜单 -->
          <el-dropdown trigger="click" class="user-dropdown">
            <el-avatar :size="32" :src="userAvatar" />
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown-menu">
                <el-dropdown-item>个人信息</el-dropdown-item>
                <el-dropdown-item>设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

const switchLanguage = (lang: string) => {
  // 实现语言切换逻辑
  console.log('切换语言到:', lang)
}

const handleLogout = () => {
  // 清除所有认证信息
  localStorage.removeItem('isAuthenticated')
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')

  // 重定向到登录页面
  router.push('/login')
}
</script>

<style scoped>
/* 导航容器，设置基本高度和宽度 */
.nav-container {
  height: 60px;
  width: 100%;
}

/* 导航栏主体样式 */
.navbar {
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  height: 60px; /* 固定高度更稳定 */
}

/* 状态栏布局，确保左右两侧内容分布 */
.status-bar {
  display: flex;
  justify-content: space-between; /* 左右两端对齐 */
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 24px; /* 增加左右内边距，避免内容贴边 */
}

/* 左侧区域样式 */
.left-section {
  display: flex;
  align-items: center;
}

/* 左侧导航区域 */
.nav-left {
  display: flex;
  align-items: center;
  gap: 16px; /* 增加logo和导航链接之间的间距 */
}

/* Logo链接样式 */
.nav-logo {
  display: flex;
  align-items: center;
  margin-right: 16px; /* 增加logo与导航链接的间距 */
}

/* Logo图片样式 */
.logo-image {
  height: 36px; /* 稍微增大logo尺寸 */
  width: auto;
}

/* 导航链接列表 */
.nav-links {
  list-style: none;
  display: flex;
  gap: 32px; /* 增加导航链接之间的间距 */
  margin: 0;
  padding: 0;
}

/* 导航链接样式 */
.nav-links li a {
  text-decoration: none;
  color: #333;
  font-size: 15px;
  transition: all 0.3s;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  position: relative;
  border: 1px solid transparent;
}

/* 导航链接悬停和激活状态 */
.nav-links li a:hover {
  color: #4f46e5;
}

.nav-links li a.router-link-active {
  color: #4f46e5;
  border-color: #4f46e5;
  font-weight: 600;
  background-color: transparent;
}

/* 移除下方指示线 */
.nav-link::before {
  display: none;
}

/* 右侧区域样式 */
.right-section {
  display: flex;
  align-items: center;
  gap: 20px; /* 增加右侧元素之间的间距 */
}

/* 通知徽章样式 */
.notification-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
}

/* 通知按钮样式 */
.notification-btn {
  padding: 8px;
}

/* 语言下拉菜单和用户下拉菜单样式 */
.language-dropdown,
.user-dropdown {
  cursor: pointer;
  margin-left: 4px; /* 增加下拉菜单之间的间距 */
}

/* 下拉菜单内容样式 */
.el-dropdown-menu {
  min-width: 120px; /* 增加下拉菜单宽度 */
}

/* 自定义下拉菜单样式 */
:global(.custom-dropdown-menu) {
  border-radius: 16px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
  overflow: hidden;
  padding: 4px 0 !important;
}

:global(.custom-dropdown-menu .el-dropdown-menu__item) {
  padding: 8px 20px !important;
  border-radius: 8px !important;
  transition: all 0.2s;
  font-size: 13px !important;
  line-height: 1.4 !important;
  margin: 2px 4px !important;
  width: calc(100% - 8px) !important;
}

:global(.custom-dropdown-menu .el-dropdown-menu__item:hover) {
  background-color: #f5f7fa !important;
}

:global(.custom-dropdown-menu .el-dropdown-menu__item.is-disabled) {
  color: #c0c4cc !important;
}
</style>
