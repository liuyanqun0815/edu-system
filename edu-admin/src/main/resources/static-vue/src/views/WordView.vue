<template>
  <div class="word-page">
    <div class="page-header">
      <h1 class="page-title">单词训练</h1>
      <div class="header-actions">
        <button class="btn btn-success" @click="goToTraining">🎯 AI智能训练</button>
        <button class="btn btn-success" @click="goToAIImport">🤖 AI智能导入</button>
        <button class="btn btn-default" @click="downloadTemplate">📥 下载模板</button>
        <button class="btn btn-info" @click="openImportModal">📤 批量导入</button>
        <button class="btn btn-primary" @click="openAddModal">+ 添加单词</button>
      </div>
    </div>

    <div class="search-bar">
      <input v-model="searchForm.word" type="text" class="search-input" placeholder="英文单词" @keyup.enter="handleSearch">
      <select v-model="searchForm.grade" class="search-select">
        <option value="">全部年级</option>
        <option v-for="grade in gradeOptions" :key="grade.itemValue" :value="grade.itemName || grade.itemLabel">{{ grade.itemName || grade.itemLabel }}</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>单词</th>
            <th>音标</th>
            <th>中文释义</th>
            <th>年级</th>
            <th>难度</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="word in wordList" :key="word.id">
            <td>{{ word.id }}</td>
            <td class="word-text">{{ word.word }}</td>
            <td>{{ word.phonetic || '-' }}</td>
            <td>{{ word.translation }}</td>
            <td>{{ word.grade }}</td>
            <td>{{ difficultyMap[word.difficulty] }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(word)">编辑</button>
              <button class="action-btn action-delete" @click="handleDelete(word)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span class="total">共 {{ total }} 条</span>
      <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">上一页</button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">下一页</button>
      <select v-model="pageSize" class="page-size" @change="handleSizeChange">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑单词' : '添加单词' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>英文单词 <span class="required">*</span></label>
              <input v-model="form.word" type="text" placeholder="请输入英文单词" required>
            </div>
            <div class="form-group">
              <label>音标</label>
              <input v-model="form.phonetic" type="text" placeholder="如：/həˈləʊ/">
            </div>
            <div class="form-group">
              <label>中文释义 <span class="required">*</span></label>
              <input v-model="form.translation" type="text" placeholder="请输入中文释义" required>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>年级</label>
                <select v-model="form.grade">
                  <option v-for="grade in gradeOptions" :key="grade.itemValue" :value="grade.itemName || grade.itemLabel">{{ grade.itemName || grade.itemLabel }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>难度</label>
                <select v-model="form.difficulty">
                  <option v-for="diff in difficultyOptions" :key="diff.itemValue" :value="parseInt(diff.itemValue)">{{ diff.itemName || diff.itemLabel }}</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>例句</label>
              <textarea v-model="form.example" rows="2" placeholder="请输入例句"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 导入弹窗 -->
    <div v-if="showImportModal" class="modal-overlay" @click.self="closeImportModal">
      <div class="modal">
        <div class="modal-header">
          <h3>批量导入单词</h3>
          <button class="close-btn" @click="closeImportModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="import-tips">
            <p>1. 请先下载模板，按模板格式填写单词数据</p>
            <p>2. 英文单词和中文释义为必填项</p>
            <p>3. 难度：1-简单，2-中等，3-困难</p>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const { toast, confirm: confirmDialog } = useMessage()
const configStore = useConfigStore()

// 动态配置
const gradeOptions = computed(() => configStore.gradeList)
const difficultyOptions = computed(() => configStore.difficultyLevels)
const difficultyMap = computed(() => configStore.difficultyMap)
const searchForm = reactive({ word: '', grade: '' })
const wordList = ref([])
const showModal = ref(false)
const showImportModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const fileInput = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)
const form = reactive({ id: null, word: '', phonetic: '', translation: '', grade: '六年级', difficulty: 1, example: '' })

async function fetchWordList() {
  try {
    const res = await request.get('/system/word/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value, word: searchForm.word || undefined, grade: searchForm.grade || undefined } })
    if (res.data.code === 200) {
      wordList.value = res.data.data?.rows || res.data.data || []
      total.value = res.data.data?.total || 0
    }
  } catch (e) { console.error('获取单词列表失败', e) }
}

function handleSearch() { pageNum.value = 1; fetchWordList() }
function resetSearch() { searchForm.word = ''; searchForm.grade = ''; handleSearch() }

function handlePageChange(p) { pageNum.value = p; fetchWordList() }
function handleSizeChange() { pageNum.value = 1; fetchWordList() }

function openAddModal() {
  isEdit.value = false
  Object.assign(form, { id: null, word: '', phonetic: '', translation: '', grade: '六年级', difficulty: 1, example: '' })
  showModal.value = true
}

function handleEdit(word) {
  isEdit.value = true
  Object.assign(form, { ...word })
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/system/word', form)
      toast.success('修改成功')
    } else {
      await request.post('/system/word', form)
      toast.success('添加成功')
    }
    closeModal()
    fetchWordList()
  } catch (e) { toast.error(e.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(word) {
  const confirmed = await confirmDialog({
    title: '删除确认',
    message: `确定要删除单词 <b>"${word.word}"</b> 吗？`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/system/word/${word.id}`)
    toast.success('删除成功')
    fetchWordList()
  } catch (e) { toast.error(e.response?.data?.msg || '删除失败') }
}

// 下载模板
function downloadTemplate() {
  window.open('/system/word/template', '_blank')
}

// 跳转到AI导入页面
function goToAIImport() {
  router.push({ path: '/ai-import' })
}

// 跳转到AI训练页面
function goToTraining() {
  router.push({ name: 'word-training' })
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

    const res = await request.post('/system/word/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.code === 200) {
      importResult.value = res.data.data
      if (res.data.data.successCount > 0) {
        fetchWordList()
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
  fetchWordList()
})
</script>

<style scoped>
.word-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }
.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-default { background: #f0f0f0; color: #666; }
.btn-info { background: #1890ff; color: white; }
.btn-success { background: #52c41a; color: white; }
.btn-danger { background: #ff4d4f; color: white; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.search-input, .search-select { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; }
.table-container { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.data-table th { background: #f8f9fa; font-weight: 600; color: #333; }
.word-text { font-weight: 600; color: #667eea; }
.actions { display: flex; gap: 8px; }
.action-btn { padding: 4px 10px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.action-btn:hover { opacity: 0.85; }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }
.pagination { display: flex; align-items: center; justify-content: flex-end; gap: 12px; padding: 20px; background: white; border-radius: 0 0 12px 12px; border-top: 1px solid #f0f0f0; }
.total { color: #666; font-size: 14px; }
.page-btn { padding: 6px 14px; border: 1px solid #e0e0e0; background: white; border-radius: 6px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; font-size: 13px; }
.page-size { padding: 6px 10px; border: 1px solid #e0e0e0; border-radius: 6px; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 500px; max-height: 90vh; overflow: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #333; }
.required { color: #ff4d4f; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
</style>
