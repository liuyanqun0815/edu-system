<template>
  <div class="training-session-page">
    <!-- 顶部工具栏 -->
    <div class="session-toolbar">
      <div class="session-info">
        <span class="session-title">{{ config.grade }} - {{ difficultyText }}</span>
        <span class="question-counter">{{ currentQuestion }}/{{ totalQuestions }}</span>
      </div>
      <div class="timer" :class="{ warning: remainingTime <= 10 }">
        ⏱ {{ formatTime(remainingTime) }}
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
    </div>

    <!-- 题目区域 -->
    <div class="question-area" v-if="currentQuestionData">
      <div class="question-header">
        <span class="question-type-badge">{{ questionTypeText }}</span>
        <span class="question-index">第 {{ currentQuestion }} 题</span>
      </div>

      <div class="question-content">
        <h2 class="question-text">{{ currentQuestionData.questionContent }}</h2>
        
        <!-- 选择题 -->
        <div v-if="isChoiceQuestion" class="options-list">
          <div 
            v-for="option in parsedOptions" 
            :key="option.label"
            class="option-item"
            :class="{ selected: selectedAnswer === option.label }"
            @click="selectOption(option.label)"
          >
            <span class="option-label">{{ option.label }}</span>
            <span class="option-content">{{ option.content }}</span>
          </div>
        </div>

        <!-- 填空/拼写题 -->
        <div v-else class="input-area">
          <input 
            v-model="userAnswer" 
            type="text" 
            class="answer-input"
            placeholder="请输入答案..."
            @keyup.enter="submitAnswer"
            ref="answerInput"
          >
        </div>
      </div>

      <div class="question-actions">
        <button class="btn btn-skip" @click="skipQuestion">跳过</button>
        <button class="btn btn-submit" @click="submitAnswer" :disabled="!canSubmit">
          提交答案
        </button>
      </div>
    </div>

    <!-- 加载提示 -->
    <div v-else class="loading-area">
      <div class="loading-spinner">⏳</div>
      <p>正在加载题目...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const route = useRoute()
const router = useRouter()
const { toast, confirm: confirmDialog } = useMessage()

const sessionId = ref(route.params.sessionId)
const config = ref(JSON.parse(route.query.config || '{}'))

const currentQuestion = ref(1)
const totalQuestions = ref(config.value.questionCount || 20)
const remainingTime = ref(config.value.perQuestionTime || 30)
const currentQuestionData = ref(null)
const userAnswer = ref('')
const selectedAnswer = ref('')
const answerStartTime = ref(null)
const timer = ref(null)
const answerInput = ref(null)

const progressPercent = computed(() => {
  return ((currentQuestion.value - 1) / totalQuestions.value) * 100
})

const isChoiceQuestion = computed(() => {
  const type = config.value.questionType
  return type === 'choice_select' || type === 'choice_word'
})

const questionTypeText = computed(() => {
  const typeMap = {
    spell: '📝 拼写题',
    choice_select: '🔤 看词选义',
    choice_word: '📖 看义选词',
    translate: '🌐 翻译题',
    sentence_fill: '✍️ 例句填空'
  }
  return typeMap[config.value.questionType] || '题目'
})

const difficultyText = computed(() => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[config.value.difficulty] || ''
})

const canSubmit = computed(() => {
  if (isChoiceQuestion.value) {
    return selectedAnswer.value !== ''
  }
  return userAnswer.value.trim() !== ''
})

const parsedOptions = computed(() => {
  if (!currentQuestionData.value?.options) return []
  try {
    return JSON.parse(currentQuestionData.value.options)
  } catch (e) {
    return []
  }
})

function formatTime(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function startTimer() {
  if (timer.value) clearInterval(timer.value)
  remainingTime.value = config.value.perQuestionTime || 30
  
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      // 超时自动提交
      toast.warning('时间到!自动跳过')
      submitAnswer(true)
    }
  }, 1000)
}

