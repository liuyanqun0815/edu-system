<template>
  <div class="login-page">
    <ul class="bg-animation">
      <li v-for="i in 10" :key="i"></li>
    </ul>
    
    <div class="container">
      <div class="brand-section">
        <div class="brand-logo">
          <span>🎓</span>
          {{ systemName }}
        </div>
        <h1 class="brand-title">{{ systemName }}</h1>
        <p class="brand-subtitle">
          {{ systemDesc }}
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">📚</div>
            <div class="feature-text">课程管理</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📝</div>
            <div class="feature-text">在线考试</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">数据分析</div>
          </div>
        </div>
      </div>

      <div class="login-section">
        <div class="login-box">
          <div class="login-header">
            <h2>欢迎登录</h2>
            <p>请使用您的账号密码登录系统</p>
          </div>

          <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <div class="input-wrapper">
                <span class="input-icon">👤</span>
                <input 
                  v-model="form.username" 
                  type="text" 
                  class="form-input" 
                  placeholder="请输入用户名" 
                  required
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">密码</label>
              <div class="input-wrapper">
                <span class="input-icon">🔒</span>
                <input 
                  v-model="form.password" 
                  type="password" 
                  class="form-input" 
                  placeholder="请输入密码" 
                  required
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">验证码</label>
              <div class="captcha-row">
                <div class="input-wrapper" style="flex:1">
                  <span class="input-icon">🔍</span>
                  <input 
                    v-model="form.captchaCode" 
                    type="text" 
                    class="form-input" 
                    placeholder="请输入验证码" 
                    maxlength="4"
                  >
                </div>
                <img 
                  v-if="captchaImg"
                  :src="captchaImg" 
                  class="captcha-img" 
                  alt="验证码" 
                  title="点击刷新" 
                  @click="refreshCaptcha"
                >
                <div v-else class="captcha-placeholder" @click="refreshCaptcha">
                  点击获取验证码
                </div>
              </div>
            </div>

            <div class="remember-forgot">
              <label class="remember-me">
                <input v-model="form.rememberMe" type="checkbox">
                <span>记住我</span>
              </label>
              <router-link to="/forgot" class="forgot-link">忘记密码？</router-link>
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </button>
          </form>

          <div class="register-link">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  rememberMe: false
})

const captchaKey = ref('')
const captchaImg = ref('')
const errorMsg = ref('')
const loading = ref(false)
const systemName = ref('智慧教培管理系统')
const systemDesc = ref('集课程管理、在线学习、考试测评、数据分析于一体的综合性教育平台')

// 加载系统设置
async function loadSettings() {
  try {
    const res = await request.get('/system/setting/all')
    if (res.data.code === 200 && res.data.data?.basic) {
      if (res.data.data.basic.systemName) {
        systemName.value = res.data.data.basic.systemName
      }
      if (res.data.data.basic.systemDesc) {
        systemDesc.value = res.data.data.basic.systemDesc
      }
    }
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  loadSettings()
  const saved = localStorage.getItem('rememberMe')
  if (saved) {
    try {
      const info = JSON.parse(saved)
      form.username = info.username || ''
      form.password = info.password || ''
      form.rememberMe = true
    } catch (e) {
      localStorage.removeItem('rememberMe')
    }
  }
  refreshCaptcha()
})

async function refreshCaptcha() {
  try {
    const res = await request.get('/auth/captcha')
    if (res.data.code === 200) {
      captchaKey.value = res.data.data.captchaKey
      captchaImg.value = res.data.data.captchaImg
      form.captchaCode = ''
    } else {
      captchaImg.value = ''
    }
  } catch (e) {
    console.error('验证码加载失败', e)
    captchaImg.value = ''
  }
}

async function handleLogin() {
  if (!form.captchaCode) {
    errorMsg.value = '请输入验证码'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    const res = await request.post('/auth/login', {
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })

    if (res.data.code === 200) {
      if (form.rememberMe) {
        localStorage.setItem('rememberMe', JSON.stringify({ 
          username: form.username, 
          password: form.password 
        }))
      } else {
        localStorage.removeItem('rememberMe')
      }

      userStore.setAuth(res.data.data)
      
      // 获取菜单权限
      try {
        const menuRes = await request.get(`/system/user/${res.data.data.userId}/menus/final`)
        if (menuRes.data.code === 200 && menuRes.data.data) {
          userStore.setMenuIds(menuRes.data.data)
        }
      } catch (e) {
        console.warn('获取菜单权限失败', e)
      }

      router.push('/dashboard')
    } else {
      errorMsg.value = res.data.msg || '登录失败'
      refreshCaptcha()
    }
  } catch (error) {
    errorMsg.value = '网络错误，请稍后重试'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.bg-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  list-style: none;
}

.bg-animation li {
  position: absolute;
  display: block;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  animation: animate 25s linear infinite;
  bottom: -150px;
  border-radius: 50%;
}

.bg-animation li:nth-child(1) { left: 25%; width: 80px; height: 80px; animation-delay: 0s; }
.bg-animation li:nth-child(2) { left: 10%; width: 20px; height: 20px; animation-delay: 2s; animation-duration: 12s; }
.bg-animation li:nth-child(3) { left: 70%; width: 20px; height: 20px; animation-delay: 4s; }
.bg-animation li:nth-child(4) { left: 40%; width: 60px; height: 60px; animation-delay: 0s; animation-duration: 18s; }
.bg-animation li:nth-child(5) { left: 65%; width: 20px; height: 20px; animation-delay: 0s; }
.bg-animation li:nth-child(6) { left: 75%; width: 110px; height: 110px; animation-delay: 3s; }
.bg-animation li:nth-child(7) { left: 35%; width: 150px; height: 150px; animation-delay: 7s; }
.bg-animation li:nth-child(8) { left: 50%; width: 25px; height: 25px; animation-delay: 15s; animation-duration: 45s; }
.bg-animation li:nth-child(9) { left: 20%; width: 15px; height: 15px; animation-delay: 2s; animation-duration: 35s; }
.bg-animation li:nth-child(10) { left: 85%; width: 150px; height: 150px; animation-delay: 0s; animation-duration: 11s; }

@keyframes animate {
  0% { transform: translateY(0) rotate(0deg); opacity: 1; border-radius: 0; }
  100% { transform: translateY(-1000px) rotate(720deg); opacity: 0; border-radius: 50%; }
}

.container {
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: auto;
  z-index: 1;
  padding: 20px;
}

.brand-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
  padding: 40px;
}

.brand-logo {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  line-height: 1.8;
  max-width: 500px;
}

.brand-features {
  margin-top: 40px;
  display: flex;
  gap: 30px;
}

.feature-item {
  text-align: center;
}

.feature-icon {
  width: 50px;
  height: 50px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 24px;
}

.feature-text {
  font-size: 14px;
  opacity: 0.9;
}

.login-section {
  width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.error-message {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  outline: none;
}

.form-input:focus {
  border-color: #667eea;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 18px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e8e8e8;
  cursor: pointer;
  object-fit: cover;
  transition: border-color 0.3s;
}

.captcha-img:hover {
  border-color: #667eea;
}

.captcha-placeholder {
  width: 120px;
  height: 44px;
  border-radius: 10px;
  border: 2px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  background: #f5f5f5;
}

.captcha-placeholder:hover {
  border-color: #667eea;
  color: #667eea;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #764ba2;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 25px;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

@media (max-width: 900px) {
  .brand-section {
    display: none;
  }
  .login-section {
    width: 100%;
  }
}
</style>
