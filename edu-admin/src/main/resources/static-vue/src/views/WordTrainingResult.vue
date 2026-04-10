<template>
  <div class="training-result-page">
    <div class="result-container">
      <div class="result-header">
        <h1 class="result-title">🎉 训练完成</h1>
      </div>

      <!-- 成绩展示 -->
      <div class="score-display">
        <div class="score-circle" :class="scoreClass">
          <div class="score-value">{{ result.accuracy?.toFixed(1) || 0 }}%</div>
          <div class="score-label">正确率</div>
        </div>
      </div>

      <!-- 详细统计 -->
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon">📝</div>
          <div class="stat-value">{{ result.questionCount }}</div>
          <div class="stat-label">总题数</div>
        </div>
        <div class="stat-item stat-correct">
          <div class="stat-icon">✅</div>
          <div class="stat-value">{{ result.correctCount }}</div>
          <div class="stat-label">正确</div>
        </div>
        <div class="stat-item stat-wrong">
          <div class="stat-icon">❌</div>
          <div class="stat-value">{{ result.wrongCount }}</div>
          <div class="stat-label">错误</div>
        </div>
        <div class="stat-item stat-skip">
          <div class="stat-icon">⏭️</div>
          <div class="stat-value">{{ result.skipCount }}</div>
          <div class="stat-label">跳过</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">⭐</div>
          <div class="stat-value">{{ result.totalScore }}</div>
          <div class="stat-label">得分</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">⏱️</div>
          <div class="stat-value">{{ formatDuration(result.actualDuration) }}</div>
          <div class="stat-label">用时</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <button class="btn btn-primary" @click="reviewWrong" v-if="result.wrongCount > 0">
          📚 复习错题
        </button>
        <button class="btn btn-info" @click="exportWrong" v-if="result.wrongCount > 0">
          📥 导出错题
        </button>
        <button class="btn btn-success" @click="startAgain">
          🔄 再次训练
        </button>
        <button class="btn btn-default" @click="goHome">
          🏠 返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const route = useRoute()
const router = useRouter()
const { toast } = useMessage()

const sessionId = ref(route.params.sessionId)
const result = ref({})

const scoreClass = computed(() => {
  const accuracy = result.value.accuracy || 0
  if (accuracy >= 90) return 'excellent'
  if (accuracy >= 70) return 'good'
  if (accuracy >= 60) return 'pass'
  return 'fail'
})

function formatDuration(seconds) {
  if (!seconds) return '0秒'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  if (m > 0) {
    return `${m}分${s}秒`
  }
  return `${s}秒`
}

async function loadResult() {
  try {
    const res = await request.post(`/word/training/${sessionId.value}/submit`)
    if (res.data.code === 200) {
      result.value = res.data.data
    } else {
      toast.error(res.data.msg || '加载结果失败')
    }
  } catch (e) {
    toast.error('加载结果失败')
  }
}

function reviewWrong() {
  router.push({ name: 'word-wrongbook' })
}

function exportWrong() {
  window.open('/word/wrongbook/export', '_blank')
}

function startAgain() {
  router.push({ name: 'word-training' })
}

function goHome() {
  router.push({ name: 'word' })
}

onMounted(() => {
  loadResult()
})
</script>

<style scoped>
.training-result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.result-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}

.result-header {
  text-align: center;
  margin-bottom: 30px;
}

.result-title {
  font-size: 32px;
  color: #333;
  margin: 0;
}

.score-display {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.score-circle {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 8px solid;
  transition: all 0.3s;
}

.score-circle.excellent {
  border-color: #4CAF50;
  background: #E8F5E9;
}

.score-circle.good {
  border-color: #2196F3;
  background: #E3F2FD;
}

.score-circle.pass {
  border-color: #FF9800;
  background: #FFF3E0;
}

.score-circle.fail {
  border-color: #F44336;
  background: #FFEBEE;
}

.score-value {
  font-size: 42px;
  font-weight: 700;
  color: #333;
}

.score-label {
  font-size: 16px;
  color: #666;
  margin-top: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #4472C4;
  color: white;
}

.btn-primary:hover {
  background: #355da7;
}

.btn-info {
  background: #17A2B8;
  color: white;
}

.btn-info:hover {
  background: #138496;
}

.btn-success {
  background: #28A745;
  color: white;
}

.btn-success:hover {
  background: #218838;
}

.btn-default {
  background: #f0f0f0;
  color: #333;
}

.btn-default:hover {
  background: #e0e0e0;
}
</style>
