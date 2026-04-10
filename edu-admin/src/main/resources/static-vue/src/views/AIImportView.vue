<template>
  <div class="ai-import-page">
    <div class="page-header">
      <h1 class="page-title">AI智能导入</h1>
      <button class="btn btn-default" @click="goBack">← 返回</button>
    </div>

    <div class="import-container">
      <!-- 步骤指示器 -->
      <div class="steps">
        <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-number">1</div>
          <div class="step-title">上传文档</div>
        </div>
        <div class="step-line" :class="{ active: currentStep > 1 }"></div>
        <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-number">2</div>
          <div class="step-title">AI解析</div>
        </div>
        <div class="step-line" :class="{ active: currentStep > 2 }"></div>
        <div class="step" :class="{ active: currentStep >= 3 }">
          <div class="step-number">3</div>
          <div class="step-title">确认导入</div>
        </div>
      </div>

      <!-- 步骤1: 上传文档 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="upload-section">
          <h3>选择导入类型</h3>
          <div class="type-selector">
            <button 
              class="type-btn" 
              :class="{ active: documentType === 'word' }"
              @click="documentType = 'word'">
              📚 单词导入
            </button>
            <button 
              class="type-btn" 
              :class="{ active: documentType === 'question' }"
              @click="documentType = 'question'">
              📝 题库导入
            </button>
          </div>

          <h3 style="margin-top: 30px;">上传文档</h3>
          <div class="upload-area" @click="triggerUpload" :class="{ 'has-file': uploadFile }">
            <input type="file" ref="fileInput" accept=".pdf,.doc,.docx" @change="handleFileChange" style="display:none">
            <template v-if="uploadFile">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ uploadFile.name }}</span>
              <span class="file-size">{{ formatFileSize(uploadFile.size) }}</span>
            </template>
            <template v-else>
              <span class="upload-icon">📁</span>
              <span class="upload-text">点击选择PDF或Word文档</span>
              <span class="upload-hint">支持 .pdf, .doc, .docx 格式</span>
            </template>
          </div>

          <div class="format-tips">
            <h4>📖 文档格式要求:</h4>
            <div v-if="documentType === 'word'">
              <p><strong>单词文档格式:</strong></p>
              <ul>
                <li>abandon [ə'bændən] v.放弃</li>
                <li>ability [ə'bɪləti] n.能力；才能</li>
                <li>able ['eɪbl] adj.能够</li>
              </ul>
            </div>
            <div v-else>
              <p><strong>题目文档格式:</strong></p>
              <ul>
                <li>What is your name? A.Tom B.Jack C.Lily 答案:A</li>
                <li>中国的首都是___ 答案:北京</li>
              </ul>
            </div>
          </div>

          <button class="btn btn-primary btn-lg" @click="handleParse" :disabled="!uploadFile || parsing">
            {{ parsing ? '解析中...' : '开始AI解析' }}
          </button>
        </div>
      </div>

      <!-- 步骤2: 解析结果预览 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="parse-result">
          <div class="result-header">
            <h3>解析结果 (可编辑)</h3>
            <div class="result-stats">
              <span class="stat success">✓ 成功 {{ parseResult.totalCount }} 条</span>
              <span v-if="parseResult.warnings && parseResult.warnings.length > 0" class="stat warning">
                ⚠ 警告 {{ parseResult.warnings.length }} 条
              </span>
              <span v-if="hasEmptyAnswers" class="stat danger">
                ⚠ 缺少答案 {{ emptyAnswerCount }} 条
              </span>
            </div>
          </div>

          <div v-if="parseResult.warnings && parseResult.warnings.length > 0" class="warnings">
            <h4>⚠️ 解析警告:</h4>
            <ul>
              <li v-for="(warning, index) in parseResult.warnings.slice(0, 5)" :key="index">
                {{ warning }}
              </li>
              <li v-if="parseResult.warnings.length > 5">
                ... 还有 {{ parseResult.warnings.length - 5 }} 条警告
              </li>
            </ul>
          </div>

          <div v-if="hasEmptyAnswers" class="answer-reminder">
            <h4>💡 提示:</h4>
            <p>检测到 {{ emptyAnswerCount }} 道题目缺少答案,请点击编辑补充答案后再导入</p>
          </div>

          <div class="data-preview">
            <div class="preview-header">
              <h4>📋 数据预览 (可编辑):</h4>
              <div class="preview-actions">
                <button v-if="documentType === 'question'" class="btn btn-sm btn-default" @click="showBatchAnswerModal = true">
                  批量设置答案
                </button>
                <button class="btn btn-sm btn-default" @click="toggleExpandAll">
                  {{ expandAll ? '收起全部' : '展开全部' }}
                </button>
              </div>
            </div>
            
            <div class="editable-table">
              <table>
                <thead>
                  <tr>
                    <th style="width: 50px;">#</th>
                    <th v-for="(value, key) in parseResult.dataList[0]" :key="key" :class="{ 'required-field': isRequiredField(key) }">
                      {{ getFieldLabel(key) }}
                      <span v-if="isRequiredField(key)" class="required-mark">*</span>
                    </th>
                    <th style="width: 80px;">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in parseResult.dataList" :key="index" :class="{ 'has-error': hasError(item) }">
                    <td>{{ index + 1 }}</td>
                    <td v-for="(value, key) in item" :key="key" class="editable-cell">
                      <!-- 题目内容可编辑 -->
                      <template v-if="key === 'questionTitle'">
                        <textarea 
                          v-if="editingCell === `${index}-${key}`" 
                          v-model="item[key]" 
                          class="cell-input cell-textarea"
                          @blur="finishEdit"
                          @keyup.escape="finishEdit"
                          rows="2"
                          autofocus
                        ></textarea>
                        <span v-else class="cell-text" @click="startEdit(index, key)" :title="value">
                          {{ value }}
                        </span>
                      </template>
                      
                      <!-- 答案字段特殊处理 -->
                      <template v-else-if="key === 'correctAnswer'">
                        <select 
                          v-if="editingCell === `${index}-${key}`"
                          v-model="item[key]" 
                          class="cell-input cell-select"
                          @blur="finishEdit"
                          @keyup.escape="finishEdit"
                          autofocus
                        >
                          <option value="">请选择答案</option>
                          <option v-for="opt in getAnswerOptions(item)" :key="opt" :value="opt">
                            {{ opt }}
                          </option>
                        </select>
                        <span 
                          v-else 
                          class="cell-answer" 
                          :class="{ 'empty': !value }"
                          @click="startEdit(index, key)"
                        >
                          {{ value || '点击设置答案' }}
                        </span>
                      </template>
                      
                      <!-- 选项字段显示 -->
                      <template v-else-if="key === 'options'">
                        <div class="options-display">
                          <div v-for="opt in parseOptions(value)" :key="opt.label" class="option-item">
                            <strong>{{ opt.label }}.</strong> {{ opt.content }}
                          </div>
                        </div>
                      </template>
                      
                      <!-- 普通字段可编辑 -->
                      <template v-else>
                        <input 
                          v-if="editingCell === `${index}-${key}`" 
                          v-model="item[key]" 
                          class="cell-input"
                          @blur="finishEdit"
                          @keyup.escape="finishEdit"
                          @keyup.enter="finishEdit"
                          autofocus
                        />
                        <span v-else class="cell-text" @click="startEdit(index, key)">
                          {{ value || '-' }}
                        </span>
                      </template>
                    </td>
                    <td class="actions-cell">
                      <button class="btn-icon btn-edit" @click="editRow(index)" title="编辑整行">
                        ✏️
                      </button>
                      <button class="btn-icon btn-delete" @click="removeRow(index)" title="删除">
                        🗑️
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <div v-if="parseResult.dataList.length > 10" class="preview-notice">
              仅显示前10条数据,导入时将包含全部 {{ parseResult.totalCount }} 条数据
            </div>
          </div>

          <div class="action-buttons">
            <button class="btn btn-default" @click="currentStep = 1">重新上传</button>
            <button class="btn btn-warning" @click="handleImport" :disabled="importing || hasEmptyAnswers">
              {{ importing ? '导入中...' : (hasEmptyAnswers ? '请补充答案后再导入' : '确认导入') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 步骤3: 导入完成 -->
      <div v-if="currentStep === 3" class="step-content">
        <div class="import-success">
          <div class="success-icon">✓</div>
          <h3>导入成功!</h3>
          <div class="import-stats">
            <p>成功导入 <strong>{{ importResult.successCount }}</strong> 条数据</p>
            <p v-if="importResult.errorCount > 0" class="error-text">
              失败 <strong>{{ importResult.errorCount }}</strong> 条
            </p>
          </div>
          
          <div v-if="importResult.errors && importResult.errors.length > 0" class="error-details">
            <h4>错误详情:</h4>
            <ul>
              <li v-for="(error, index) in importResult.errors" :key="index">{{ error }}</li>
            </ul>
          </div>

          <div class="action-buttons">
            <button class="btn btn-default" @click="resetImport">继续导入</button>
            <button class="btn btn-primary" @click="goBack">返回列表</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 批量设置答案弹窗 -->
    <div v-if="showBatchAnswerModal" class="modal-overlay" @click.self="showBatchAnswerModal = false">
      <div class="modal modal-medium">
        <div class="modal-header">
          <h3>批量设置答案</h3>
          <button class="close-btn" @click="showBatchAnswerModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="batch-answer-form">
            <div class="form-group">
              <label>选择题目范围:</label>
              <div class="radio-group">
                <label><input type="radio" v-model="batchScope" value="all"> 全部题目</label>
                <label><input type="radio" v-model="batchScope" value="empty"> 仅缺少答案的题目</label>
              </div>
            </div>
            
            <div class="form-group">
              <label>设置答案:</label>
              <select v-model="batchAnswer" class="full-width">
                <option value="">请选择答案</option>
                <option value="A">A</option>
                <option value="B">B</option>
                <option value="C">C</option>
                <option value="D">D</option>
              </select>
            </div>

            <div class="batch-preview">
              <h4>将影响 {{ getBatchCount() }} 道题目</h4>
              <ul>
                <li v-for="(item, index) in getBatchItems()" :key="index">
                  题目{{ index + 1 }}: {{ item.questionTitle?.substring(0, 50) }}... → 答案: {{ batchAnswer }}
                </li>
              </ul>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showBatchAnswerModal = false">取消</button>
          <button class="btn btn-primary" @click="applyBatchAnswer" :disabled="!batchAnswer">确认设置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const router = useRouter()
const { toast } = useMessage()

const currentStep = ref(1)
const documentType = ref('word')
const uploadFile = ref(null)
const parsing = ref(false)
const importing = ref(false)
const editingCell = ref(null)
const expandAll = ref(false)
const showBatchAnswerModal = ref(false)
const batchScope = ref('empty')
const batchAnswer = ref('')

const parseResult = reactive({
  success: false,
  message: '',
  totalCount: 0,
  dataList: [],
  warnings: []
})

const importResult = reactive({
  successCount: 0,
  errorCount: 0,
  errors: []
})

// 计算属性: 缺少答案的题目数量
const hasEmptyAnswers = computed(() => {
  if (documentType.value !== 'question') return false
  return parseResult.dataList.some(item => !item.correctAnswer)
})

const emptyAnswerCount = computed(() => {
  if (documentType.value !== 'question') return 0
  return parseResult.dataList.filter(item => !item.correctAnswer).length
})

function triggerUpload() {
  this.$refs.fileInput.click()
}

function handleFileChange(event) {
  const file = event.target.files[0]
  if (file) {
    const allowedTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ]
    
    if (!allowedTypes.includes(file.type) && !file.name.match(/\.(pdf|doc|docx)$/i)) {
      toast.error('请选择PDF或Word文档')
      return
    }
    
    uploadFile.value = file
  }
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

async function handleParse() {
  if (!uploadFile.value) {
    toast.error('请先选择文件')
    return
  }

  parsing.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    formData.append('documentType', documentType.value)

    const res = await request.post('/system/document/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.code === 200) {
      Object.assign(parseResult, res.data.data)
      
      if (parseResult.totalCount === 0) {
        toast.warning('未解析到有效数据，请检查文档格式')
        return
      }
      
      currentStep.value = 2
      toast.success('解析成功')
    } else {
      toast.error(res.data.msg || '解析失败')
    }
  } catch (e) {
    toast.error('解析失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    parsing.value = false
  }
}

async function handleImport() {
  if (parseResult.dataList.length === 0) {
    toast.error('没有可导入的数据')
    return
  }

  importing.value = true
  try {
    let importUrl = ''
    if (documentType.value === 'word') {
      importUrl = '/system/word/import/parsed'
    } else if (documentType.value === 'question') {
      importUrl = '/exam/question/import/parsed'
    }

    const res = await request.post(importUrl, parseResult.dataList)

    if (res.data.code === 200) {
      Object.assign(importResult, res.data.data)
      currentStep.value = 3
      toast.success('导入成功')
    } else {
      toast.error(res.data.msg || '导入失败')
    }
  } catch (e) {
    toast.error('导入失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    importing.value = false
  }
}

function resetImport() {
  currentStep.value = 1
  uploadFile.value = null
  parseResult.success = false
  parseResult.message = ''
  parseResult.totalCount = 0
  parseResult.dataList = []
  parseResult.warnings = []
  importResult.successCount = 0
  importResult.errorCount = 0
  importResult.errors = []
}

function goBack() {
  router.back()
}

// ========== 编辑功能 ==========

function startEdit(index, key) {
  editingCell.value = `${index}-${key}`
}

function finishEdit() {
  editingCell.value = null
}

function editRow(index) {
  // 编辑整行 - 可以扩展为弹窗编辑
  toast.info('点击字段即可编辑')
}

function removeRow(index) {
  if (confirm('确定要删除这条数据吗?')) {
    parseResult.dataList.splice(index, 1)
    parseResult.totalCount = parseResult.dataList.length
    toast.success('已删除')
  }
}

function hasError(item) {
  // 检查是否有必填字段为空
  if (documentType.value === 'question') {
    return !item.correctAnswer
  }
  return false
}

function isRequiredField(key) {
  if (documentType.value === 'question') {
    return key === 'questionTitle' || key === 'correctAnswer'
  }
  if (documentType.value === 'word') {
    return key === 'word' || key === 'translation'
  }
  return false
}

function getFieldLabel(key) {
  const labelMap = {
    questionTitle: '题目内容',
    correctAnswer: '正确答案',
    questionType: '题型',
    options: '选项',
    difficulty: '难度',
    score: '分数',
    word: '单词',
    phonetic: '音标',
    translation: '释义',
    grade: '年级'
  }
  return labelMap[key] || key
}

function getAnswerOptions(item) {
  // 从选项中提取答案选项
  if (!item.options) return ['A', 'B', 'C', 'D']
  try {
    const options = typeof item.options === 'string' ? JSON.parse(item.options) : item.options
    return options.map(opt => opt.label)
  } catch (e) {
    return ['A', 'B', 'C', 'D']
  }
}

function parseOptions(options) {
  if (!options) return []
  try {
    return typeof options === 'string' ? JSON.parse(options) : options
  } catch (e) {
    return []
  }
}

function toggleExpandAll() {
  expandAll.value = !expandAll.value
}

// ========== 批量设置答案 ==========

function getBatchItems() {
  if (batchScope.value === 'all') {
    return parseResult.dataList
  }
  return parseResult.dataList.filter(item => !item.correctAnswer)
}

function getBatchCount() {
  return getBatchItems().length
}

function applyBatchAnswer() {
  if (!batchAnswer.value) {
    toast.warning('请选择答案')
    return
  }
  
  const items = getBatchItems()
  items.forEach(item => {
    item.correctAnswer = batchAnswer.value
  })
  
  toast.success(`已为 ${items.length} 道题目设置答案: ${batchAnswer.value}`)
  showBatchAnswerModal.value = false
  batchAnswer.value = ''
}
</script>

<style scoped>
.ai-import-page {
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

.import-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 步骤指示器 */
.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s;
}

.step.active .step-number {
  background: #667eea;
  color: white;
}

.step.completed .step-number {
  background: #52c41a;
  color: white;
}

.step-title {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

.step.active .step-title {
  color: #667eea;
  font-weight: 600;
}

.step-line {
  width: 100px;
  height: 2px;
  background: #e0e0e0;
  margin: 0 10px;
  transition: all 0.3s;
}

.step-line.active {
  background: #667eea;
}

/* 上传区域 */
.upload-section h3 {
  margin-bottom: 15px;
  color: #333;
}

.type-selector {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.type-btn {
  flex: 1;
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.type-btn:hover {
  border-color: #667eea;
}

.type-btn.active {
  border-color: #667eea;
  background: #f0f5ff;
  color: #667eea;
  font-weight: 600;
}

.upload-area {
  border: 2px dashed #d0d0d0;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-area:hover {
  border-color: #667eea;
  background: #fafafa;
}

.upload-area.has-file {
  border-color: #52c41a;
  background: #f6ffed;
}

.upload-icon {
  font-size: 48px;
}

.upload-text {
  font-size: 16px;
  color: #666;
}

.upload-hint {
  font-size: 13px;
  color: #999;
}

.file-icon {
  font-size: 36px;
}

.file-name {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.file-size {
  font-size: 13px;
  color: #999;
}

.format-tips {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.format-tips h4 {
  margin-bottom: 10px;
  color: #333;
}

.format-tips ul {
  margin: 10px 0;
  padding-left: 20px;
}

.format-tips li {
  margin: 5px 0;
  color: #666;
  font-family: monospace;
  background: white;
  padding: 5px 10px;
  border-radius: 4px;
}

/* 解析结果 */
.parse-result {
  margin-top: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-stats {
  display: flex;
  gap: 15px;
}

.stat {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
}

.stat.success {
  background: #f6ffed;
  color: #52c41a;
}

.stat.warning {
  background: #fffbe6;
  color: #faad14;
}

.stat.danger {
  background: #fff1f0;
  color: #ff4d4f;
}

.answer-reminder {
  background: #e6f7ff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #1890ff;
}

.answer-reminder h4 {
  margin: 0 0 8px 0;
  color: #1890ff;
}

.answer-reminder p {
  margin: 0;
  color: #666;
}

.warnings {
  background: #fffbe6;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #faad14;
}

.warnings h4 {
  margin-bottom: 10px;
  color: #faad14;
}

.warnings ul {
  margin: 0;
  padding-left: 20px;
}

.warnings li {
  margin: 5px 0;
  color: #666;
  font-size: 13px;
}

.data-preview {
  margin-bottom: 30px;
}

.data-preview h4 {
  margin-bottom: 15px;
  color: #333;
}

.preview-table {
  overflow-x: auto;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

.editable-table {
  overflow-x: auto;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.editable-table table {
  width: 100%;
  border-collapse: collapse;
}

.editable-table th {
  background: #f5f5f5;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
  position: relative;
}

.editable-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
  font-size: 13px;
  vertical-align: top;
}

.editable-table tr:hover {
  background: #fafafa;
}

.editable-table tr.has-error {
  background: #fff1f0;
}

.required-field {
  color: #ff4d4f;
}

.required-mark {
  color: #ff4d4f;
  margin-left: 2px;
}

.editable-cell {
  cursor: pointer;
  position: relative;
}

.editable-cell:hover {
  background: #f0f5ff;
}

.cell-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #1890ff;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
  box-sizing: border-box;
}

.cell-textarea {
  resize: vertical;
  min-height: 60px;
  font-family: inherit;
}

.cell-select {
  cursor: pointer;
}

.cell-text {
  display: block;
  padding: 4px;
  border-radius: 4px;
  min-height: 20px;
}

.cell-text:hover {
  background: #e6f7ff;
}

.cell-answer {
  display: inline-block;
  padding: 4px 12px;
  background: #52c41a;
  color: white;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
}

.cell-answer.empty {
  background: #ff4d4f;
  color: white;
  font-weight: normal;
  font-style: italic;
}

.cell-answer.empty:hover {
  background: #ff7875;
}

.options-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-item {
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
}

.actions-cell {
  text-align: center;
  white-space: nowrap;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #f0f0f0;
}

.btn-edit:hover {
  background: #e6f7ff;
}

.btn-delete:hover {
  background: #fff1f0;
}

.preview-notice {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 13px;
  background: #fafafa;
  border-top: 1px solid #e0e0e0;
}

/* 导入成功 */
.import-success {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #52c41a;
  color: white;
  font-size: 48px;
  line-height: 80px;
  margin: 0 auto 20px;
}

.import-success h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.import-stats {
  margin-bottom: 30px;
}

.import-stats p {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.import-stats strong {
  color: #52c41a;
  font-size: 24px;
}

.error-text strong {
  color: #ff4d4f;
}

.error-details {
  background: #fff1f0;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: left;
  border-left: 4px solid #ff4d4f;
}

.error-details h4 {
  margin-bottom: 10px;
  color: #ff4d4f;
}

.error-details ul {
  margin: 0;
  padding-left: 20px;
}

.error-details li {
  margin: 5px 0;
  color: #666;
  font-size: 13px;
}

/* 按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-default {
  background: #f5f5f5;
  color: #666;
}

.btn-default:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5568d3;
}

.btn-lg {
  padding: 14px 40px;
  font-size: 16px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-warning {
  background: #faad14;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background: #d48806;
}

.btn-warning:disabled {
  background: #ffe58f;
  cursor: not-allowed;
}

/* 批量设置答案弹窗 */
.modal-medium {
  max-width: 600px;
}

.batch-answer-form {
  padding: 10px 0;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.full-width {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  margin-top: 8px;
}

.batch-preview {
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.batch-preview h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
}

.batch-preview ul {
  margin: 0;
  padding-left: 20px;
}

.batch-preview li {
  margin: 5px 0;
  color: #666;
  font-size: 12px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 25px;
  border-top: 1px solid #f0f0f0;
}
</style>
