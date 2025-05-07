import NavBar from '@/components/NavBar.vue'

<template>
  <NavBar />
  <div class="home-layout">
    <div class="module-container">
      <div class="module-card" @click="$router.push('/documents')">
        <div class="module-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <h2>文档管理</h2>
        <p>上传和管理您的文档</p>
      </div>

      <div class="module-card" @click="router.push('/chat')">
        <div class="module-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
        </div>
        <h2>AI 聊天</h2>
        <p>开始智能对话</p>
      </div>

      <div class="module-card" @click="router.push('/service')">
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

const router = useRouter()
const showDocumentModal = ref(false)

</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  background-color: #ffffff;
  padding-top: 60px; /* 为固定导航栏留出空间 */
}

.module-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  padding: 2rem;
  margin-top: 20vh; /* 增加顶部的margin值以实现向下移动的效果 */
}

.module-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  min-width: 300px;
  max-width: 300px;
  max-height: 180px;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.module-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  color: #4f46e5;
}

.module-card h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #111827;
}

.module-card p {
  color: #6b7280;
  font-size: 1rem;
}
</style>

