<template>
  <div class="exam-result-page">
    <div class="page-header">
      <h1 class="page-title">考试成绩详情</h1>
      <button class="btn btn-default" @click="goBack">返回列表</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="result.record" class="result-container">
      <!-- 成绩概览 -->
      <div class="score-overview">
        <div class="score-card">
          <div class="score-value">{{ result.record.score }}</div>
          <div class="score-label">总分</div>
        </div>
        <div class="score-card">
          <div class="score-value">{{ result.totalCount }}</div>
          <div class="score-label">总题数</div>
        </div>
        <div class="score-card correct">
          <div class="score-value">{{ result.correctCount }}</div>
          <div class="score-label">正确</div>
        </div>
        <div class="score-card wrong">
          <div class="score-value">{{ result.wrongCount }}</div>
          <div class="score-label">错误</div>
        </div>
        <div class="score-card">
          <div class="score-value">{{ accuracyRate }}%</div>
          <div class="score-label">正确率</div>
        </div>
      </div>

      <!-- 防作弊信息 -->
      <div v-if="result.record.screenSwitchCount > 0 || result.record.warningCount > 0" class="anti-cheat-section">
        <div class="anti-cheat-header">
          <h3>⚠️ 防作弊监控</h3>
          <span :class="['cheat-status', result.hasCheating ? 'danger' : 'warning']">
            {{ result.hasCheating ? '存在作弊行为' : '疑似违规' }}
          </span>
        </div>
        <div class="anti-cheat-stats">
          <div class="cheat-stat">
            <span class="cheat-label">浏览器:</span>
            <span class="cheat-value">{{ result.record.browserInfo || '未知' }}</span>
          </div>
          <div class="cheat-stat">
            <span class="cheat-label">切屏次数:</span>
            <span class="cheat-value" :class="{ 'danger': result.record.screenSwitchCount > 5 }">
              {{ result.record.screenSwitchCount || 0 }}
            </span>
          </div>
          <div class="cheat-stat">
            <span class="cheat-label">警告次数:</span>
            <span class="cheat-value" :class="{ 'danger': result.record.warningCount > 3 }">
              {{ result.record.warningCount || 0 }}
            </span>
          </div>
        </div>
      </div>

      <!-- 答题详情 -->
      <div class="questions-section">
        <h2 class="section-title">答题详情</h2>
        
        <div v-for="(item, index) in result.questions" :key="index" class="question-card">
          <div class="question-header">
            <span class="question-number">第 {{ index + 1 }} 题</span>
            <span :class="['question-status', item.detail.isCorrect === 1 ? 'correct' : 'wrong']">
              {{ item.detail.isCorrect === 1 ? '✓ 正确' : '✗ 错误' }}
            </span>
            <span class="question-score">得分: {{ item.detail.score }}</span>
          </div>

          <div class="question-content">
            <p class="question-text">{{ item.question.content }}</p>

            <!-- 选项 -->
            <div v-if="item.options && item.options.length > 0" class="options-list">
              <div v-for="opt in item.options" :key="opt.id" 
                   :class="['option-item', getOptionClass(opt, item)]">
                <span class="option-tag">{{ opt.optionTag }}.</span>
                <span class="option-text">{{ opt.content }}</span>
                <span v-if="opt.isCorrect === 1" class="correct-mark">✓</span>
              </div>
            </div>

            <!-- 用户答案 -->
            <div class="answer-section">
              <div class="user-answer">
                <strong>你的答案：</strong>
                <span :class="item.detail.isCorrect === 1 ? 'text-correct' : 'text-wrong'">
                  {{ item.detail.userAnswer || '未作答' }}
                </span>
              </div>
              <div v-if="item.detail.isCorrect !== 1" class="correct-answer">
                <strong>正确答案：</strong>
                <span class="text-correct">{{ item.question.answer }}</span>
              </div>
            </div>

            <!-- 解析 -->
            <div v-if="item.question.analysis" class="analysis-section">
              <strong>📖 解析：</strong>
              <p>{{ item.question.analysis }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const result = ref({})

const accuracyRate = computed(() => {
  if (result.value.totalCount === 0) return 0
  return Math.round((result.value.correctCount / result.value.totalCount) * 100)
})

function getOptionClass(opt, item) {
  const classes = []
  if (opt.isCorrect === 1) {
    classes.push('is-correct')
  }
  if (item.detail.userAnswer && item.detail.userAnswer.includes(opt.optionTag)) {
    classes.push('is-selected')
    if (opt.isCorrect !== 1) {
      classes.push('is-wrong')
    }
  }
  return classes.join(' ')
}

async function loadDetail() {
  const recordId = route.params.recordId
  if (!recordId) {
    router.back()
    return
  }

  loading.value = true
  try {
    const res = await request.get(`/exam/paper/record/${recordId}/detail`)
    if (res.data.code === 200) {
      result.value = res.data.data
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.exam-result-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
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

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
  font-size: 16px;
}

.result-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 成绩概览 */
.score-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.score-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.score-label {
  font-size: 14px;
  color: #999;
}

.score-card.correct .score-value {
  color: #43e97b;
}

.score-card.wrong .score-value {
  color: #f5576c;
}

/* 防作弊信息 */
.anti-cheat-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #faad14;
}

.anti-cheat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.anti-cheat-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.cheat-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.cheat-status.warning {
  background: #fff7e6;
  color: #fa8c16;
}

.cheat-status.danger {
  background: #fff0f6;
  color: #f5576c;
}

.anti-cheat-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.cheat-stat {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cheat-label {
  font-size: 13px;
  color: #999;
}

.cheat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.cheat-value.danger {
  color: #f5576c;
}

/* 答题详情 */
.questions-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.question-card {
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.question-card:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.question-number {
  font-size: 16px;
  font-weight: bold;
  color: #667eea;
}

.question-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.question-status.correct {
  background: #e6ffed;
  color: #43e97b;
}

.question-status.wrong {
  background: #fff0f6;
  color: #f5576c;
}

.question-score {
  margin-left: auto;
  font-size: 14px;
  color: #666;
}

.question-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
}

/* 选项 */
.options-list {
  margin-bottom: 20px;
}

.option-item {
  padding: 12px 15px;
  margin-bottom: 10px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  transition: all 0.3s;
}

.option-item.is-correct {
  border-color: #43e97b;
  background: #f6ffed;
}

.option-item.is-selected {
  border-color: #667eea;
  background: #f0f5ff;
}

.option-item.is-wrong {
  border-color: #f5576c;
  background: #fff0f6;
}

.option-tag {
  font-weight: bold;
  color: #667eea;
  min-width: 20px;
}

.option-text {
  flex: 1;
  color: #333;
}

.correct-mark {
  color: #43e97b;
  font-weight: bold;
  font-size: 18px;
}

/* 答案区域 */
.answer-section {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.user-answer, .correct-answer {
  margin-bottom: 10px;
  font-size: 14px;
}

.user-answer:last-child, .correct-answer:last-child {
  margin-bottom: 0;
}

.text-correct {
  color: #43e97b;
  font-weight: 600;
}

.text-wrong {
  color: #f5576c;
  font-weight: 600;
}

/* 解析 */
.analysis-section {
  background: #fffbe6;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #faad14;
}

.analysis-section strong {
  display: block;
  margin-bottom: 8px;
  color: #fa8c16;
}

.analysis-section p {
  margin: 0;
  line-height: 1.8;
  color: #666;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn-default:hover {
  background: #e0e0e0;
}

@media (max-width: 768px) {
  .score-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .question-header {
    flex-wrap: wrap;
  }

  .question-score {
    margin-left: 0;
    width: 100%;
  }
}
</style>
