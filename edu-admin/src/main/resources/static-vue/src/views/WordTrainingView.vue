<template>
  <div class="word-training-page">
    <div class="page-header">
      <h1 class="page-title">📚 单词AI训练</h1>
      <button class="btn btn-default" @click="goBack">← 返回单词管理</button>
    </div>

    <!-- 训练配置面板 -->
    <div class="config-panel">
      <h2 class="panel-title">⚙️ 训练配置</h2>
      <div class="config-grid">
        <div class="config-item">
          <label>选择年级</label>
          <select v-model="config.grade" class="config-select">
            <option value="">请选择年级</option>
            <option v-for="grade in gradeOptions" :key="grade.itemValue" :value="grade.itemName || grade.itemLabel">
              {{ grade.itemName || grade.itemLabel }}
            </option>
          </select>
        </div>

        <div class="config-item">
          <label>选择难度</label>
          <select v-model="config.difficulty" class="config-select">
            <option value="">请选择难度</option>
            <option :value="1">⭐ 简单</option>
            <option :value="2">⭐⭐ 中等</option>
            <option :value="3">⭐⭐⭐ 困难</option>
          </select>
        </div>

        <div class="config-item">
          <label>题目类型</label>
          <select v-model="config.questionType" class="config-select">
            <option value="">请选择题型</option>
            <option value="spell">📝 拼写题(看中文写英文)</option>
            <option value="choice_select">🔤 看词选义(看英文选中文)</option>
            <option value="choice_word">📖 看义选词(看中文选英文)</option>
            <option value="translate">🌐 翻译题(翻译单词)</option>
            <option value="sentence_fill">✍️ 例句填空(补全例句)</option>
          </select>
        </div>

        <div class="config-item">
          <label>题目数量</label>
          <input 
            v-model.number="config.questionCount" 
            type="number" 
            min="5" 
            max="100" 
            class="config-input"
            placeholder="5-100题"
          >
        </div>

        <div class="config-item">
          <label>每题时间(秒)</label>
          <input 
            v-model.number="config.perQuestionTime" 
            type="number" 
            min="10" 
            max="120" 
            class="config-input"
            placeholder="10-120秒"
          >
        </div>
      </div>

      <div class="config-actions">
        <button class="btn btn-primary btn-large" @click="startTraining" :disabled="!canStart">
          🚀 开始训练
        </button>
      </div>
    </div>

    <!-- 今日错题提示 -->
    <div v-if="todayWrongCount > 0" class="wrong-book-hint">
      <div class="hint-content">
        <span class="hint-icon">📌</span>
        <span class="hint-text">你今天有 <strong>{{ todayWrongCount }}</strong> 道错题需要复习</span>
      </div>
      <button class="btn btn-warning" @click="goToWrongBook">去复习错题</button>
    </div>

    <!-- 训练统计 -->
    <div class="training-stats">
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalSessions }}</div>
          <div class="stat-label">总训练次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ avgAccuracy }}%</div>
          <div class="stat-label">平均正确率</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalWords }}</div>
          <div class="stat-label">累计训练单词</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const { toast } = useMessage()
const configStore = useConfigStore()

const gradeOptions = computed(() => configStore.gradeList)

const config = reactive({
  grade: '',
  difficulty: null,
  questionType: '',
  questionCount: 20,
  perQuestionTime: 30
})

const todayWrongCount = ref(0)
const totalSessions = ref(0)
const avgAccuracy = ref(0)
const totalWords = ref(0)

const canStart = computed(() => {
  return config.grade && config.difficulty && config.questionType && config.questionCount
})

function goBack() {
  router.push({ name: 'word' })
}

async function startTraining() {
  try {
    const res = await request.post('/word/training/start', config)
    if (res.data.code === 200) {
      const sessionId = res.data.data.sessionId
      toast.success('训练开始!准备进入...')
      
      // 5秒倒计时后进入训练
      let countdown = 5
      const timer = setInterval(() => {
        countdown--
        if (countdown <= 0) {
          clearInterval(timer)
          router.push({ 
            name: 'word-training-session', 
            params: { sessionId },
            query: { config: JSON.stringify(config) }
          })
        }
      }, 1000)
    } else {
      toast.error(res.data.msg || '开始训练失败')
    }
  } catch (e) {
    toast.error('开始训练失败: ' + (e.response?.data?.msg || e.message))
  }
}

function goToWrongBook() {
  router.push({ name: 'word-wrongbook' })
}

async function loadStats() {
  try {
    // 查询今日错题
    const wrongRes = await request.get('/word/wrongbook/today')
    if (wrongRes.data.code === 200) {
      todayWrongCount.value = wrongRes.data.data?.length || 0
    }
    
    // TODO: 加载训练统计(需要后端补充接口)
    totalSessions.value = 0
    avgAccuracy.value = 0
    totalWords.value = 0
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

onMounted(() => {
  configStore.loadConfigs()
  loadStats()
})
</script>

<style scoped>
.word-training-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.config-panel {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}

.panel-title {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.config-item {
  display: flex;
  flex-direction: column;
}

.config-item label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.config-select,
.config-input {
  padding: 10px 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.config-select:focus,
.config-input:focus {
  outline: none;
  border-color: #4472C4;
}

.config-actions {
  display: flex;
  justify-content: center;
}

.btn-large {
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
}

.wrong-book-hint {
  background: #FFF3E0;
  border-left: 4px solid #FF9800;
  padding: 16px 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.hint-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hint-icon {
  font-size: 24px;
}

.hint-text {
  font-size: 15px;
  color: #333;
}

.training-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.stat-icon {
  font-size: 36px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #4472C4;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #355da7;
}

.btn-default {
  background: #f0f0f0;
  color: #333;
}

.btn-default:hover {
  background: #e0e0e0;
}

.btn-warning {
  background: #FF9800;
  color: white;
}

.btn-warning:hover {
  background: #F57C00;
}
</style>
