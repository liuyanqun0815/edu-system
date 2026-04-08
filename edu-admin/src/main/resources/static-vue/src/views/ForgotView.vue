<template>
  <div class="forgot-page">
    <div class="forgot-box">
      <h2>找回密码</h2>
      
      <!-- 步骤1：输入用户名或邮箱 -->
      <div v-if="step === 1">
        <p>请输入您的用户名或邮箱</p>
        <form @submit.prevent="handleSendCode">
          <input v-model="form.usernameOrEmail" type="text" placeholder="用户名或邮箱" required>
          <button type="submit" :disabled="sending">{{ sending ? '发送中...' : '发送验证码' }}</button>
        </form>
      </div>

      <!-- 步骤2：输入验证码和新密码 -->
      <div v-if="step === 2">
        <p>验证码已发送至 <strong>{{ form.email }}</strong></p>
        <form @submit.prevent="handleReset">
          <input v-model="form.verifyCode" type="text" placeholder="请输入6位验证码" maxlength="6" required>
          <input v-model="form.newPassword" type="password" placeholder="新密码（6-20位）" required>
          <input v-model="form.confirmPassword" type="password" placeholder="确认新密码" required>
          <button type="submit" :disabled="resetting">{{ resetting ? '重置中...' : '重置密码' }}</button>
        </form>
        <div class="resend" @click="handleSendCode">
          {{ countdown > 0 ? `${countdown}秒后重发` : '重新发送验证码' }}
        </div>
      </div>

      <router-link to="/login" class="back-link">返回登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast } = useMessage()
const router = useRouter()

const step = ref(1)
const sending = ref(false)
const resetting = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({
  usernameOrEmail: '',
  email: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
})

async function handleSendCode() {
  if (!form.usernameOrEmail) {
    toast.warning('请输入用户名或邮箱')
    return
  }

  sending.value = true
  try {
    const res = await request.post('/auth/forgot/send-code', {
      usernameOrEmail: form.usernameOrEmail
    })
    
    if (res.data.code === 200) {
      toast.success('验证码已发送')
      step.value = 2
      
      // 查找用户邮箱（从后端响应中获取）
      form.email = form.usernameOrEmail.includes('@') ? form.usernameOrEmail : ''
      
      // 启动倒计时
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      toast.error(res.data.msg || '发送失败')
    }
  } catch (e) {
    toast.error('网络错误：' + (e.response?.data?.msg || e.message))
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  if (!form.verifyCode || form.verifyCode.length !== 6) {
    toast.warning('请输入6位验证码')
    return
  }

  if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    toast.warning('密码长度为6-20位')
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    toast.warning('两次密码不一致')
    return
  }

  if (!form.email) {
    toast.warning('无法获取邮箱，请重新操作')
    step.value = 1
    return
  }

  resetting.value = true
  try {
    const res = await request.post('/auth/forgot/reset', {
      email: form.email,
      verifyCode: form.verifyCode,
      newPassword: form.newPassword
    })

    if (res.data.code === 200) {
      toast.success('密码重置成功')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      toast.error(res.data.msg || '重置失败')
    }
  } catch (e) {
    toast.error('网络错误：' + (e.response?.data?.msg || e.message))
  } finally {
    resetting.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  width: 420px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.forgot-box h2 {
  margin-bottom: 10px;
  color: #333;
}

.forgot-box p {
  color: #666;
  margin-bottom: 25px;
}

.forgot-box strong {
  color: #667eea;
}

.forgot-box input {
  width: 100%;
  padding: 14px;
  margin-bottom: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.forgot-box input:focus {
  outline: none;
  border-color: #667eea;
}

.forgot-box button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.forgot-box button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.forgot-box button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.resend {
  margin-top: 15px;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.resend:hover {
  color: #764ba2;
}

.back-link {
  display: inline-block;
  margin-top: 25px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #764ba2;
}
</style>
