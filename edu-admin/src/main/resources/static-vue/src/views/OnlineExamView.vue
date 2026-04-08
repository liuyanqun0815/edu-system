<template>
  <div class="online-exam-page">
    <div class="page-header">
      <h1 class="page-title">在线考试</h1>
    </div>

    <div class="exam-list" v-if="!currentExam">
      <div class="search-bar">
        <select v-model="filter.subject">
          <option value="">全部科目</option>
          <option v-for="s in subjectOptions" :key="s.itemValue" :value="s.itemValue">{{ s.itemName || s.itemLabel }}</option>
        </select>
        <select v-model="filter.status">
          <option value="">全部状态</option>
          <option value="1">进行中</option>
          <option value="0">未开始</option>
          <option value="2">已结束</option>
        </select>
        <button class="btn btn-primary" @click="fetchExamList">查询</button>
      </div>

      <div class="exam-cards">
        <div v-for="exam in examList" :key="exam.id" class="exam-card" :class="{ disabled: exam.status === 2 }">
          <div class="exam-header">
            <span class="exam-subject">{{ exam.subjectName }}</span>
            <span class="exam-status" :class="'status-' + exam.status">{{ statusMap[exam.status] }}</span>
          </div>
          <h3 class="exam-title">{{ exam.name }}</h3>
          <div class="exam-info">
            <span>⏱ {{ exam.duration }}分钟</span>
            <span>📝 {{ exam.questionCount }}题</span>
            <span>💯 {{ exam.totalScore }}分</span>
          </div>
          <div class="exam-time">
            {{ exam.startTime }} ~ {{ exam.endTime }}
          </div>
          <div class="exam-actions">
            <button v-if="exam.status === 1" class="btn btn-primary" @click="startExam(exam)">开始考试</button>
            <button v-if="exam.status === 0" class="btn btn-default" disabled>未开始</button>
            <button v-if="exam.status === 2" class="btn btn-default" @click="viewResult(exam)">查看成绩</button>
          </div>
        </div>
        <div v-if="examList.length === 0" class="empty-tip">暂无可参加的考试</div>
      </div>
    </div>

    <!-- 考试界面 -->
    <div v-else class="exam-container">
      <div class="exam-toolbar">
        <div class="exam-info-bar">
          <span class="exam-name">{{ currentExam.name }}</span>
          <span class="timer" :class="{ warning: remainingTime < 300 }">
            ⏱ 剩余时间: {{ formatTime(remainingTime) }}
          </span>
        </div>
        <div class="question-nav">
          <span
            v-for="(q, idx) in questions"
            :key="q.id"
            :class="['nav-item', { current: currentIndex === idx, answered: answers[q.id] }]"
            @click="currentIndex = idx"
          >{{ idx + 1 }}</span>
        </div>
      </div>

      <div class="question-panel" v-if="currentQuestion">
        <div class="question-header">
          <span class="question-type">{{ questionTypeMap[currentQuestion.type] }}</span>
          <span class="question-score">（{{ currentQuestion.score }}分）</span>
        </div>
        <div class="question-content">
          <p class="question-text">{{ currentIndex + 1 }}. {{ currentQuestion.content }}</p>
          <div v-if="currentQuestion.type === 1 || currentQuestion.type === 2" class="options">
            <label
              v-for="opt in currentQuestion.options"
              :key="opt.label"
              class="option-item"
              :class="{ selected: isOptionSelected(currentQuestion.id, opt.label) }"
            >
              <input
                :type="currentQuestion.type === 1 ? 'radio' : 'checkbox'"
                :name="'q-' + currentQuestion.id"
                :value="opt.label"
                @change="handleAnswer(currentQuestion.id, opt.label, currentQuestion.type === 2)"
              >
              <span class="option-label">{{ opt.label }}.</span>
              <span class="option-text">{{ opt.content }}</span>
            </label>
          </div>
          <div v-else-if="currentQuestion.type === 3" class="judge-options">
            <label class="option-item" :class="{ selected: answers[currentQuestion.id] === 'T' }">
              <input type="radio" :name="'q-' + currentQuestion.id" value="T" @change="answers[currentQuestion.id] = 'T'">
              <span>✓ 正确</span>
            </label>
            <label class="option-item" :class="{ selected: answers[currentQuestion.id] === 'F' }">
              <input type="radio" :name="'q-' + currentQuestion.id" value="F" @change="answers[currentQuestion.id] = 'F'">
              <span>✗ 错误</span>
            </label>
          </div>
        </div>

        <div class="question-actions">
          <button class="btn btn-default" :disabled="currentIndex === 0" @click="currentIndex--">上一题</button>
          <button class="btn btn-default" v-if="currentIndex < questions.length - 1" @click="currentIndex++">下一题</button>
          <button class="btn btn-primary" v-if="currentIndex === questions.length - 1" @click="submitExam">提交试卷</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const { toast, confirm } = useMessage()
const configStore = useConfigStore()

const filter = reactive({ subject: '', status: '' })
const examList = ref([])
const currentExam = ref(null)
const questions = ref([])
const answers = ref({})
const currentIndex = ref(0)
const remainingTime = ref(0)
let timer = null

const subjectOptions = computed(() => configStore.examSubjects)
const questionTypeMap = computed(() => configStore.questionTypeMap)
const statusMap = { 0: '未开始', 1: '进行中', 2: '已结束' }

const currentQuestion = computed(() => questions.value[currentIndex.value])

