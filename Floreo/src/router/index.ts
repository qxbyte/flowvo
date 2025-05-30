import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import DocumentView from '../views/DocumentView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserSettingsView from '../views/UserSettingsView.vue' // Import the new view
import ServiceHomeview from '../views/pages/ServiceHomeview.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/documents',
      name: 'documents',
      component: DocumentView,
      meta: {
        requiresAuth: true,
        }
    },
    {
      path: '/service',
      name: 'service',
      component: ServiceHomeview,
      meta: { requiresAuth: true }
    },
    {
      path: '/settings', // Define the path for user settings
      name: 'settings',
      component: UserSettingsView,
      meta: { requiresAuth: true } // Assuming settings page requires authentication
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫检查: 目标路径', to.path)
  
  // 检查是否存在有效token
  const token = localStorage.getItem('token')
  const isAuthenticated = token && localStorage.getItem('isAuthenticated') === 'true'
  
  console.log('认证状态:', isAuthenticated ? '已登录' : '未登录')

  // 需要鉴权的路由，但没有有效的token
  if (to.meta.requiresAuth && !isAuthenticated) {
    console.log('拦截未授权访问，重定向到登录页面')
    
    // 清除无效的认证信息
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    
    // 重定向到登录页面，并记录原始目标路由
    next({ 
      path: '/login', 
      query: { redirect: to.fullPath } 
    })
  } else {
    next()
  }
})

export default router
