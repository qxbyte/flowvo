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
    console.log('发送登录请求:', { username: username.value, password: password.value })

    const response = await axios.post('/api/user/login', {
      username: username.value,
      password: password.value
    })

    const data = response.data
    console.log('登录响应:', data)

    if (response.status === 200) {
      localStorage.setItem('isAuthenticated', 'true')
      router.push('/')
    } else {
      alert(data.message || '登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('登录错误:', error)
    alert('登录过程中发生错误')
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