async function fetchExamList() {
  try {
    // 使用试卷列表接口，只查询已发布的试卷(status=1)
    const res = await request.get('/exam/paper/page', {
      params: {
        pageNum: 1,
        pageSize: 100,
        subjectId: filter.subject || undefined,
        status: 1 // 只查询已发布的试卷
      }
    })
    if (res.data.code === 200) {
      // 转换数据格式适配页面显示
      examList.value = (res.data.data.rows || []).map(paper => ({
        id: paper.id,
        name: paper.name,
        subjectName: getSubjectName(paper.subjectId),
        duration: paper.duration,
        questionCount: paper.questionCount,
        totalScore: paper.totalScore,
        status: 1, // 已发布的试卷默认进行中
        startTime: paper.createTime,
        endTime: paper.updateTime
      }))
    }
  } catch (e) {
    console.error('获取考试列表失败', e)
  }
}

// 获取学科名称
function getSubjectName(subjectId) {
  const subject = subjectOptions.value.find(s => s.itemValue === String(subjectId))
  return subject ? (subject.itemName || subject.itemLabel) : '-'
}

async function startExam(exam) {
  const confirmed = await confirm({
    title: '开始考试',
    message: `确定要开始考试 <b>"${exam.name}"</b> 吗？<br>考试时长：${exam.duration}分钟`,
    confirmText: '开始考试'
  })
  if (!confirmed) return

  try {
    const res = await request.post(`/exam/online/start/${exam.id}`)
    if (res.data.code === 200) {
      currentExam.value = exam
      questions.value = res.data.data.questions || []
      remainingTime.value = exam.duration * 60
      startTimer()
    }
  } catch (e) {
    toast.error('开始考试失败: ' + (e.response?.data?.msg || e.message))
  }
}

function startTimer() {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      submitExam()
    }
  }, 1000)
}

function formatTime(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function isOptionSelected(questionId, label) {
  const ans = answers.value[questionId]
  if (Array.isArray(ans)) {
    return ans.includes(label)
  }
  return ans === label
}

function handleAnswer(questionId, label, isMultiple) {
  if (isMultiple) {
    if (!answers.value[questionId]) {
      answers.value[questionId] = []
    }
    const idx = answers.value[questionId].indexOf(label)
    if (idx > -1) {
      answers.value[questionId].splice(idx, 1)
    } else {
      answers.value[questionId].push(label)
    }
  } else {
    answers.value[questionId] = label
  }
}

async function submitExam() {
  const confirmed = await confirm({
    title: '提交试卷',
    message: '确定要提交试卷吗？提交后不可修改。',
    confirmText: '提交'
  })
  if (!confirmed) return

  try {
    const res = await request.post('/exam/online/submit', {
      examId: currentExam.value.id,
      answers: answers.value
    })
    if (res.data.code === 200) {
      toast.success('试卷提交成功')
      const result = res.data.data
      alert(`考试结束！您的得分：${result.score}分`)
      exitExam()
    }
  } catch (e) {
    toast.error('提交失败: ' + (e.response?.data?.msg || e.message))
  }
}

function exitExam() {
  if (timer) clearInterval(timer)
  currentExam.value = null
  questions.value = []
  answers.value = {}
  currentIndex.value = 0
  fetchExamList()
}

function viewResult(exam) {
  router.push({ name: 'exam-result', params: { recordId: exam.recordId } })
}

onMounted(() => {
  configStore.loadConfigs()
  fetchExamList()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.online-exam-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.search-bar select {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  min-width: 150px;
}

.exam-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.exam-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}

.exam-card:hover:not(.disabled) {
  transform: translateY(-3px);
}

.exam-card.disabled {
  opacity: 0.7;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.exam-subject {
  font-size: 13px;
  color: #667eea;
  background: #f0f4ff;
  padding: 4px 10px;
  border-radius: 4px;
}

.exam-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 4px;
}

.status-0 { background: #fff7e6; color: #fa8c16; }
.status-1 { background: #e6f7ff; color: #1890ff; }
.status-2 { background: #f5f5f5; color: #999; }

.exam-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 15px 0;
}

.exam-info {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
}

.exam-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.exam-actions {
  text-align: right;
}

.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 60px;
  background: white;
  border-radius: 12px;
}

/* 考试界面样式 */
.exam-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.exam-toolbar {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.exam-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.exam-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.timer {
  font-size: 20px;
  font-weight: 600;
  color: #667eea;
}

.timer.warning {
  color: #ff4d4f;
  animation: blink 1s infinite;
}

@keyframes blink {
  50% { opacity: 0.6; }
}

.question-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.nav-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
}

.nav-item.current {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.nav-item.answered {
  background: #52c41a;
  border-color: #52c41a;
  color: white;
}

.question-panel {
  padding: 30px;
}

.question-header {
  margin-bottom: 20px;
}

.question-type {
  font-size: 14px;
  color: #667eea;
  background: #f0f4ff;
  padding: 4px 12px;
  border-radius: 4px;
}

.question-score {
  font-size: 14px;
  color: #999;
}

.question-text {
  font-size: 16px;
  color: #333;
  line-height: 1.8;
  margin-bottom: 25px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.option-item.selected {
  border-color: #667eea;
  background: #f0f4ff;
}

.option-item input {
  display: none;
}

.option-label {
  font-weight: 600;
  margin-right: 10px;
  color: #667eea;
}

.option-text {
  flex: 1;
}

.judge-options {
  display: flex;
  gap: 20px;
}

.question-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
}
</style>
