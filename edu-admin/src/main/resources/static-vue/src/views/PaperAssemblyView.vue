<template>
  <div class="paper-assembly-page">
    <div class="page-header">
      <h1 class="page-title">智能组卷</h1>
      <p class="page-desc">选择条件自动生成试卷，支持预览和手动调整</p>
    </div>

    <div class="assembly-container">
      <!-- 左侧：配置面板 -->
      <div class="config-panel">
        <div class="panel-section">
          <h3>📋 基础配置</h3>
          <div class="form-group">
            <label>试卷名称</label>
            <input v-model="form.paperName" type="text" placeholder="请输入试卷名称">
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>科目</label>
              <select v-model="form.subjectId">
                <option value="">请选择科目</option>
                <option v-for="subject in subjects" :key="subject.id" :value="subject.id">
                  {{ subject.subjectName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>年级</label>
              <select v-model="form.grade">
                <option value="">请选择年级</option>
                <option v-for="grade in grades" :key="grade" :value="grade">{{ grade }}</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>考试类型</label>
            <select v-model="form.examType">
              <option value="">不限</option>
              <option value="1">单元测试</option>
              <option value="2">期中考试</option>
              <option value="3">期末考试</option>
              <option value="4">模拟考</option>
              <option value="5">真题</option>
              <option value="6">课后练习</option>
            </select>
          </div>
        </div>

        <div class="panel-section">
          <h3>📊 题型配置</h3>
          <div v-for="(config, index) in form.difficultyConfigs" :key="index" class="config-item">
            <div class="config-header">
              <span>配置 {{ index + 1 }}</span>
              <button @click="removeConfig(index)" class="btn-remove">删除</button>
            </div>
            
            <div class="config-fields">
              <select v-model="config.questionType">
                <option value="1">单选题</option>
                <option value="2">多选题</option>
                <option value="3">判断题</option>
                <option value="4">填空题</option>
                <option value="5">简答题</option>
              </select>
              
              <select v-model="config.difficulty">
                <option value="1">简单</option>
                <option value="2">中等</option>
                <option value="3">困难</option>
              </select>

              <input v-model.number="config.count" type="number" placeholder="数量" min="1">
              <input v-model.number="config.score" type="number" placeholder="分值" min="1">
            </div>
          </div>
          
          <button @click="addConfig" class="btn-add">+ 添加题型配置</button>
        </div>

        <div class="panel-actions">
          <button @click="previewAssembly" class="btn btn-primary" :disabled="previewing">
            {{ previewing ? '生成中...' : '🤖 AI一键组卷' }}
          </button>
          <button @click="useFavorites" class="btn btn-warning">
            ⭐ 使用收藏 ({{ favorites.length }})
          </button>
        </div>
      </div>

      <!-- 右侧：预览和调整面板 -->
      <div class="preview-panel" v-if="previewResult">
        <div class="preview-header">
          <h3>📝 试卷预览</h3>
          <div class="preview-actions">
            <button @click="showFavorites = true" class="btn btn-secondary" :disabled="favorites.length === 0">
              ⭐ 收藏题目
            </button>
            <button @click="smartRecommend" class="btn btn-info">
              🎯 智能推荐
            </button>
            <button @click="showQuestionPool = true" class="btn btn-secondary">📚 换题</button>
            <button @click="saveAssembly" class="btn btn-success" :disabled="saving">
              {{ saving ? '保存中...' : '💾 保存试卷' }}
            </button>
          </div>
        </div>
        
        <div class="preview-tips">
          💡 提示：拖拽题目可调整顺序 | Ctrl+P预览 | Ctrl+S保存
        </div>

        <div class="preview-stats">
          <div class="stat-item">
            <span class="stat-label">总题数</span>
            <span class="stat-value">{{ previewResult.totalCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总分</span>
            <span class="stat-value">{{ previewResult.totalScore }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">简单题</span>
            <span class="stat-value">{{ previewResult.difficultyStats.easyCount }}道</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">中等题</span>
            <span class="stat-value">{{ previewResult.difficultyStats.mediumCount }}道</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">困难题</span>
            <span class="stat-value">{{ previewResult.difficultyStats.hardCount }}道</span>
          </div>
        </div>

        <div class="question-list">
          <draggable 
            v-model="previewResult.selectedQuestions" 
            v-bind="dragOptions"
            @end="onDragEnd"
            item-key="id"
          >
            <template #item="{element: question, index}">
              <div class="question-card">
                <div class="question-header">
                  <span class="drag-handle">⋮⋮</span>
                  <span class="question-number">第{{ index + 1 }}题</span>
                  <div class="question-tags">
                    <span class="tag tag-type">{{ getQuestionTypeName(question.questionType) }}</span>
                    <span class="tag tag-difficulty" :class="getDifficultyClass(question.difficulty)">
                      {{ getDifficultyName(question.difficulty) }}
                    </span>
                    <span class="tag tag-score">{{ question.usageCount }}分</span>
                  </div>
                  <div class="question-actions">
                    <button 
                      @click="toggleFavorite(question)" 
                      class="btn-favorite"
                      :class="{ active: isFavorited(question.id) }"
                      title="收藏题目"
                    >
                      {{ isFavorited(question.id) ? '★' : '☆' }}
                    </button>
                    <button @click="removeQuestion(index)" class="btn-remove-question">✕</button>
                  </div>
                </div>
                
                <div class="question-content">{{ question.questionTitle }}</div>
                
                <div v-if="question.images" class="question-images">
                  <img v-for="(img, imgIdx) in parseImages(question.images)" :key="imgIdx" :src="img" class="question-image">
                </div>

                <div v-if="question.options" class="question-options">
                  <div v-for="opt in parseOptions(question.options)" :key="opt.label" class="option-item">
                    <span class="option-label">{{ opt.label }}.</span>
                    <span class="option-content">{{ opt.content }}</span>
                  </div>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <div v-else class="preview-placeholder">
        <div class="placeholder-icon">📄</div>
        <div class="placeholder-text">配置完成后点击"AI一键组卷"预览试卷</div>
      </div>
    </div>

    <!-- 候选题目池弹窗 -->
    <div v-if="showQuestionPool" class="modal-overlay" @click.self="showQuestionPool = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>候选题目池</h3>
          <button @click="showQuestionPool = false" class="btn-close">✕</button>
        </div>
        
        <div class="pool-filters">
          <select v-model="poolFilters.difficulty">
            <option value="">全部难度</option>
            <option value="1">简单</option>
            <option value="2">中等</option>
            <option value="3">困难</option>
          </select>
          <select v-model="poolFilters.questionType">
            <option value="">全部题型</option>
            <option value="1">单选题</option>
            <option value="2">多选题</option>
            <option value="3">判断题</option>
            <option value="4">填空题</option>
            <option value="5">简答题</option>
          </select>
          <input 
            @input="handleSearchInput($event.target.value)" 
            type="text" 
            placeholder="搜索题目..."
            class="search-input"
          >
          <button @click="loadQuestionPool" class="btn btn-primary" :disabled="loadingPool">
            {{ loadingPool ? '加载中...' : '搜索' }}
          </button>
        </div>

        <div class="pool-questions">
          <div v-if="loadingPool" class="loading-placeholder">加载中...</div>
          <div v-else-if="poolQuestions.length === 0" class="empty-placeholder">
            暂无候选题目，请调整筛选条件
          </div>
          <div v-for="q in poolQuestions" :key="q.id" class="pool-question-item">
            <input type="checkbox" v-model="selectedPoolQuestions" :value="q.id">
            <div class="pool-question-content">
              <div class="pool-question-title">{{ q.questionTitle }}</div>
              <div class="pool-question-meta">
                <span class="tag">{{ getQuestionTypeName(q.questionType) }}</span>
                <span class="tag">{{ getDifficultyName(q.difficulty) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <span class="selected-count">已选择 {{ selectedPoolQuestions.length }} 题</span>
          <button @click="replaceQuestions" class="btn btn-primary">添加题目</button>
        </div>
      </div>
    </div>

    <!-- 收藏题目弹窗 -->
    <div v-if="showFavorites" class="modal-overlay" @click.self="showFavorites = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>⭐ 收藏题目</h3>
          <button @click="showFavorites = false" class="btn-close">✕</button>
        </div>

        <div class="pool-questions">
          <div v-if="favorites.length === 0" class="empty-placeholder">
            暂无收藏题目<br>
            <small>在试卷预览中点击题目旁的☆即可收藏</small>
          </div>
          <div v-for="fav in favorites" :key="fav.id" class="pool-question-item">
            <div class="pool-question-content">
              <div class="pool-question-title">{{ fav.title }}</div>
              <div class="pool-question-meta">
                <span class="tag">{{ getQuestionTypeName(fav.type) }}</span>
                <span class="tag">{{ getDifficultyName(fav.difficulty) }}</span>
              </div>
            </div>
            <button @click="removeFavorite(fav.id)" class="btn-remove">删除</button>
          </div>
        </div>

        <div class="modal-footer">
          <span class="selected-count">共 {{ favorites.length }} 题</span>
          <button @click="useFavorites" class="btn btn-primary">使用收藏</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import request from '@/utils/request'
import toast from '@/utils/toast'
import draggable from 'vuedraggable'

// ✅ 容错处理：检查vuedraggable是否正确加载
if (!draggable) {
  console.error('vuedraggable组件未正确加载，请运行: npm install vuedraggable@next')
}

const form = reactive({
  paperName: '',
  subjectId: null,
  grade: '',
  examType: null,
  difficultyConfigs: [
    { questionType: 1, difficulty: 1, count: 10, score: 5 }
  ]
})

const previewResult = ref(null)
const previewing = ref(false)
const saving = ref(false)
const showQuestionPool = ref(false)
const poolQuestions = ref([])
const selectedPoolQuestions = ref([])
const poolFilters = reactive({
  difficulty: '',
  questionType: '',
  keyword: ''
})
const loadingPool = ref(false)

const subjects = ref([])
const grades = ['小学六年级', '初一', '初二', '初三', '高一', '高二', '高三']

// 题目收藏功能
const favorites = ref([])
const showFavorites = ref(false)

// 拖拽排序
const dragOptions = {
  animation: 200,
  ghostClass: 'ghost',
  chosenClass: 'chosen',
  dragClass: 'dragging'
}

// 搜索防抖
let searchTimer = null
const handleSearchInput = (value) => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    poolFilters.keyword = value
    loadQuestionPool()
  }, 500)
}

onMounted(async () => {
  await loadSubjects()
  await loadFavorites()
})

async function loadSubjects() {
  try {
    const res = await request.get('/exam/subject/list')
    subjects.value = res.data || []
  } catch (e) {
    console.error('加载科目失败', e)
  }
}

async function loadFavorites() {
  try {
    const stored = localStorage.getItem('questionFavorites')
    if (stored) {
      const parsed = JSON.parse(stored)
      // ✅ 验证数据格式
      if (Array.isArray(parsed)) {
        favorites.value = parsed
      } else {
        console.warn('收藏数据格式错误，已重置')
        favorites.value = []
      }
    } else {
      favorites.value = []
    }
  } catch (e) {
    console.error('加载收藏失败:', e)
    favorites.value = []
    // ✅ 清理损坏的数据
    localStorage.removeItem('questionFavorites')
  }
}

function saveFavorites() {
  try {
    localStorage.setItem('questionFavorites', JSON.stringify(favorites.value))
  } catch (e) {
    console.error('保存收藏失败:', e)
    toast.error('收藏保存失败，可能是浏览器存储限制')
  }
}

function toggleFavorite(question) {
  const idx = favorites.value.findIndex(f => f.id === question.id)
  if (idx > -1) {
    favorites.value.splice(idx, 1)
    toast.info('已取消收藏')
  } else {
    favorites.value.push({
      id: question.id,
      title: question.questionTitle,
      type: question.questionType,
      difficulty: question.difficulty
    })
    toast.success('已收藏题目')
  }
  saveFavorites()
}

function isFavorited(questionId) {
  return favorites.value.some(f => f.id === questionId)
}

function removeFavorite(questionId) {
  const idx = favorites.value.findIndex(f => f.id === questionId)
  if (idx > -1) {
    favorites.value.splice(idx, 1)
    saveFavorites()
    toast.success('已删除收藏')
  }
}

function addConfig() {
  form.difficultyConfigs.push({
    questionType: 1,
    difficulty: 2,
    count: 5,
    score: 10
  })
}

function removeConfig(index) {
  form.difficultyConfigs.splice(index, 1)
}

async function previewAssembly() {
  if (!form.subjectId || !form.grade) {
    toast.warning('请选择科目和年级')
    return
  }

  previewing.value = true
  try {
    const res = await request.post('/exam/paper/preview', {
      subjectId: form.subjectId,
      grade: form.grade,
      examType: form.examType,
      difficultyConfigs: form.difficultyConfigs
    })
    previewResult.value = res.data
    toast.success('组卷成功')
  } catch (e) {
    toast.error('组卷失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    previewing.value = false
  }
}

function removeQuestion(index) {
  if (!previewResult.value || !previewResult.value.selectedQuestions) {
    console.warn('预览数据不存在')
    return
  }
  
  if (index >= 0 && index < previewResult.value.selectedQuestions.length) {
    previewResult.value.selectedQuestions.splice(index, 1)
    previewResult.value.totalCount--
    toast.info('已删除题目')
  } else {
    console.warn('题目索引无效:', index)
  }
}

// 拖拽排序完成回调
function onDragEnd() {
  // 重新计算题目序号
  if (previewResult.value && previewResult.value.selectedQuestions) {
    previewResult.value.selectedQuestions.forEach((q, idx) => {
      q.tempOrder = idx + 1
    })
    toast.success('题目顺序已调整')
  }
}

async function saveAssembly() {
  if (!previewResult.value) {
    toast.warning('请先生成试卷')
    return
  }
  
  if (!previewResult.value.selectedQuestions || previewResult.value.selectedQuestions.length === 0) {
    toast.warning('试卷中没有题目')
    return
  }

  saving.value = true
  try {
    // 先创建试卷
    const paperRes = await request.post('/exam/paper', {
      name: form.paperName || `${form.grade}试卷`,
      subjectId: form.subjectId,
      totalScore: previewResult.value.totalScore,
      questionCount: previewResult.value.totalCount
    })

    // 保存题目关联
    const questionIds = previewResult.value.selectedQuestions.map(q => q.id)
    const scores = previewResult.value.selectedQuestions.map(q => q.usageCount || 10)

    await request.post('/exam/paper/manual-assembly', {
      paperId: paperRes.data.id,
      questionIds,
      scores
    })

    toast.success('试卷保存成功')
    
    // ✅ 可选：跳转到试卷详情页
    // router.push(`/exam/paper/${paperRes.data.id}`)
  } catch (e) {
    console.error('保存试卷失败:', e)
    toast.error('保存失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    saving.value = false
  }
}

async function loadQuestionPool() {
  if (!form.subjectId || !form.grade) {
    toast.warning('请先选择科目和年级')
    return
  }
  
  loadingPool.value = true
  try {
    const res = await request.get('/exam/paper/question-pool', {
      params: {
        subjectId: form.subjectId,
        grade: form.grade,
        examType: form.examType,
        difficulty: poolFilters.difficulty || undefined,
        questionType: poolFilters.questionType || undefined,
        keyword: poolFilters.keyword || undefined
      }
    })
    
    // 题目去重：排除已选中的题目
    const selectedIds = new Set(
      previewResult.value?.selectedQuestions?.map(q => q.id) || []
    )
    poolQuestions.value = (res.data || []).filter(q => !selectedIds.has(q.id))
    
    if (poolQuestions.value.length === 0) {
      toast.info('暂无候选题目，请调整筛选条件')
    }
  } catch (e) {
    console.error('加载题目池失败:', e)
    toast.error('加载题目池失败: ' + (e.response?.data?.msg || e.message))
    poolQuestions.value = []
  } finally {
    loadingPool.value = false
  }
}

function replaceQuestions() {
  if (selectedPoolQuestions.value.length === 0) {
    toast.warning('请选择题目')
    return
  }
  
  // 获取新题目详情
  const newQuestions = poolQuestions.value.filter(q => 
    selectedPoolQuestions.value.includes(q.id)
  )
  
  // 添加到预览结果
  if (previewResult.value) {
    newQuestions.forEach(q => {
      q.usageCount = 10 // 默认分值
      previewResult.value.selectedQuestions.push(q)
      previewResult.value.totalCount++
    })
  }
  
  toast.success(`已添加${newQuestions.length}道题目`)
  selectedPoolQuestions.value = []
  showQuestionPool.value = false
  
  // 重新加载题目池（去重）
  loadQuestionPool()
}

// 快速使用收藏题目
async function useFavorites() {
  if (favorites.value.length === 0) {
    toast.warning('暂无收藏题目')
    return
  }
  
  try {
    // 获取收藏题目的详情
    const questionIds = favorites.value.map(f => f.id)
    const res = await request.get('/exam/question/list-by-ids', {
      params: { ids: questionIds.join(',') }
    })
    
    if (res.data && res.data.length > 0) {
      // 过滤掉已存在的题目
      const selectedIds = new Set(
        previewResult.value?.selectedQuestions?.map(q => q.id) || []
      )
      const availableQuestions = res.data.filter(q => !selectedIds.has(q.id))
      
      if (availableQuestions.length === 0) {
        toast.warning('收藏的题目已在试卷中')
        return
      }
      
      // 添加到预览
      availableQuestions.forEach(q => {
        q.usageCount = 10
      })
      
      if (!previewResult.value) {
        previewResult.value = {
          selectedQuestions: [],
          totalCount: 0,
          totalScore: 0,
          difficultyStats: {
            easyCount: 0,
            mediumCount: 0,
            hardCount: 0,
            easyScore: 0,
            mediumScore: 0,
            hardScore: 0
          }
        }
      }
      
      previewResult.value.selectedQuestions.push(...availableQuestions)
      previewResult.value.totalCount += availableQuestions.length
      
      toast.success(`已添加${availableQuestions.length}道收藏题目`)
    } else {
      toast.warning('收藏的题目可能已被删除')
      // ✅ 清理无效收藏
      const validIds = new Set(res.data.map(q => q.id))
      favorites.value = favorites.value.filter(f => validIds.has(f.id))
      saveFavorites()
    }
  } catch (e) {
    console.error('加载收藏题目失败:', e)
    toast.error('加载收藏题目失败: ' + (e.response?.data?.msg || e.message))
  }
}

// 智能推荐题目
async function smartRecommend() {
  if (!previewResult.value) {
    toast.warning('请先生成试卷')
    return
  }
  
  if (!previewResult.value.selectedQuestions || previewResult.value.selectedQuestions.length === 0) {
    toast.warning('试卷中没有题目，无法推荐')
    return
  }
  
  try {
    // 获取当前试卷的难度分布
    const currentQuestions = previewResult.value.selectedQuestions
    const difficultyMap = {
      1: currentQuestions.filter(q => q.difficulty === 1).length,
      2: currentQuestions.filter(q => q.difficulty === 2).length,
      3: currentQuestions.filter(q => q.difficulty === 3).length
    }
    
    // 找出最缺的难度
    let targetDifficulty = 1
    let minCount = difficultyMap[1]
    
    if (difficultyMap[2] < minCount) {
      targetDifficulty = 2
      minCount = difficultyMap[2]
    }
    if (difficultyMap[3] < minCount) {
      targetDifficulty = 3
    }
    
    // ✅ 如果难度分布均衡，提示用户
    if (difficultyMap[1] === difficultyMap[2] && difficultyMap[2] === difficultyMap[3]) {
      toast.info('难度分布均衡，无需补充')
      return
    }
    
    toast.info(`智能推荐：缺少${getDifficultyName(targetDifficulty)}题目，正在加载...`)
    
    // 加载对应难度的题目
    poolFilters.difficulty = targetDifficulty.toString()
    await loadQuestionPool()
    showQuestionPool.value = true
  } catch (e) {
    console.error('智能推荐失败:', e)
    toast.error('智能推荐失败: ' + (e.response?.data?.msg || e.message))
  }
}

function getQuestionTypeName(type) {
  const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答' }
  return map[type] || '未知'
}

function getDifficultyName(diff) {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[diff] || '未知'
}

function getDifficultyClass(diff) {
  const map = { 1: 'tag-easy', 2: 'tag-medium', 3: 'tag-hard' }
  return map[diff] || ''
}

function parseImages(imagesStr) {
  try {
    return JSON.parse(imagesStr)
  } catch {
    return []
  }
}

function parseOptions(optionsStr) {
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}

// 监听科目变化，自动重置
watch(() => form.subjectId, () => {
  previewResult.value = null
})

// 快捷键支持
onMounted(() => {
  document.addEventListener('keydown', (e) => {
    // Ctrl+S 保存
    if (e.ctrlKey && e.key === 's' && previewResult.value) {
      e.preventDefault()
      saveAssembly()
    }
    // Ctrl+P 预览
    if (e.ctrlKey && e.key === 'p') {
      e.preventDefault()
      previewAssembly()
    }
  })
})
</script>

<style scoped>
.paper-assembly-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.assembly-container {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
}

.config-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.panel-section {
  margin-bottom: 20px;
}

.panel-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px 0;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.config-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
}

.config-fields {
  display: grid;
  grid-template-columns: 1fr 1fr 80px 80px;
  gap: 8px;
}

.config-fields select,
.config-fields input {
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 13px;
}

.btn-add {
  width: 100%;
  padding: 10px;
  background: #f0f5ff;
  border: 1px dashed #667eea;
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
}

.btn-remove {
  background: none;
  border: none;
  color: #f5576c;
  cursor: pointer;
  font-size: 12px;
}

.panel-actions {
  margin-top: 20px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  width: 100%;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #f0f5ff;
  color: #667eea;
}

.btn-success {
  background: #52c41a;
  color: white;
}

.btn-warning {
  background: #faad14;
  color: white;
  margin-top: 10px;
}

.btn-info {
  background: #1890ff;
  color: white;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preview-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.preview-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.preview-tips {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
  padding: 10px 15px;
  margin-bottom: 15px;
  border-radius: 4px;
  font-size: 13px;
  color: #1890ff;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

.preview-actions .btn {
  width: auto;
  padding: 8px 16px;
  font-size: 13px;
}

.preview-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #667eea;
}

.question-list {
  max-height: 600px;
  overflow-y: auto;
}

.question-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 12px;
  background: white;
  transition: all 0.3s;
  cursor: move;
}

.question-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

/* 拖拽样式 */
.ghost {
  opacity: 0.5;
  background: #f0f5ff;
}

.chosen {
  background: #f0f5ff;
}

.dragging {
  opacity: 0.8;
  background: #fafafa;
}

.drag-handle {
  cursor: move;
  color: #999;
  font-size: 18px;
  margin-right: 8px;
  user-select: none;
}

.drag-handle:hover {
  color: #667eea;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-number {
  font-weight: 600;
  color: #667eea;
}

.question-tags {
  display: flex;
  gap: 6px;
  flex: 1;
}

.tag {
  padding: 3px 8px;
  background: #f0f5ff;
  border-radius: 4px;
  font-size: 11px;
  color: #667eea;
}

.tag-easy { background: #f6ffed; color: #52c41a; }
.tag-medium { background: #fff7e6; color: #fa8c16; }
.tag-hard { background: #fff1f0; color: #f5222d; }

.tag-score {
  background: #667eea;
  color: white;
}

.question-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

.btn-favorite {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px;
}

.btn-favorite:hover {
  color: #faad14;
  transform: scale(1.2);
}

.btn-favorite.active {
  color: #faad14;
}

.btn-remove-question {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  transition: all 0.3s;
}

.btn-remove-question:hover {
  color: #f5576c;
  transform: scale(1.2);
}

.question-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
}

.question-images {
  margin-bottom: 10px;
}

.question-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 6px;
  margin-right: 10px;
}

.question-options {
  padding-left: 20px;
}

.option-item {
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
}

.option-label {
  font-weight: 600;
  margin-right: 6px;
}

.preview-placeholder {
  background: white;
  border-radius: 12px;
  padding: 100px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.placeholder-text {
  font-size: 14px;
  color: #999;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.pool-filters {
  padding: 15px 20px;
  display: flex;
  gap: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.pool-filters select,
.pool-filters input,
.search-input {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.3s;
}

.pool-filters input:focus,
.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.pool-filters input {
  flex: 1;
}

.pool-questions {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.pool-question-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.pool-question-item:hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.pool-question-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}

.pool-question-meta {
  display: flex;
  gap: 6px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

.modal-footer .btn {
  width: auto;
}

.loading-placeholder,
.empty-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.empty-placeholder small {
  display: block;
  margin-top: 10px;
  color: #bbb;
  font-size: 12px;
}
</style>
