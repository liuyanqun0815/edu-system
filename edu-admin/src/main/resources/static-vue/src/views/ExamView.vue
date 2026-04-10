<template>
  <div class="exam-page">
    <div class="page-header">
      <h1 class="page-title">考试管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建试卷</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchForm.paperName"
        type="text"
        class="search-input"
        placeholder="试卷名称"
        @keyup.enter="handleSearch"
      >
      <select v-model="searchForm.subjectId" class="search-select">
        <option value="">全部学科</option>
        <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">
          {{ subject.subjectName }}
        </option>
      </select>
      <select v-model="searchForm.status" class="search-select">
        <option value="">全部状态</option>
        <option :value="0">草稿</option>
        <option :value="1">已发布</option>
        <option :value="2">已归档</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>试卷名称</th>
            <th>学科</th>
            <th>总分</th>
            <th>及格分</th>
            <th>时长</th>
            <th>题数</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="paper in paperList" :key="paper.id">
            <td>{{ paper.id }}</td>
            <td>{{ paper.name }}</td>
            <td>{{ getSubjectName(paper.subjectId) }}</td>
            <td>{{ paper.totalScore }}分</td>
            <td>{{ paper.passScore }}分</td>
            <td>{{ paper.duration }}分钟</td>
            <td>{{ paper.questionCount }}题</td>
            <td>
              <span :class="['status-tag', `status-${paper.status}`]">
                {{ statusMap[paper.status] }}
              </span>
            </td>
            <td>{{ formatDate(paper.createTime) }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(paper)">编辑</button>
              <button class="action-btn action-warn" @click="handleQuestions(paper)">组卷</button>
              <button class="action-btn action-export" @click="handleExport(paper)">导出</button>
              <button class="action-btn action-delete" @click="handleDelete(paper)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-left">
          <span class="total">共 <em>{{ total }}</em> 条</span>
        </div>
        <div class="pagination-center">
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(1)" title="首页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/></svg>
          </button>
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
            <span>上一页</span>
          </button>
          <span class="page-info"><strong>{{ pageNum }}</strong> / {{ totalPages }}</span>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">
            <span>下一页</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
          </button>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(totalPages)" title="末页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 17l5-5-5-5M6 17l5-5-5-5"/></svg>
          </button>
        </div>
        <div class="pagination-right">
          <select v-model="pageSize" class="page-size" @change="handleSizeChange">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑试卷' : '新建试卷' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>试卷名称 <span class="required">*</span></label>
              <input v-model="form.name" type="text" placeholder="请输入试卷名称" required>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>学科 <span class="required">*</span></label>
                <select v-model="form.subjectId" required>
                  <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">
                    {{ subject.subjectName }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>试卷类型</label>
                <select v-model="form.paperType">
                  <option :value="1">单元测试</option>
                  <option :value="2">期中考试</option>
                  <option :value="3">期末考试</option>
                  <option :value="4">模拟考试</option>
                  <option :value="5">真题演练</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>总分</label>
                <input v-model.number="form.totalScore" type="number" placeholder="默认100">
              </div>
              <div class="form-group">
                <label>及格分</label>
                <input v-model.number="form.passScore" type="number" placeholder="默认60">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>考试时长（分钟）</label>
                <input v-model.number="form.duration" type="number" placeholder="默认120">
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="0">草稿</option>
                  <option :value="1">已发布</option>
                  <option :value="2">已归档</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>试卷描述</label>
              <textarea v-model="form.description" rows="3" placeholder="请输入试卷描述"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>年级</label>
                <select v-model="form.gradeId">
                  <option :value="null">请选择年级</option>
                  <option v-for="grade in gradeList" :key="grade.id" :value="grade.id">
                    {{ grade.itemName || grade.itemLabel || grade.name }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>最大考试次数</label>
                <input v-model.number="form.maxAttempts" type="number" min="0" placeholder="0表示不限次数">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>考试开始时间</label>
                <input v-model="form.examStartTime" type="datetime-local" placeholder="请选择开始时间">
              </div>
              <div class="form-group">
                <label>考试结束时间</label>
                <input v-model="form.examEndTime" type="datetime-local" placeholder="请选择结束时间">
              </div>
            </div>
            <div class="form-group">
              <label>防作弊</label>
              <select v-model="form.antiCheat">
                <option :value="0">关闭</option>
                <option :value="1">开启</option>
              </select>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 组卷弹窗 -->
    <div v-if="showQuestionModal" class="modal-overlay" @click.self="closeQuestionModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>组卷管理 - {{ currentPaper?.name }}</h3>
          <button class="close-btn" @click="closeQuestionModal">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 组卷模式切换 -->
          <div class="assembly-tabs">
            <button :class="['tab-btn', { active: assemblyMode === 'manual' } ]" @click="assemblyMode = 'manual'">手动选题</button>
            <button :class="['tab-btn', { active: assemblyMode === 'auto' } ]" @click="assemblyMode = 'auto'">按难度组卷</button>
            <button :class="['tab-btn', { active: assemblyMode === 'topic' } ]" @click="assemblyMode = 'topic'">专题训练</button>
          </div>

          <!-- 手动选题模式 -->
          <div v-if="assemblyMode === 'manual'" class="question-pool">
            <div class="pool-header">
              <span>题库选题</span>
              <span class="selected-count">已选 {{ selectedQuestions.length }} 题</span>
            </div>
            <div class="question-list">
              <div
                v-for="question in questionList"
                :key="question.id"
                :class="['question-item', { selected: isQuestionSelected(question.id) }]"
                @click="toggleQuestion(question)"
              >
                <div class="question-header">
                  <span class="question-type">{{ questionTypeMap[question.questionType] }}</span>
                  <span class="question-difficulty">{{ difficultyMap[question.difficulty] }}</span>
                  <span class="question-score">{{ question.score }}分</span>
                </div>
                <div class="question-title">{{ question.questionTitle }}</div>
              </div>
            </div>
          </div>

          <!-- 按难度组卷模式 -->
          <div v-if="assemblyMode === 'auto'" class="auto-assembly">
            <div class="assembly-config">
              <div class="config-row">
                <label>学科：</label>
                <select v-model="assemblyConfig.subjectId">
                  <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">
                    {{ subject.subjectName }}
                  </option>
                </select>
              </div>
              <div class="config-row">
                <label>年级：</label>
                <select v-model="assemblyConfig.grade">
                  <option value="">全部年级</option>
                  <option v-for="grade in gradeList" :key="grade" :value="grade">{{ grade }}</option>
                </select>
              </div>
            </div>

            <!-- 难度配置表格 -->
            <div class="difficulty-table">
              <div class="table-header">
                <span>难度</span>
                <span>题型</span>
                <span>数量</span>
                <span>每题分值</span>
                <span>可用题数</span>
                <span>小计</span>
              </div>
              <div v-for="(config, index) in difficultyConfigs" :key="index" class="table-row">
                <select v-model="config.difficulty">
                  <option :value="1">简单</option>
                  <option :value="2">中等</option>
                  <option :value="3">困难</option>
                </select>
                <select v-model="config.questionType">
                  <option :value="1">单选</option>
                  <option :value="2">多选</option>
                  <option :value="3">判断</option>
                  <option :value="4">填空</option>
                  <option :value="5">简答</option>
                </select>
                <input v-model.number="config.count" type="number" min="0" placeholder="数量" @change="updateAvailableCount(config)">
                <input v-model.number="config.score" type="number" min="1" placeholder="分值">
                <span :class="['available-count', { warning: config.availableCount < config.count } ]">{{ config.availableCount }}</span>
                <span class="subtotal">{{ config.count * config.score }}分</span>
                <button class="remove-btn" @click="removeDifficultyConfig(index)">&times;</button>
              </div>
              <button class="add-config-btn" @click="addDifficultyConfig">+ 添加配置</button>
            </div>

            <!-- 统计信息 -->
            <div class="assembly-stats">
              <div class="stat-item">
                <span class="stat-label">总题数：</span>
                <span class="stat-value">{{ totalQuestionCount }} 题</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">总分：</span>
                <span class="stat-value">{{ totalScoreCount }} 分</span>
              </div>
            </div>

            <!-- 预览结果 -->
            <div v-if="previewResult" class="preview-result">
              <h4>组卷预览</h4>
              <div class="preview-stats">
                <span>简单: {{ previewResult.difficultyStats?.easyCount }}题 ({{ previewResult.difficultyStats?.easyScore }}分)</span>
                <span>中等: {{ previewResult.difficultyStats?.mediumCount }}题 ({{ previewResult.difficultyStats?.mediumScore }}分)</span>
                <span>困难: {{ previewResult.difficultyStats?.hardCount }}题 ({{ previewResult.difficultyStats?.hardScore }}分)</span>
              </div>
            </div>
          </div>

          <!-- 专题训练模式 -->
          <div v-if="assemblyMode === 'topic'" class="topic-assembly">
            <div class="assembly-config">
              <div class="config-row">
                <label>学科：</label>
                <select v-model="topicConfig.subjectId" @change="loadKnowledgePoints">
                  <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">
                    {{ subject.subjectName }}
                  </option>
                </select>
              </div>
              <div class="config-row">
                <label>年级：</label>
                <select v-model="topicConfig.grade">
                  <option value="">全部年级</option>
                  <option v-for="grade in gradeList" :key="grade" :value="grade">{{ grade }}</option>
                </select>
              </div>
            </div>

            <!-- 知识点选择 -->
            <div class="knowledge-points-section">
              <h4>选择知识点</h4>
              <div class="knowledge-points-list">
                <label v-for="kp in knowledgePointList" :key="kp" class="kp-item">
                  <input type="checkbox" :value="kp" v-model="topicConfig.selectedPoints">
                  <span>{{ kp }}</span>
                </label>
              </div>
              <div v-if="knowledgePointList.length === 0" class="empty-tip">
                请先选择学科，或该学科暂无知识点数据
              </div>
            </div>

            <!-- 配置 -->
            <div class="topic-config-row">
              <div class="config-item">
                <label>每个知识点抽题数：</label>
                <input v-model.number="topicConfig.countPerPoint" type="number" min="1" max="20">
              </div>
              <div class="config-item">
                <label>每题分值：</label>
                <input v-model.number="topicConfig.score" type="number" min="1" max="100">
              </div>
            </div>

            <!-- 统计 -->
            <div class="assembly-stats">
              <div class="stat-item">
                <span class="stat-label">已选知识点：</span>
                <span class="stat-value">{{ topicConfig.selectedPoints.length }} 个</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">预计题数：</span>
                <span class="stat-value">{{ topicConfig.selectedPoints.length * topicConfig.countPerPoint }} 题</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">预计总分：</span>
                <span class="stat-value">{{ topicConfig.selectedPoints.length * topicConfig.countPerPoint * topicConfig.score }} 分</span>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closeQuestionModal">取消</button>
            <button v-if="assemblyMode === 'auto'" type="button" class="btn btn-info" :disabled="submitting" @click="previewAssembly">
              预览组卷
            </button>
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="savePaperQuestions">
              {{ submitting ? '保存中...' : '保存组卷' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导出弹窗 -->
    <div v-if="showExportModal" class="modal-overlay" @click.self="closeExportModal">
      <div class="modal modal-small">
        <div class="modal-header">
          <h3>导出试卷 - {{ currentPaper?.name }}</h3>
          <button class="close-btn" @click="closeExportModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="export-options">
            <button class="export-btn" @click="doExport('word')">
              <span class="export-icon">📄</span>
              <span class="export-label">导出Word</span>
              <span class="export-desc">可编辑的Word文档</span>
            </button>
            <button class="export-btn" @click="doExport('pdf')">
              <span class="export-icon">📑</span>
              <span class="export-label">导出PDF</span>
              <span class="export-desc">适合打印和分享</span>
            </button>
            <button class="export-btn" @click="doPrint">
              <span class="export-icon">🖨️</span>
              <span class="export-label">在线打印</span>
              <span class="export-desc">直接打印试卷</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const { toast, confirm } = useMessage()
const configStore = useConfigStore()

const statusMap = { 0: '草稿', 1: '已发布', 2: '已归档' }
const questionTypeMap = computed(() => configStore.questionTypeMap)
const difficultyMap = computed(() => configStore.difficultyMap)

// 搜索表单
const searchForm = reactive({
  paperName: '',
  subjectId: '',
  status: ''
})

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

// 数据列表
const paperList = ref([])
const subjectList = ref([])
const questionList = ref([])
const selectedQuestions = ref([])

// 弹窗控制
const showModal = ref(false)
const showQuestionModal = ref(false)
const showExportModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentPaper = ref(null)

// 组卷相关
const assemblyMode = ref('manual')
const previewResult = ref(null)
const assemblyConfig = reactive({
  subjectId: null,
  grade: ''
})
const difficultyConfigs = ref([
  { difficulty: 1, questionType: 1, count: 5, score: 2, availableCount: 0 },
  { difficulty: 2, questionType: 1, count: 3, score: 3, availableCount: 0 },
  { difficulty: 3, questionType: 1, count: 2, score: 5, availableCount: 0 }
])

// 专题训练相关
const knowledgePointList = ref([])
const topicConfig = reactive({
  subjectId: null,
  grade: '',
  selectedPoints: [],
  countPerPoint: 5,
  score: 10
})

// 计算总题数和总分
const totalQuestionCount = computed(() => {
  return difficultyConfigs.value.reduce((sum, c) => sum + (c.count || 0), 0)
})
const totalScoreCount = computed(() => {
  return difficultyConfigs.value.reduce((sum, c) => sum + (c.count || 0) * (c.score || 0), 0)
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  subjectId: null,
  gradeId: null,
  paperType: 1,
  totalScore: 100,
  passScore: 60,
  duration: 120,
  questionCount: 0,
  description: '',
  status: 0,
  examStartTime: '',
  examEndTime: '',
  maxAttempts: 0,
  antiCheat: 0
})

// 年级列表（从configStore获取）
const gradeList = computed(() => configStore.gradeList)

// 获取学科名称
function getSubjectName(subjectId) {
  const subject = subjectList.value.find(s => s.id === subjectId)
  return subject ? subject.subjectName : '-'
}

// 获取试卷列表
async function fetchPaperList() {
  try {
    const res = await request.get('/exam/paper/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        paperName: searchForm.paperName || undefined,
        subjectId: searchForm.subjectId || undefined,
        status: searchForm.status !== '' ? searchForm.status : undefined
      }
    })
    if (res.data.code === 200) {
      paperList.value = res.data.data.rows || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('获取试卷列表失败', e)
    toast.error('获取试卷列表失败')
  }
}

// 获取学科列表
async function fetchSubjectList() {
  try {
    const res = await request.get('/exam/subject/list')
    if (res.data.code === 200) {
      subjectList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取学科列表失败', e)
  }
}

// 获取题库列表
async function fetchQuestionList() {
  try {
    const res = await request.get('/exam/question/page', {
      params: {
        pageNum: 1,
        pageSize: 100
      }
    })
    if (res.data.code === 200) {
      questionList.value = res.data.data.rows || []
    }
  } catch (e) {
    console.error('获取题库失败', e)
  }
}

// 搜索
function handleSearch() {
  pageNum.value = 1
  fetchPaperList()
}

// 重置搜索
function resetSearch() {
  searchForm.paperName = ''
  searchForm.subjectId = ''
  searchForm.status = ''
  handleSearch()
}

// 分页
function handlePageChange(newPage) {
  pageNum.value = newPage
  fetchPaperList()
}

function handleSizeChange() {
  pageNum.value = 1
  fetchPaperList()
}

// 打开新增弹窗
function openAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function handleEdit(paper) {
  isEdit.value = true
  Object.assign(form, {
    id: paper.id,
    name: paper.name,
    subjectId: paper.subjectId,
    gradeId: paper.gradeId || null,
    paperType: paper.paperType || 1,
    totalScore: paper.totalScore || 100,
    passScore: paper.passScore || 60,
    duration: paper.duration || 120,
    description: paper.description || '',
    status: paper.status,
    examStartTime: paper.examStartTime || '',
    examEndTime: paper.examEndTime || '',
    maxAttempts: paper.maxAttempts || 0,
    antiCheat: paper.antiCheat || 0
  })
  showModal.value = true
}

// 关闭弹窗
function closeModal() {
  showModal.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    subjectId: null,
    gradeId: null,
    paperType: 1,
    totalScore: 100,
    passScore: 60,
    duration: 120,
    description: '',
    status: 0,
    examStartTime: '',
    examEndTime: '',
    maxAttempts: 0,
    antiCheat: 0
  })
}

// 提交表单
async function handleSubmit() {
  submitting.value = true
  try {
    const data = { ...form }

    if (isEdit.value) {
      const res = await request.put('/exam/paper', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeModal()
        fetchPaperList()
      }
    } else {
      const res = await request.post('/exam/paper', data)
      if (res.data.code === 200) {
        toast.success('新增成功')
        closeModal()
        fetchPaperList()
      }
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除试卷
async function handleDelete(paper) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除试卷 <b>"${paper.name}"</b> 吗？<br><small style="color:#999">此操作不可恢复！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/exam/paper/${paper.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchPaperList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 打开组卷弹窗
async function handleQuestions(paper) {
  currentPaper.value = paper
  selectedQuestions.value = []
  assemblyMode.value = 'manual'
  previewResult.value = null
  assemblyConfig.subjectId = paper.subjectId
  assemblyConfig.grade = ''
  await fetchQuestionList()
  showQuestionModal.value = true
}

// 关闭组卷弹窗
function closeQuestionModal() {
  showQuestionModal.value = false
  currentPaper.value = null
  selectedQuestions.value = []
}

// 判断是否选中
function isQuestionSelected(questionId) {
  return selectedQuestions.value.some(q => q.id === questionId)
}

// 切换选题
function toggleQuestion(question) {
  const index = selectedQuestions.value.findIndex(q => q.id === question.id)
  if (index > -1) {
    selectedQuestions.value.splice(index, 1)
  } else {
    selectedQuestions.value.push(question)
  }
}

// 保存组卷
async function savePaperQuestions() {
  if (assemblyMode.value === 'manual') {
    if (selectedQuestions.value.length === 0) {
      toast.warning('请至少选择一道题目')
      return
    }
    submitting.value = true
    try {
      const questionIds = selectedQuestions.value.map(q => q.id)
      const scores = selectedQuestions.value.map(q => q.score || 10)
      const res = await request.post('/exam/paper/manual-assembly', {
        paperId: currentPaper.value.id,
        questionIds,
        scores
      })
      if (res.data.code === 200) {
        toast.success(`组卷成功！共选择 ${selectedQuestions.value.length} 道题目`)
        closeQuestionModal()
        fetchPaperList()
      }
    } catch (e) {
      toast.error('组卷失败')
    } finally {
      submitting.value = false
    }
  } else if (assemblyMode.value === 'auto') {
    // 按难度组卷
    if (totalQuestionCount.value === 0) {
      toast.warning('请至少配置一项难度配置')
      return
    }
    submitting.value = true
    try {
      const res = await request.post('/exam/paper/assemble', {
        paperId: currentPaper.value.id,
        subjectId: assemblyConfig.subjectId,
        grade: assemblyConfig.grade,
        difficultyConfigs: difficultyConfigs.value.map(c => ({
          difficulty: c.difficulty,
          questionType: c.questionType,
          count: c.count,
          score: c.score
        }))
      })
      if (res.data.code === 200) {
        toast.success(`组卷成功！共 ${res.data.data.totalCount} 道题目，总分 ${res.data.data.totalScore} 分`)
        closeQuestionModal()
        fetchPaperList()
      }
    } catch (e) {
      toast.error('组卷失败: ' + (e.response?.data?.msg || e.message))
    } finally {
      submitting.value = false
    }
  } else if (assemblyMode.value === 'topic') {
    // 专题训练组卷
    await saveTopicAssembly()
  }
}

// 添加难度配置
function addDifficultyConfig() {
  difficultyConfigs.value.push({ difficulty: 1, questionType: 1, count: 0, score: 2, availableCount: 0 })
}

// 移除难度配置
function removeDifficultyConfig(index) {
  difficultyConfigs.value.splice(index, 1)
}

// 更新可用题目数量
async function updateAvailableCount(config) {
  try {
    const res = await request.get('/exam/paper/available-count', {
      params: {
        subjectId: assemblyConfig.subjectId,
        grade: assemblyConfig.grade,
        difficulty: config.difficulty,
        questionType: config.questionType
      }
    })
    if (res.data.code === 200) {
      config.availableCount = res.data.data || 0
    }
  } catch (e) {
    console.error('获取可用题目数量失败', e)
  }
}

// 预览组卷
async function previewAssembly() {
  if (totalQuestionCount.value === 0) {
    toast.warning('请至少配置一项难度配置')
    return
  }
  submitting.value = true
  try {
    const res = await request.post('/exam/paper/preview', {
      paperId: currentPaper.value.id,
      subjectId: assemblyConfig.subjectId,
      grade: assemblyConfig.grade,
      difficultyConfigs: difficultyConfigs.value.map(c => ({
        difficulty: c.difficulty,
        questionType: c.questionType,
        count: c.count,
        score: c.score
      }))
    })
    if (res.data.code === 200) {
      previewResult.value = res.data.data
      toast.success('预览成功')
    }
  } catch (e) {
    toast.error('预览失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    submitting.value = false
  }
}

// 加载知识点列表
async function loadKnowledgePoints() {
  if (!topicConfig.subjectId) {
    knowledgePointList.value = []
    return
  }
  try {
    const res = await request.get('/exam/paper/knowledge-points', {
      params: { subjectId: topicConfig.subjectId }
    })
    if (res.data.code === 200) {
      knowledgePointList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载知识点失败', e)
    knowledgePointList.value = []
  }
}

// 专题训练组卷
async function saveTopicAssembly() {
  if (topicConfig.selectedPoints.length === 0) {
    toast.warning('请至少选择一个知识点')
    return
  }
  submitting.value = true
  try {
    const res = await request.post('/exam/paper/assemble-by-kp', {
      paperId: currentPaper.value.id,
      subjectId: topicConfig.subjectId,
      grade: topicConfig.grade,
      knowledgePoints: topicConfig.selectedPoints,
      countPerPoint: topicConfig.countPerPoint,
      score: topicConfig.score
    })
    if (res.data.code === 200) {
      toast.success(`专题训练组卷成功！共 ${res.data.data.totalCount} 道题目，总分 ${res.data.data.totalScore} 分`)
      closeQuestionModal()
      fetchPaperList()
    }
  } catch (e) {
    toast.error('组卷失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    submitting.value = false
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 打开导出弹窗
function handleExport(paper) {
  currentPaper.value = paper
  showExportModal.value = true
}

// 关闭导出弹窗
function closeExportModal() {
  showExportModal.value = false
}

// 执行导出
function doExport(type) {
  if (!currentPaper.value) return
  const url = `/exam/paper/export/${type}/${currentPaper.value.id}`
  window.open(url, '_blank')
  closeExportModal()
}

// 在线打印
async function doPrint() {
  if (!currentPaper.value) return
  try {
    const res = await request.get(`/exam/paper/print/${currentPaper.value.id}`)
    if (res.data.code === 200) {
      const printData = res.data.data
      // 创建打印窗口
      const printWindow = window.open('', '_blank')
      const printContent = generatePrintContent(printData)
      printWindow.document.write(printContent)
      printWindow.document.close()
      printWindow.print()
    }
  } catch (e) {
    toast.error('获取打印数据失败')
  }
  closeExportModal()
}

// 生成打印内容
function generatePrintContent(data) {
  const paper = data.paper
  const questions = data.questions || []

  const typeNames = { 1: '一、单选题', 2: '二、多选题', 3: '三、判断题', 4: '四、填空题', 5: '五、简答题' }

  // 按题型分组
  const grouped = {}
  questions.forEach(q => {
    const type = q.questionType
    if (!grouped[type]) grouped[type] = []
    grouped[type].push(q)
  })

  let questionsHtml = ''
  let num = 1
  for (const [type, qs] of Object.entries(grouped)) {
    questionsHtml += `<h3 style="margin-top:20px;">${typeNames[type] || '其他题目'}</h3>`
    qs.forEach(q => {
      questionsHtml += `<p style="margin:10px 0;"><b>${num}.</b> ${q.questionTitle} <small>(${q.score}分)</small></p>`
      if (q.options && (type === '1' || type === '2')) {
        const opts = q.options.split('|||')
        let optChar = 'A'
        opts.forEach(opt => {
          questionsHtml += `<p style="margin-left:20px;color:#666;">${optChar}. ${opt}</p>`
          optChar = String.fromCharCode(optChar.charCodeAt(0) + 1)
        })
      }
      num++
    })
  }

  return `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>${paper.name}</title>
  <style>
    body { font-family: 'SimSun', serif; padding: 40px; }
    h1 { text-align: center; margin-bottom: 10px; }
    .info { text-align: center; color: #666; margin-bottom: 30px; }
    h3 { border-bottom: 1px solid #333; padding-bottom: 5px; }
  </style>
</head>
<body>
  <h1>${paper.name}</h1>
  <div class="info">总分：${paper.totalScore}分 | 时长：${paper.duration}分钟 | 及格分：${paper.passScore}分</div>
  ${questionsHtml}
</body>
</html>
  `
}

onMounted(async () => {
  await configStore.loadConfigs()
  fetchPaperList()
  fetchSubjectList()
})
</script>

<style scoped>
.exam-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.btn {
  padding: 10px 25px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
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

.search-input,
.search-select {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.search-input {
  width: 200px;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-0 {
  background: #f0f0f0;
  color: #666;
}

.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.status-2 {
  background: #e6f7ff;
  color: #1890ff;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.action-btn:hover { opacity: 0.85; transform: translateY(-1px); }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-warn { background: #fff7e6; color: #fa8c16; }
.action-warn:hover { background: #fa8c16; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

.pagination { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-top: 1px solid #f0f0f0; background: #fafbfc; border-radius: 0 0 12px 12px; }
.pagination-left, .pagination-center, .pagination-right { display: flex; align-items: center; gap: 8px; }
.total { font-size: 13px; color: #666; }
.total em { font-style: normal; color: #667eea; font-weight: 600; }
.page-btn { display: inline-flex; align-items: center; gap: 4px; padding: 8px 14px; border: 1px solid #e8e8e8; background: #fff; border-radius: 8px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.25s; }
.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.page-btn:first-child, .page-btn:last-child { padding: 8px 10px; }
.page-info { padding: 0 12px; font-size: 13px; color: #666; }
.page-info strong { color: #667eea; font-size: 15px; font-weight: 600; }
.page-size { padding: 8px 32px 8px 12px; border: 1px solid #e8e8e8; border-radius: 8px; font-size: 13px; color: #666; background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L2 4h8z'/%3E%3C/svg%3E") no-repeat right 10px center; cursor: pointer; outline: none; appearance: none; transition: all 0.2s; }
.page-size:hover { border-color: #667eea; }
.page-size:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }

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

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
}

.modal-large {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 25px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.required {
  color: #ff4d4f;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 组卷样式 */
.question-pool {
  max-height: 500px;
  overflow-y: auto;
}

.pool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.selected-count {
  color: #667eea;
  font-weight: 600;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-item {
  padding: 15px;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: #667eea;
}

.question-item.selected {
  border-color: #52c41a;
  background: #f6ffed;
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.question-type {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-score {
  color: #ff4d4f;
  font-size: 12px;
}

.question-title {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

/* 组卷模式切换 */
.assembly-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 按难度组卷样式 */
.auto-assembly {
  padding: 10px 0;
}

.assembly-config {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.config-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.config-row label {
  font-size: 14px;
  color: #666;
}

.config-row select {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  min-width: 150px;
}

.difficulty-table {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.difficulty-table .table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 80px 80px 80px 80px 40px;
  gap: 10px;
  padding: 12px 15px;
  background: #f8f9fa;
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

.difficulty-table .table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 80px 80px 80px 80px 40px;
  gap: 10px;
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  align-items: center;
}

.difficulty-table select,
.difficulty-table input {
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
}

.available-count {
  text-align: center;
  color: #52c41a;
  font-weight: 600;
}

.available-count.warning {
  color: #ff4d4f;
}

.subtotal {
  text-align: center;
  color: #667eea;
  font-weight: 600;
}

.remove-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #fff1f0;
  color: #ff4d4f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.remove-btn:hover {
  background: #ff4d4f;
  color: white;
}

.add-config-btn {
  width: 100%;
  padding: 12px;
  border: 2px dashed #e0e0e0;
  background: white;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  margin-top: 10px;
}

.add-config-btn:hover {
  border-color: #667eea;
  background: #f5f4ff;
}

.assembly-stats {
  display: flex;
  gap: 30px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.stat-value {
  color: #667eea;
  font-size: 18px;
  font-weight: 700;
}

.preview-result {
  padding: 15px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
}

.preview-result h4 {
  margin: 0 0 10px;
  color: #52c41a;
}

.preview-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.question-difficulty {
  background: #fff7e6;
  color: #fa8c16;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.btn-info {
  background: #1890ff;
  color: white;
}

/* 专题训练样式 */
.topic-assembly {
  padding: 10px 0;
}

.knowledge-points-section {
  margin: 20px 0;
}

.knowledge-points-section h4 {
  margin: 0 0 15px;
  font-size: 14px;
  color: #333;
}

.knowledge-points-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.kp-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.kp-item:hover {
  border-color: #667eea;
}

.kp-item input:checked + span {
  color: #667eea;
  font-weight: 600;
}

.kp-item input:checked {
  accent-color: #667eea;
}

.empty-tip {
  color: #999;
  font-size: 13px;
  padding: 20px;
  text-align: center;
}

.topic-config-row {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.config-item label {
  font-size: 14px;
  color: #666;
}

.config-item input {
  width: 80px;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
}

/* 导出弹窗样式 */
.modal-small {
  max-width: 400px;
}

.export-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 16px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
}

.export-btn:hover {
  border-color: #667eea;
  background: #f5f4ff;
}

.export-icon {
  font-size: 28px;
}

.export-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.export-desc {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.action-export {
  background: #f0f5ff;
  color: #2f54eb;
}
.action-export:hover {
  background: #2f54eb;
  color: #fff;
}
</style>
