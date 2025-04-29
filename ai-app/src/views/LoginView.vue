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
      <div class="register-link">
        <a @click.prevent="navigateToRegister">注册</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')

const handleLogin = async () => {
  try {
    console.log('发送登录请求:', { username: username.value, password: password.value })

    const response = await fetch('/api/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })

    const data = await response.json()
    console.log('登录响应:', data)

    if (response.ok) {
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
  width: 100vw;         /* 确保宽度填满整个视口 */
  height: 100vh;        /* 高度填满视口 */
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
  margin: 0 auto; /* 确保容器居中 */
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
  border-radius: 0.25rem;
}

.login-box button {
  width: 100%;
  padding: 0.75rem;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
}

.login-box button:hover {
  background-color: #059669;
}

.login-box .register-link {
  text-align: center;
  margin-top: 1rem;
  color: #6b7280;
}

.login-box .register-link a {
  color: #4f46e5;
  text-decoration: none;
}

.login-box .register-link a:hover {
  text-decoration: underline;
}
</style>

<NavBar />
