<template>
  <div class="wrong-book-page">
    <div class="page-header">
      <h1 class="page-title">📚 我的错题本</h1>
      <div class="header-actions">
        <button class="btn btn-info" @click="exportWrongBook">📥 导出错题</button>
        <button class="btn btn-default" @click="goBack">← 返回</button>
      </div>
    </div>

    <!-- 今日错题提示 -->
    <div v-if="todayWrongBooks.length > 0" class="today-hint">
      <div class="hint-header">
        <span class="hint-icon">📌</span>
        <h3>今日错题 ({{ todayWrongBooks.length }})</h3>
        <button class="btn btn-warning btn-sm" @click="reviewToday">立即复习</button>
      </div>
      <div class="hint-list">
        <div v-for="item in todayWrongBooks.slice(0, 5)" :key="item.id" class="hint-item">
          <span class="word">{{ getWordInfo(item.wordId)?.word }}</span>
          <span class="translation">{{ getWordInfo(item.wordId)?.translation }}</span>
          <span class="wrong-count">错{{ item.wrongCount }}次</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select" @change="loadWrongBooks">
        <option value="">全部状态</option>
        <option value="0">待复习</option>
        <option value="1">已掌握</option>
        <option value="2">需强化</option>
      </select>
    </div>

    <!-- 错题列表 -->
    <div class="wrong-list">
      <div v-for="item in wrongBooks" :key="item.id" class="wrong-card">
        <div class="card-header">
          <div class="word-info">
            <h3 class="word">{{ getWordInfo(item.wordId)?.word }}</h3>
            <span class="phonetic">{{ getWordInfo(item.wordId)?.phonetic }}</span>
          </div>
          <div class="card-actions">
            <select v-model="item.masteryLevel" @change="updateMastery(item)" class="mastery-select">
              <option value="0">未掌握</option>
              <option value="1">部分掌握</option>
              <option value="2">已掌握</option>
            </select>
            <button class="btn-icon" @click="deleteItem(item)" title="删除">🗑️</button>
          </div>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">释义:</span>
            <span class="value">{{ getWordInfo(item.wordId)?.translation }}</span>
          </div>
          <div class="info-row">
            <span class="label">题型:</span>
            <span class="value">{{ questionTypeMap[item.questionType] }}</span>
          </div>
          <div class="info-row">
            <span class="label">错误次数:</span>
            <span class="value error">{{ item.wrongCount }}次</span>
          </div>
          <div class="info-row">
            <span class="label">最后错误:</span>
            <span class="value">{{ formatDate(item.lastWrongTime) }}</span>
          </div>
        </div>
      </div>

      <div v-if="wrongBooks.length === 0" class="empty-state">
        <div class="empty-icon">🎉</div>
        <p>暂无错题,继续保持!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const router = useRouter()
const { toast, confirm: confirmDialog } = useMessage()

const wrongBooks = ref([])
const todayWrongBooks = ref([])
const wordMap = ref({})
const filterStatus = ref('')

const questionTypeMap = {
  spell: '拼写题',
  choice_select: '看词选义',
  choice_word: '看义选词',
  translate: '翻译题',
  sentence_fill: '例句填空'
}

function goBack() {
  router.push({ name: 'word-training' })
}

function getWordInfo(wordId) {
  return wordMap.value[wordId] || {}
}

function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

async function loadWrongBooks() {
  try {
    const res = await request.get('/word/wrongbook/page', {
      params: {
        reviewStatus: filterStatus.value || undefined,
        pageNum: 1,
        pageSize: 50
      }
    })
    if (res.data.code === 200) {
      wrongBooks.value = res.data.data || []
      await loadWordInfo()
    }
  } catch (e) {
    toast.error('加载错题失败')
  }
}

async function loadTodayWrong() {
  try {
    const res = await request.get('/word/wrongbook/today')
    if (res.data.code === 200) {
      todayWrongBooks.value = res.data.data || []
      await loadWordInfo()
    }
  } catch (e) {
    console.error('加载今日错题失败', e)
  }
}

async function loadWordInfo() {
  const wordIds = [...new Set([
    ...wrongBooks.value.map(item => item.wordId),
    ...todayWrongBooks.value.map(item => item.wordId)
  ])]

  for (const wordId of wordIds) {
    if (!wordMap.value[wordId]) {
      try {
        const res = await request.get(`/system/word/${wordId}`)
        if (res.data.code === 200) {
          wordMap.value[wordId] = res.data.data
        }
      } catch (e) {
        console.error('加载单词信息失败', e)
      }
    }
  }
}

async function updateMastery(item) {
  try {
    const res = await request.put(`/word/wrongbook/${item.id}/mastery`, null, {
      params: { masteryLevel: item.masteryLevel }
    })
    if (res.data.code === 200) {
      toast.success('更新成功')
    }
  } catch (e) {
    toast.error('更新失败')
  }
}

async function deleteItem(item) {
  const confirmed = await confirmDialog({
    title: '确认删除',
    message: '确定要删除这道错题吗?',
    confirmText: '删除'
  })

  if (confirmed) {
    try {
      const res = await request.delete(`/word/wrongbook/${item.id}`)
      if (res.data.code === 200) {
        toast.success('删除成功')
        loadWrongBooks()
        loadTodayWrong()
      }
    } catch (e) {
      toast.error('删除失败')
    }
  }
}

function reviewToday() {
  const wordIds = todayWrongBooks.value.map(item => item.wordId)
  toast.info(`开始复习${wordIds.length}道错题`)
  // TODO: 跳转到错题复习模式
}

function exportWrongBook() {
  window.open('/word/wrongbook/export', '_blank')
}

onMounted(() => {
  loadWrongBooks()
  loadTodayWrong()
})
</script>

<style scoped>
.wrong-book-page {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.today-hint {
  background: #FFF3E0;
  border-left: 4px solid #FF9800;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.hint-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.hint-icon {
  font-size: 24px;
}

.hint-header h3 {
  margin: 0;
  flex: 1;
}

.hint-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
}

.word {
  font-weight: 600;
  color: #333;
  min-width: 120px;
}

.translation {
  color: #666;
  flex: 1;
}

.wrong-count {
  color: #F44336;
  font-weight: 600;
}

.filter-bar {
  margin-bottom: 20px;
}

.filter-select {
  padding: 10px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.wrong-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wrong-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e0e0e0;
}

.word-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #333;
}

.phonetic {
  color: #666;
  font-size: 14px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mastery-select {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 13px;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.label {
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
}

.value.error {
  color: #F44336;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

.btn-default {
  background: #f0f0f0;
  color: #333;
}

.btn-info {
  background: #17A2B8;
  color: white;
}

.btn-warning {
  background: #FF9800;
  color: white;
}
</style>
