import NavBar from '@/components/NavBar.vue'

<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名：</label>
          <input type="text" v-model="username" required>
        </div>
        <div class="form-group">
          <label>密码：</label>
          <input type="password" v-model="password" required>
        </div>
        <button type="submit">登录</button>
      </form>
      <div class="divider"></div>
      <div class="register-link">
        <p class="register-hint">还没有账号？</p>
        <a @click.prevent="navigateToRegister" class="register-button">注册</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')

const handleLogin = async () => {
  try {
    console.log('开始登录处理，用户名:', username.value)

    // 清除之前可能存在的认证数据
    localStorage.removeItem('token')
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')

    console.log('发送登录请求...')
    const response = await axios.post('/api/user/login', {
      username: username.value,
      password: password.value
    })

    console.log('收到登录响应，状态码:', response.status)
    const data = response.data
    
    if (response.status === 200 && data.token) {
      console.log('登录成功，存储认证信息...')
      
      // 保存token和用户信息
      localStorage.setItem('isAuthenticated', 'true')
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      
      console.log('保存的令牌:', data.token.substring(0, 20) + '...' + data.token.substring(data.token.length - 20))
      
      // 检查是否有重定向参数
      const redirectPath = router.currentRoute.value.query.redirect as string
      console.log('重定向路径:', redirectPath || '/')
      
      router.push(redirectPath || '/')
    } else {
      console.error('登录响应无效:', data)
      alert(data.message || '登录失败，请检查用户名和密码')
    }
  } catch (error: any) {
    console.error('登录错误:', error)
    
    let errorMessage = '登录失败，请检查用户名和密码'
    
    if (error.response) {
      console.error('服务器响应错误:', error.response.status, error.response.data)
      
      // 尝试提取更有用的错误信息
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data
      } else if (error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message
      }
    } else if (error.request) {
      console.error('无服务器响应:', error.request)
      errorMessage = '服务器没有响应，请检查网络连接'
    } else {
      console.error('请求配置错误:', error.message)
      errorMessage = `请求错误: ${error.message}`
    }
    
    alert(errorMessage)
  }
}

const navigateToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  position: fixed;
  top: 0;
  left: 0;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.login-box h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  color: #111827;
}

.login-box input {
  width: 100%;
  padding: 0.75rem;
  margin-bottom: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.login-box button {
  width: 100%;
  padding: 0.75rem;
  background-color: #1d4ed8;
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.login-box button:hover {
  background-color: #1e40af;
}

.divider {
  width: 100%;
  height: 1px;
  background-color: #e5e7eb;
  margin: 1.5rem 0;
}

.login-box .register-link {
  width: 100%;
  text-align: center;
}

.login-box .register-link .register-hint {
  margin-bottom: 0.7rem;
  color: #6b7280;
  font-size: 0.8rem;
}

.login-box .register-link .register-button {
  display: inline-block;
  width: auto;
  padding: 0.18rem 1.5rem;
  background-color: white;
  color: #1d4ed8;
  border: 1px solid #1d4ed8;
  border-radius: 0.25rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.7rem;
}

.login-box .register-link .register-button:hover {
  background-color: #1d4ed8;
  color: white;
  text-decoration: none;
}
</style>

<NavBar />