async function loadQuestion() {
  try {
    const res = await request.get(`/word/training/${sessionId.value}/next`)
    if (res.data.code === 200) {
      currentQuestionData.value = res.data.data
      userAnswer.value = ''
      selectedAnswer.value = ''
      answerStartTime.value = Date.now()
      startTimer()
      
      // 自动聚焦输入框
      await nextTick()
      if (answerInput.value && !isChoiceQuestion.value) {
        answerInput.value.focus()
      }
    } else {
      toast.error(res.data.msg || '加载题目失败')
    }
  } catch (e) {
    toast.error('加载题目失败: ' + (e.response?.data?.msg || e.message))
  }
}

function selectOption(label) {
  selectedAnswer.value = label
  userAnswer.value = label
}

async function submitAnswer(isTimeout = false) {
  if (!canSubmit.value && !isTimeout) return

  const answerTime = Math.floor((Date.now() - answerStartTime.value) / 1000)
  
  try {
    const res = await request.post('/word/training/answer', {
      sessionId: sessionId.value,
      wordId: currentQuestionData.value.wordId,
      userAnswer: userAnswer.value || selectedAnswer.value,
      skipped: false,
      answerTime: answerTime,
      timeout: isTimeout
    })

    if (res.data.code === 200) {
      const isCorrect = res.data.data
      
      if (isTimeout) {
        toast.warning('已超时,进入下一题')
      } else if (isCorrect) {
        toast.success('✅ 回答正确!')
      } else {
        toast.error('❌ 回答错误')
      }

      // 下一题
      if (currentQuestion.value < totalQuestions.value) {
        currentQuestion.value++
        await loadQuestion()
      } else {
        // 完成所有题目
        await finishTraining()
      }
    }
  } catch (e) {
    toast.error('提交失败: ' + (e.response?.data?.msg || e.message))
  }
}

async function skipQuestion() {
  try {
    const answerTime = Math.floor((Date.now() - answerStartTime.value) / 1000)
    
    await request.post('/word/training/answer', {
      sessionId: sessionId.value,
      wordId: currentQuestionData.value.wordId,
      userAnswer: null,
      skipped: true,
      answerTime: answerTime,
      timeout: false
    })

    toast.info('已跳过')

    if (currentQuestion.value < totalQuestions.value) {
      currentQuestion.value++
      await loadQuestion()
    } else {
      await finishTraining()
    }
  } catch (e) {
    toast.error('跳过失败')
  }
}

async function finishTraining() {
  if (timer.value) clearInterval(timer.value)
  
  const confirmed = await confirmDialog({
    title: '训练完成',
    message: '是否查看训练结果?',
    confirmText: '查看结果',
    cancelText: '返回首页'
  })

  if (confirmed) {
    router.push({ 
      name: 'word-training-result', 
      params: { sessionId: sessionId.value }
    })
  } else {
    router.push({ name: 'word-training' })
  }
}

onMounted(() => {
  loadQuestion()
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<style scoped>
.training-session-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.session-toolbar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.session-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.session-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.question-counter {
  font-size: 16px;
  color: #666;
}

.timer {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 12px;
  color: #4CAF50;
}

.timer.warning {
  color: #FF5722;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  transition: width 0.3s;
}

.question-area {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  max-width: 800px;
  margin: 0 auto;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.question-type-badge {
  background: #4472C4;
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.question-index {
  font-size: 16px;
  color: #666;
}

.question-text {
  font-size: 24px;
  color: #333;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #4472C4;
  background: #f5f9ff;
}

.option-item.selected {
  border-color: #4472C4;
  background: #e8f0fe;
}

.option-label {
  font-size: 18px;
  font-weight: 600;
  color: #4472C4;
  margin-right: 16px;
  min-width: 30px;
}

.option-content {
  font-size: 16px;
  color: #333;
}

.input-area {
  margin-bottom: 30px;
}

.answer-input {
  width: 100%;
  padding: 16px 20px;
  font-size: 18px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  transition: border-color 0.3s;
}

.answer-input:focus {
  outline: none;
  border-color: #4472C4;
}

.question-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 14px 32px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-skip {
  background: #f0f0f0;
  color: #666;
}

.btn-skip:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-submit {
  background: #4472C4;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #355da7;
}

.loading-area {
  text-align: center;
  padding: 60px 20px;
  color: white;
}

.loading-spinner {
  font-size: 48px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
