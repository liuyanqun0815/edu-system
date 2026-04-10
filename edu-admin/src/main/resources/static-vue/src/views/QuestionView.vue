<template>
  <div class="question-page">
    <div class="page-header">
      <h1 class="page-title">题库管理</h1>
      <div class="header-actions">
        <button class="btn btn-default" @click="downloadTemplate">📥 下载模板</button>
        <button class="btn btn-info" @click="openImportModal">📤 批量导入</button>
        <button class="btn btn-primary" @click="openAddModal">+ 新建题目</button>
      </div>
    </div>

    <div class="search-bar">
      <input v-model="searchForm.questionTitle" type="text" class="search-input" placeholder="题目内容" @keyup.enter="handleSearch">
      <select v-model="searchForm.questionType" class="search-select">
        <option value="">全部类型</option>
        <option v-for="type in questionTypeOptions" :key="type.itemValue" :value="parseInt(type.itemValue)">{{ type.itemLabel || type.itemName }}</option>
      </select>
      <select v-model="searchForm.subjectId" class="search-select">
        <option value="">全部学科</option>
        <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">{{ subject.subjectName }}</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>题目内容</th>
            <th>类型</th>
            <th>学科</th>
            <th>难度</th>
            <th>分值</th>
            <th>标签</th>
            <th>来源</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="question in questionList" :key="question.id">
            <td>{{ question.id }}</td>
            <td class="question-title-cell">{{ question.questionTitle }}</td>
            <td><span :class="['type-tag', `type-${question.questionType}`]">{{ typeMap[question.questionType] }}</span></td>
            <td>{{ getSubjectName(question.subjectId) }}</td>
            <td><span :class="['difficulty-tag', `difficulty-${question.difficulty}`]">{{ difficultyMap[question.difficulty] }}</span></td>
            <td>{{ question.score }}分</td>
            <td class="tags-cell">
              <span v-if="question.tags" class="tag-item">{{ question.tags }}</span>
              <span v-else class="tag-empty">-</span>
            </td>
            <td>
              <span :class="['source-tag', `source-${question.sourceType}`]">
                {{ sourceTypeMap[question.sourceType] || '手动' }}
              </span>
            </td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(question)">编辑</button>
              <button class="action-btn action-delete" @click="handleDelete(question)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <div class="pagination-left">
          <span class="total-info">共 <em>{{ total }}</em> 条</span>
        </div>
        <div class="pagination-center">
          <button :disabled="pageNum <= 1" class="page-btn nav-btn" @click="handlePageChange(1)" title="首页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/>
            </svg>
          </button>
          <button :disabled="pageNum <= 1" class="page-btn nav-btn" @click="handlePageChange(pageNum - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
            <span>上一页</span>
          </button>
          <span class="page-info"><strong>{{ pageNum }}</strong> / {{ totalPages }}</span>
          <button :disabled="pageNum >= totalPages" class="page-btn nav-btn" @click="handlePageChange(pageNum + 1)">
            <span>下一页</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6"/>
            </svg>
          </button>
          <button :disabled="pageNum >= totalPages" class="page-btn nav-btn" @click="handlePageChange(totalPages)" title="末页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 17l5-5-5-5M6 17l5-5-5-5"/>
            </svg>
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

    <!-- 导入弹窗 -->
    <div v-if="showImportModal" class="modal-overlay" @click.self="closeImportModal">
      <div class="modal">
        <div class="modal-header">
          <h3>批量导入题目</h3>
          <button class="close-btn" @click="closeImportModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="import-tips">
            <p>1. 请先下载模板，按模板格式填写题目数据</p>
            <p>2. 题目内容、题型、正确答案为必填项</p>
            <p>3. 题型：1-单选，2-多选，3-判断，4-填空，5-简答</p>
            <p>4. 选项格式：A.xxx B.xxx C.xxx（用空格分隔）</p>
          </div>
          <div class="upload-area" @click="triggerUpload" :class="{ 'has-file': importFile }">
            <input type="file" ref="fileInput" accept=".xlsx,.xls" @change="handleFileChange" style="display:none">
            <template v-if="importFile">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ importFile.name }}</span>
            </template>
            <template v-else>
              <span class="upload-icon">📁</span>
              <span class="upload-text">点击选择Excel文件</span>
            </template>
          </div>
          <div v-if="importResult" class="import-result">
            <p class="success">成功导入 {{ importResult.successCount }} 条</p>
            <p v-if="importResult.errorCount > 0" class="error">失败 {{ importResult.errorCount }} 条</p>
            <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list">
              <p v-for="err in importResult.errors" :key="err">{{ err }}</p>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closeImportModal">取消</button>
            <button type="button" class="btn btn-primary" :disabled="!importFile || uploading" @click="doImport">
              {{ uploading ? '导入中...' : '确认导入' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑题目' : '新建题目' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>题目内容 <span class="required">*</span></label>
              <textarea v-model="form.questionTitle" rows="3" placeholder="请输入题目内容" required></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>题目类型 <span class="required">*</span></label>
                <select v-model="form.questionType" required>
                  <option v-for="type in questionTypeOptions" :key="type.itemValue" :value="parseInt(type.itemValue)">{{ type.itemLabel || type.itemName }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>学科 <span class="required">*</span></label>
                <select v-model="form.subjectId" required>
                  <option v-for="subject in subjectList" :key="subject.id" :value="subject.id">{{ subject.subjectName }}</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>难度</label>
                <select v-model="form.difficulty">
                  <option v-for="diff in difficultyOptions" :key="diff.itemValue" :value="parseInt(diff.itemValue)">{{ diff.itemName || diff.itemLabel }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>分值</label>
                <input v-model.number="form.score" type="number" placeholder="请输入分值">
              </div>
            </div>
            <div class="form-group">
              <label>正确答案</label>
              <input v-model="form.answer" type="text" placeholder="请输入正确答案">
            </div>
            <div class="form-group">
              <label>答案解析</label>
              <textarea v-model="form.analysis" rows="2" placeholder="请输入答案解析"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '保存中...' : '保存' }}</button>
            </div>
          </form>
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

// 动态配置
const questionTypeOptions = computed(() => configStore.questionTypes)
const difficultyOptions = computed(() => configStore.difficultyLevels)
const typeMap = computed(() => configStore.questionTypeMap)
const difficultyMap = computed(() => configStore.difficultyMap)
const sourceTypeMap = { 1: '手动', 2: '导入', 3: 'AI' }

const searchForm = reactive({ questionTitle: '', questionType: '', subjectId: '' })
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)
const questionList = ref([])
const subjectList = ref([])
const showModal = ref(false)
const showImportModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const fileInput = ref(null)
const form = reactive({ id: null, questionTitle: '', questionType: 1, subjectId: null, difficulty: 2, score: 10, answer: '', analysis: '' })

function getSubjectName(subjectId) {
  const subject = subjectList.value.find(s => s.id === subjectId)
  return subject ? subject.subjectName : '-'
}

async function fetchQuestionList() {
  try {
    const res = await request.get('/exam/question/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, questionTitle: searchForm.questionTitle || undefined, questionType: searchForm.questionType || undefined, subjectId: searchForm.subjectId || undefined }
    })
    if (res.data.code === 200) {
      questionList.value = res.data.data.rows || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error('获取题目列表失败', e) }
}

async function fetchSubjectList() {
  try {
    const res = await request.get('/exam/subject/list')
    if (res.data.code === 200) subjectList.value = res.data.data || []
  } catch (e) { console.error('获取学科列表失败', e) }
}

function handleSearch() { pageNum.value = 1; fetchQuestionList() }
function resetSearch() { searchForm.questionTitle = ''; searchForm.questionType = ''; searchForm.subjectId = ''; handleSearch() }
function handlePageChange(newPage) { pageNum.value = newPage; fetchQuestionList() }
function handleSizeChange() { pageNum.value = 1; fetchQuestionList() }

function openAddModal() {
  isEdit.value = false
  Object.assign(form, { id: null, questionTitle: '', questionType: 1, subjectId: subjectList.value[0]?.id, difficulty: 2, score: 10, answer: '', analysis: '' })
  showModal.value = true
}

function handleEdit(question) {
  isEdit.value = true
  Object.assign(form, { ...question })
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/exam/question', form)
      toast.success('修改成功')
    } else {
      await request.post('/exam/question', form)
      toast.success('新增成功')
    }
    closeModal()
    fetchQuestionList()
  } catch (e) { toast.error(e.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(question) {
  const confirmed = await confirm({
    title: '删除确认',
    message: '确定要删除该题目吗？',
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/exam/question/${question.id}`)
    toast.success('删除成功')
    fetchQuestionList()
  } catch (e) { toast.error(e.response?.data?.msg || '删除失败') }
}

// 下载模板
function downloadTemplate() {
  window.open('/exam/question/template', '_blank')
}

// 打开导入弹窗
function openImportModal() {
  importFile.value = null
  importResult.value = null
  showImportModal.value = true
}

// 关闭导入弹窗
function closeImportModal() {
  showImportModal.value = false
}

// 触发文件选择
function triggerUpload() {
  fileInput.value?.click()
}

// 文件选择变化
function handleFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    importFile.value = file
    importResult.value = null
  }
}

// 执行导入
async function doImport() {
  if (!importFile.value) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)

    const res = await request.post('/exam/question/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.code === 200) {
      importResult.value = res.data.data
      if (res.data.data.successCount > 0) {
        fetchQuestionList()
      }
    }
  } catch (e) {
    toast.error('导入失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    uploading.value = false
  }
}

onMounted(async () => {
  await configStore.loadConfigs()
  fetchQuestionList()
  fetchSubjectList()
})
</script>

<style scoped>
.question-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }
.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-default { background: #f0f0f0; color: #666; }
.btn-danger { background: #ff4d4f; color: white; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.search-input, .search-select { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; }
.search-input { width: 200px; }
.table-container { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.data-table th { background: #f8f9fa; font-weight: 600; color: #333; }
.question-title-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.type-tag { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.type-1 { background: #e6f7ff; color: #1890ff; }
.type-2 { background: #f6ffed; color: #52c41a; }
.type-3 { background: #fff7e6; color: #fa8c16; }
.type-4 { background: #f9f0ff; color: #722ed1; }
.type-5 { background: #fff0f6; color: #eb2f96; }
.difficulty-tag { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.difficulty-1 { background: #f6ffed; color: #52c41a; }
.difficulty-2 { background: #fff7e6; color: #fa8c16; }
.difficulty-3 { background: #fff0f6; color: #eb2f96; }
.tags-cell { max-width: 150px; }
.tag-item { display: inline-block; padding: 2px 8px; background: #f0f0f0; border-radius: 4px; font-size: 12px; color: #666; }
.tag-empty { color: #999; font-size: 12px; }
.source-tag { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.source-1 { background: #e6f7ff; color: #1890ff; }
.source-2 { background: #f6ffed; color: #52c41a; }
.source-3 { background: #f9f0ff; color: #722ed1; }
.difficulty-3 { background: #fff1f0; color: #ff4d4f; }
.actions { display: flex; gap: 8px; }
.action-btn { padding: 4px 10px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.action-btn:hover { opacity: 0.85; }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }
.pagination { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-top: 1px solid #f0f0f0; background: #fafbfc; border-radius: 0 0 12px 12px; }
.pagination-left, .pagination-center, .pagination-right { display: flex; align-items: center; gap: 8px; }
.total-info { font-size: 13px; color: #666; }
.total-info em { font-style: normal; color: #667eea; font-weight: 600; }
.page-btn { display: inline-flex; align-items: center; gap: 4px; padding: 8px 14px; border: 1px solid #e8e8e8; background: #fff; border-radius: 8px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.25s; }
.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.page-btn.nav-btn:first-child, .page-btn.nav-btn:last-child { padding: 8px 10px; }
.page-info { padding: 0 12px; font-size: 13px; color: #666; }
.page-info strong { color: #667eea; font-size: 15px; font-weight: 600; }
.page-size { padding: 8px 32px 8px 12px; border: 1px solid #e8e8e8; border-radius: 8px; font-size: 13px; color: #666; background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L2 4h8z'/%3E%3C/svg%3E") no-repeat right 10px center; cursor: pointer; outline: none; appearance: none; transition: all 0.2s; }
.page-size:hover { border-color: #667eea; }
.page-size:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 600px; max-height: 90vh; overflow: auto; }
.modal-large { max-width: 700px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #333; }
.required { color: #ff4d4f; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-group textarea { resize: vertical; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.header-actions { display: flex; gap: 10px; }
.btn-info { background: linear-gradient(135deg, #17a2b8 0%, #138496 100%); color: white; }
.import-tips { background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.import-tips p { margin: 5px 0; font-size: 13px; color: #666; }
.upload-area { border: 2px dashed #d9d9d9; border-radius: 8px; padding: 40px; text-align: center; cursor: pointer; transition: all 0.3s; }
.upload-area:hover { border-color: #667eea; background: #f5f7ff; }
.upload-area.has-file { border-color: #52c41a; background: #f6ffed; }
.upload-icon { font-size: 48px; display: block; margin-bottom: 10px; }
.upload-text { color: #999; }
.file-icon { font-size: 36px; display: block; margin-bottom: 8px; }
.file-name { color: #333; font-weight: 500; }
.import-result { margin-top: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px; }
.import-result .success { color: #52c41a; font-weight: 500; margin: 0; }
.import-result .error { color: #ff4d4f; margin: 8px 0 0 0; }
.error-list { margin-top: 10px; max-height: 150px; overflow-y: auto; }
.error-list p { margin: 4px 0; font-size: 12px; color: #ff4d4f; }
</style>
