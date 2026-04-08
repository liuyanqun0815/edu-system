<template>
  <div class="setting-page">
    <h1 class="page-title">系统设置</h1>

    <!-- 基本信息 -->
    <div class="setting-card">
      <h3 class="setting-title">🏠 基本信息</h3>
      <div class="form-group">
        <label class="form-label">系统名称</label>
        <input v-model="settings.systemName" type="text" class="form-input" placeholder="请输入系统名称">
      </div>
      <div class="form-group">
        <label class="form-label">系统描述</label>
        <textarea v-model="settings.systemDesc" class="form-textarea" rows="3" placeholder="请输入系统描述"></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">联系邮箱</label>
        <input v-model="settings.contactEmail" type="email" class="form-input" placeholder="管理员联系邮箱">
      </div>
      <div class="form-group">
        <label class="form-label">版权信息</label>
        <input v-model="settings.copyright" type="text" class="form-input" placeholder="如：© 2024 智慧教培管理系统">
      </div>
      <button class="btn btn-primary" :disabled="saving.basic" @click="saveBasic">
        {{ saving.basic ? '保存中...' : '保存设置' }}
      </button>
    </div>

    <!-- 安全设置 -->
    <div class="setting-card">
      <h3 class="setting-title">🔐 安全设置</h3>
      <div class="form-row">
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">登录密码有效期（天）</label>
            <input v-model.number="settings.pwdExpire" type="number" class="form-input" min="0">
            <div class="form-hint">设置为 0 则永不过期</div>
          </div>
        </div>
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">登录失败锁定次数</label>
            <input v-model.number="settings.lockCount" type="number" class="form-input" min="1">
            <div class="form-hint">连续失败达到此次数后锁定账号</div>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">Token 有效期（小时）</label>
            <input v-model.number="settings.tokenExpire" type="number" class="form-input" min="1">
          </div>
        </div>
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">锁定时长（分钟）</label>
            <input v-model.number="settings.lockDuration" type="number" class="form-input" min="1">
          </div>
        </div>
      </div>
      <button class="btn btn-primary" :disabled="saving.security" @click="saveSecurity">
        {{ saving.security ? '保存中...' : '保存设置' }}
      </button>
    </div>

    <!-- 通知设置 -->
    <div class="setting-card">
      <h3 class="setting-title">📧 通知设置</h3>
      <div class="form-group">
        <label class="form-label">发件人邮箱</label>
        <input v-model="settings.smtpFrom" type="email" class="form-input" placeholder="发件人邮箱地址">
      </div>
      <div class="form-group">
        <label class="form-label">SMTP 服务器</label>
        <input v-model="settings.smtpHost" type="text" class="form-input" placeholder="如：smtp.qq.com">
      </div>
      <div class="form-row">
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">SMTP 端口</label>
            <input v-model.number="settings.smtpPort" type="number" class="form-input" placeholder="465 或 587">
          </div>
        </div>
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">SMTP 授权码</label>
            <input v-model="settings.smtpPassword" type="password" class="form-input" placeholder="邮箱授权码">
          </div>
        </div>
      </div>
      <div class="switch-row">
        <label class="form-label">开启邮件通知</label>
        <div class="switch-wrap">
          <label class="switch">
            <input type="checkbox" v-model="settings.emailEnabled">
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ settings.emailEnabled ? '已开启' : '已关闭' }}</span>
        </div>
      </div>
      <div class="switch-row">
        <label class="form-label">注册成功通知</label>
        <div class="switch-wrap">
          <label class="switch">
            <input type="checkbox" v-model="settings.registerNotify">
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ settings.registerNotify ? '已开启' : '已关闭' }}</span>
        </div>
      </div>
      <button class="btn btn-primary" :disabled="saving.notify" @click="saveNotify">
        {{ saving.notify ? '保存中...' : '保存设置' }}
      </button>
    </div>

    <!-- 学习设置 -->
    <div class="setting-card">
      <h3 class="setting-title">📚 学习设置</h3>
      <div class="form-row">
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">课程有效期（天）</label>
            <input v-model.number="settings.courseExpire" type="number" class="form-input" min="0">
            <div class="form-hint">0 表示永久有效</div>
          </div>
        </div>
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">考试及格分数线（%）</label>
            <input v-model.number="settings.passScore" type="number" class="form-input" min="0" max="100">
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">优秀分数线（%）</label>
            <input v-model.number="settings.excellentScore" type="number" class="form-input" min="0" max="100">
          </div>
        </div>
        <div class="form-col">
          <div class="form-group">
            <label class="form-label">每日学习目标（分钟）</label>
            <input v-model.number="settings.dailyTarget" type="number" class="form-input" min="0">
          </div>
        </div>
      </div>
      <div class="switch-row">
        <label class="form-label">允许重考</label>
        <div class="switch-wrap">
          <label class="switch">
            <input type="checkbox" v-model="settings.allowRetake">
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ settings.allowRetake ? '允许' : '不允许' }}</span>
        </div>
      </div>
      <div class="switch-row">
        <label class="form-label">开放自主报名</label>
        <div class="switch-wrap">
          <label class="switch">
            <input type="checkbox" v-model="settings.openEnroll">
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ settings.openEnroll ? '已开放' : '已关闭' }}</span>
        </div>
      </div>
      <button class="btn btn-primary" :disabled="saving.study" @click="saveStudy">
        {{ saving.study ? '保存中...' : '保存设置' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast } = useMessage()

const settings = reactive({
  // 基本
  systemName: '智慧教培管理系统',
  systemDesc: '专业的在线教育管理平台，提供课程管理、考试管理、学员管理等一站式解决方案。',
  contactEmail: '',
  copyright: '© 2024 智慧教培管理系统',
  // 安全
  pwdExpire: 90,
  lockCount: 5,
  tokenExpire: 24,
  lockDuration: 30,
  // 通知
  smtpFrom: '',
  smtpHost: '',
  smtpPort: 465,
  smtpPassword: '',
  emailEnabled: false,
  registerNotify: false,
  // 学习
  courseExpire: 365,
  passScore: 60,
  excellentScore: 90,
  dailyTarget: 30,
  allowRetake: true,
  openEnroll: false
})

const saving = reactive({ basic: false, security: false, notify: false, study: false })

async function loadSettings() {
  try {
    const res = await request.get('/system/setting/all')
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      const merge = (group) => {
        if (group) Object.keys(group).forEach(k => {
          if (k in settings) settings[k] = group[k]
        })
      }
      merge(data.basic)
      merge(data.security)
      merge(data.notify || data.notification)
      merge(data.study || data.learning)
    }
  } catch (e) {
    console.warn('加载系统设置失败', e)
  }
}

