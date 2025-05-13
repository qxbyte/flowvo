import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'
import router from './router'

// 导入Element Plus样式，页面信息提示需要
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/el-message.css'
import { ElMessage } from 'element-plus'

import App from './App.vue'

// 设置请求拦截器，自动添加token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 添加响应拦截器，处理401未授权错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // 清除无效的认证信息
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      
      // 重定向到登录页面
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }
    return Promise.reject(error)
  }
)

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 配置Element Plus的全局默认值
app.config.globalProperties.$ELEMENT = {
  // 消息提示的默认配置
  message: {
    plain: true
  }
}

app.mount('#app')
