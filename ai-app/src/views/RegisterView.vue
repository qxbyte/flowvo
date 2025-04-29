import NavBar from '@/components/NavBar.vue'

<template>
  <NavBar />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

const handleRegister = async () => {
  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }

  try {
    console.log('发送注册请求:', { username: username.value, email: email.value, password: password.value })

    const response = await fetch('/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        email: email.value,
        password: password.value
      })
    })

    const data = await response.json()
    console.log('注册响应:', data)

    if (response.ok) {
      alert('注册成功，请使用新账户登录')
      router.push('/login')
    } else {
      alert(data.message || '注册失败，请检查输入信息')
    }
  } catch (error) {
    console.error('注册错误:', error)
    alert('注册过程中发生错误')
  }
}
</script>

<style scoped>
.register-container {
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

.register-box {
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

.register-box h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}
</style>