<template>
  <div class="change-password-page">
    <div class="password-box">
      <h2>修改密码</h2>
      <p>为了账号安全，请定期修改密码</p>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>原密码</label>
          <input v-model="form.oldPassword" type="password" placeholder="请输入原密码" required>
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input v-model="form.newPassword" type="password" placeholder="6-20位字符" required>
          <div class="password-tips">
            <span :class="{ active: form.newPassword.length >= 6 }">✓ 至少6位</span>
            <span :class="{ active: /[A-Z]/.test(form.newPassword) }">✓ 大写字母</span>
            <span :class="{ active: /[0-9]/.test(form.newPassword) }">✓ 数字</span>
          </div>
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" required>
        </div>

        <button type="submit" :disabled="submitting">
          {{ submitting ? '修改中...' : '确认修改' }}
        </button>
      </form>

      <router-link to="/dashboard" class="back-link">返回主页</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useUserStore } from '@/stores/user'

const { toast } = useMessage()
const router = useRouter()
const userStore = useUserStore()

const submitting = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

async function handleSubmit() {
  if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    toast.warning('密码长度为6-20位')
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    toast.warning('两次密码不一致')
    return
  }

  if (form.oldPassword === form.newPassword) {
    toast.warning('新密码不能与原密码相同')
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/auth/change-password', null, {
      params: {
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      }
    })

    if (res.data.code === 200) {
      toast.success('密码修改成功，请重新登录')
      // 清除登录状态
      userStore.logout()
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      toast.error(res.data.msg || '修改失败')
    }
  } catch (e) {
    toast.error('网络错误：' + (e.response?.data?.msg || e.message))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.change-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.password-box {
  background: white;
  padding: 40px;
  border-radius: 16px;
  width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.password-box h2 {
  margin-bottom: 10px;
  color: #333;
  text-align: center;
}

.password-box p {
  color: #666;
  margin-bottom: 30px;
  text-align: center;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.password-tips {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
}

.password-tips span {
  color: #999;
  transition: color 0.3s;
}

.password-tips span.active {
  color: #43e97b;
  font-weight: 600;
}

button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-link {
  display: block;
  text-align: center;
  margin-top: 25px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #764ba2;
}

@media (max-width: 500px) {
  .password-box {
    width: 100%;
    padding: 30px 20px;
  }

  .password-tips {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
