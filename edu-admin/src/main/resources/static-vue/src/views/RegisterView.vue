<template>
  <div class="register-page">
    <div class="register-box">
      <h2>用户注册</h2>
      <p>创建您的账号</p>
      <form @submit.prevent="handleRegister">
        <input v-model="form.username" type="text" placeholder="用户名" required>
        <input v-model="form.password" type="password" placeholder="密码" required>
        <input v-model="form.confirmPassword" type="password" placeholder="确认密码" required>
        <button type="submit">注册</button>
      </form>
      <router-link to="/login">已有账号？去登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast } = useMessage()
const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    toast.warning('两次密码不一致')
    return
  }
  try {
    const res = await request.post('/auth/register', {
      username: form.username,
      password: form.password
    })
    if (res.data.code === 200) {
      toast.success('注册成功')
      router.push('/login')
    } else {
      toast.error(res.data.msg || '注册失败')
    }
  } catch (e) {
    toast.error('网络错误')
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  width: 400px;
  text-align: center;
}

.register-box h2 {
  margin-bottom: 10px;
}

.register-box p {
  color: #999;
  margin-bottom: 30px;
}

.register-box input {
  width: 100%;
  padding: 12px;
  margin-bottom: 15px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.register-box button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
}

.register-box a {
  color: #667eea;
  text-decoration: none;
}
</style>