async function saveGroup(group, payload, key) {
  saving[key] = true
  try {
    const res = await request.post(`/system/setting/group/${group}`, payload)
    if (res.data.code === 200) {
      toast.success('保存成功')
    } else {
      toast.error(res.data.msg || '保存失败')
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '保存失败')
  } finally {
    saving[key] = false
  }
}

function saveBasic() {
  saveGroup('basic', {
    systemName: settings.systemName,
    systemDesc: settings.systemDesc,
    contactEmail: settings.contactEmail,
    copyright: settings.copyright
  }, 'basic')
}

function saveSecurity() {
  saveGroup('security', {
    pwdExpire: String(settings.pwdExpire),
    lockCount: String(settings.lockCount),
    tokenExpire: String(settings.tokenExpire),
    lockDuration: String(settings.lockDuration)
  }, 'security')
}

function saveNotify() {
  saveGroup('notify', {
    smtpFrom: settings.smtpFrom,
    smtpHost: settings.smtpHost,
    smtpPort: String(settings.smtpPort),
    smtpPassword: settings.smtpPassword,
    emailEnabled: String(settings.emailEnabled),
    registerNotify: String(settings.registerNotify)
  }, 'notify')
}

function saveStudy() {
  saveGroup('study', {
    courseExpire: String(settings.courseExpire),
    passScore: String(settings.passScore),
    excellentScore: String(settings.excellentScore),
    dailyTarget: String(settings.dailyTarget),
    allowRetake: String(settings.allowRetake),
    openEnroll: String(settings.openEnroll)
  }, 'study')
}

onMounted(loadSettings)
</script>

<style scoped>
.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 25px;
}

.setting-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.setting-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 25px 0;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
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

.form-hint {
  font-size: 12px;
  color: #aaa;
  margin-top: 5px;
}

.form-input, .form-textarea {
  width: 100%;
  max-width: 420px;
  padding: 10px 15px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus, .form-textarea:focus {
  border-color: #667eea;
}

.form-textarea {
  max-width: 100%;
  min-height: 80px;
  resize: vertical;
}

.form-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.form-col {
  flex: 1;
  min-width: 200px;
}

/* 开关行 */
.switch-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 18px;
}

.switch-row .form-label {
  margin: 0;
  min-width: 140px;
}

.switch-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background: #ccc;
  border-radius: 24px;
  transition: 0.3s;
}

.slider::before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

input:checked + .slider {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

input:checked + .slider::before {
  transform: translateX(20px);
}

.switch-label {
  font-size: 13px;
  color: #666;
}

.btn {
  padding: 10px 30px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  margin-top: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102,126,234,0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
