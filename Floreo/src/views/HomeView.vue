import NavBar from '@/components/NavBar.vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DocumentModal from '@/components/document/DocumentModal.vue'

<template>
  <NavBar />
  <div class="home-layout">
    <div class="module-container">
      <div class="module-card" @click="$router.push('/documents')" @mousemove="handleMouseMove">
        <div class="module-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <h2>文档管理</h2>
        <p>上传和管理您的文档</p>
      </div>

      <div class="module-card" @click="router.push('/chat')" @mousemove="handleMouseMove">
        <div class="module-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
        </div>
        <h2>AI 聊天</h2>
        <p>开始智能对话</p>
      </div>

      <div class="module-card" @click="router.push('/service')" @mousemove="handleMouseMove">
        <div class="module-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
        </div>
        <h2>业务系统</h2>
        <p>企业业务管理平台</p>
      </div>
    </div>

    <!-- 文档上传模态框 -->
    <DocumentModal v-if="showDocumentModal" @close="showDocumentModal = false" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import DocumentModal from '@/components/document/DocumentModal.vue'
import { onMounted } from 'vue'

const router = useRouter()
const showDocumentModal = ref(false)

// 在组件挂载时进行用户认证检查
onMounted(() => {
  console.log('HomeView 组件已挂载，检查用户认证状态')
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
  } else {
    console.log('用户已登录，可以访问首页')
    const username = localStorage.getItem('username')
    console.log('当前登录用户:', username)
  }
})

// 处理鼠标移动事件，更新边框效果位置
const handleMouseMove = (event: MouseEvent) => {
  const target = event.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  
  target.style.setProperty('--mouse-x', `${x}px`)
  target.style.setProperty('--mouse-y', `${y}px`)
}
</script>

<style scoped>
.home-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  background-color: #f5f5f5;
  padding-top: 60px;
  padding-bottom: 40px;
}

.module-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  padding: 2rem;
  margin-top: 10vh;
  justify-content: center;
}

.module-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-height: 200px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  --mouse-x: 0px;
  --mouse-y: 0px;
}

/* 卡片悬停效果 */
.module-card:hover {
  transform: translateY(-3px);
}

/* 卡片边框效果：使用伪元素实现 */
.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 0.75rem;
  border: 2px solid black;
  opacity: 0;
  z-index: 10;
  pointer-events: none;
  transition: opacity 0.1s ease;
  clip-path: circle(70px at var(--mouse-x) var(--mouse-y));
}

/* 创建渐变遮罩层，让边框两端淡化 */
.module-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 0.75rem;
  background: radial-gradient(
    circle at var(--mouse-x) var(--mouse-y),
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0) 40%,
    rgba(255, 255, 255, 0.8) 80%,
    rgba(255, 255, 255, 1) 100%
  );
  opacity: 0;
  z-index: 11;
  pointer-events: none;
  transition: opacity 0.1s ease;
  clip-path: circle(70px at var(--mouse-x) var(--mouse-y));
}

.module-card:hover::before {
  opacity: 1;
}

.module-card:hover::after {
  opacity: 1;
}

.module-icon {
  width: 36px;
  height: 36px;
  margin-bottom: 0.75rem;
  color: #6366f1;
  position: relative;
  z-index: 2;
}

.module-card h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: #111827;
  position: relative;
  z-index: 2;
}

.module-card p {
  color: #6b7280;
  font-size: 0.875rem;
  position: relative;
  z-index: 2;
}

@media (max-width: 768px) {
  .module-container {
    grid-template-columns: repeat(1, 1fr);
  }
}
</style>

