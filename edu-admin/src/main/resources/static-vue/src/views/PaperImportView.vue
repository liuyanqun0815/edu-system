<template>
  <div class="paper-import-page">
    <div class="page-header">
      <h1 class="page-title">试卷导入</h1>
      <button class="btn btn-default" @click="goBack">返回</button>
    </div>

    <div class="import-container">
      <!-- 方式选择 -->
      <div class="method-selector">
        <button 
          :class="['method-btn', { active: importMethod === 'word' }]"
          @click="importMethod = 'word'"
        >
          📄 Word文档导入
        </button>
        <button 
          :class="['method-btn', { active: importMethod === 'text' }]"
          @click="importMethod = 'text'"
        >
          📝 文本粘贴导入
        </button>
      </div>

      <!-- Word导入 -->
      <div v-if="importMethod === 'word'" class="import-section">
        <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop" @click="triggerFileInput">
          <input ref="fileInput" type="file" accept=".docx,.doc" @change="handleFileSelect" style="display:none">
          <div v-if="!selectedFile" class="upload-placeholder">
            <div class="upload-icon">📤</div>
            <p>点击或拖拽Word文件到此处</p>
            <span class="upload-tip">支持 .docx, .doc 格式</span>
          </div>
          <div v-else class="file-info">
            <div class="file-icon">📄</div>
            <div class="file-details">
              <div class="file-name">{{ selectedFile.name }}</div>
              <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
            </div>
            <button class="remove-btn" @click.stop="removeFile">✕</button>
          </div>
        </div>
      </div>

      <!-- 文本导入 -->
      <div v-if="importMethod === 'text'" class="import-section">
        <div class="text-input-wrapper">
          <textarea 
            v-model="textContent" 
            placeholder="请粘贴试卷内容，格式示例：&#10;1. 题目内容&#10;A. 选项A&#10;B. 选项B&#10;C. 选项C&#10;D. 选项D&#10;答案：A&#10;&#10;2. 第二题..."
            rows="15"
          ></textarea>
          <div class="text-tips">
            <strong>📋 格式要求：</strong>
            <ul>
              <li>题目格式：<code>1. 题目内容</code> 或 <code>1、题目内容</code></li>
              <li>选项格式：<code>A. 选项内容</code></li>
              <li>答案格式：<code>答案：A</code> 或 <code>答案:A</code></li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 配置选项 -->
      <div class="config-section">
        <h3 class="section-title">配置选项</h3>
        <div class="config-form">
          <div class="form-group">
            <label>年级</label>
            <select v-model="config.gradeId">
              <option value="">请选择年级</option>
              <option v-for="grade in gradeOptions" :key="grade.itemValue" :value="grade.itemValue">
                {{ grade.itemName || grade.itemLabel }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>学科</label>
            <select v-model="config.subjectId">
              <option value="">请选择学科</option>
              <option v-for="subject in subjectOptions" :key="subject.itemValue" :value="subject.itemValue">
                {{ subject.itemName || subject.itemLabel }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="btn btn-primary" :disabled="submitting || !canSubmit" @click="handleImport">
          {{ submitting ? '解析中...' : '开始解析' }}
        </button>
      </div>

      <!-- 解析结果 -->
      <div v-if="parseResult" class="result-section">
        <h3 class="section-title">解析结果</h3>
        <div :class="['result-card', parseResult.success ? 'success' : 'error']">
          <div class="result-icon">{{ parseResult.success ? '✅' : '❌' }}</div>
          <div class="result-content">
            <p class="result-message">{{ parseResult.message }}</p>
            <div v-if="parseResult.success && parseResult.questions" class="question-preview">
              <h4>成功导入 {{ parseResult.questions.length }} 道题目：</h4>
              <div v-for="(q, idx) in parseResult.questions.slice(0, 5)" :key="idx" class="preview-item">
                <span class="preview-number">{{ idx + 1 }}.</span>
                <span class="preview-text">{{ q.title || q.content }}</span>
              </div>
              <div v-if="parseResult.questions.length > 5" class="more-tip">
                还有 {{ parseResult.questions.length - 5 }} 道题目...
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const router = useRouter()
const { toast } = useMessage()
const configStore = useConfigStore()

const importMethod = ref('word')
const selectedFile = ref(null)
const textContent = ref('')
const submitting = ref(false)
const parseResult = ref(null)
const fileInput = ref(null)

const config = reactive({
  gradeId: '',
  subjectId: ''
})

const gradeOptions = computed(() => configStore.gradeList)
const subjectOptions = computed(() => configStore.examSubjects)

const canSubmit = computed(() => {
  if (importMethod.value === 'word') {
    return selectedFile.value && config.gradeId && config.subjectId
  } else {
    return textContent.value.trim() && config.gradeId && config.subjectId
  }
})

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
  }
}

function handleDrop(event) {
  const file = event.dataTransfer.files[0]
  if (file && (file.name.endsWith('.docx') || file.name.endsWith('.doc'))) {
    selectedFile.value = file
  } else {
    toast.warning('请上传Word文件')
  }
}

function removeFile() {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

async function handleImport() {
  if (!canSubmit.value) {
    toast.warning('请完善所有必填项')
    return
  }

  submitting.value = true
  parseResult.value = null

  try {
    if (importMethod.value === 'word') {
      await importWord()
    } else {
      await importText()
    }
  } catch (e) {
    parseResult.value = {
      success: false,
      message: '解析失败：' + (e.response?.data?.msg || e.message)
    }
  } finally {
    submitting.value = false
  }
}

async function importWord() {
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  if (config.gradeId) formData.append('gradeId', config.gradeId)
  if (config.subjectId) formData.append('subjectId', config.subjectId)

  const res = await request.post('/exam/paper/parse/word', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

  if (res.data.code === 200) {
    parseResult.value = {
      success: true,
      message: res.data.msg || '解析成功',
      questions: res.data.data || []
    }
    toast.success('解析成功')
  } else {
    throw new Error(res.data.msg)
  }
}

async function importText() {
  const res = await request.post('/exam/paper/parse/text', {
    content: textContent.value,
    gradeId: config.gradeId,
    subjectId: config.subjectId
  })

  if (res.data.code === 200) {
    parseResult.value = {
      success: true,
      message: res.data.msg || '解析成功',
      questions: res.data.data || []
    }
    toast.success('解析成功')
  } else {
    throw new Error(res.data.msg)
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  configStore.loadConfigs()
})
</script>

<style scoped>
.paper-import-page {
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
  max-width: 900px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.method-selector {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.method-btn {
  flex: 1;
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s;
}

.method-btn:hover {
  border-color: #667eea;
}

.method-btn.active {
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
}

.upload-area:hover {
  border-color: #667eea;
  background: #fafafa;
}

.upload-placeholder {
  color: #999;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.upload-tip {
  display: block;
  margin-top: 10px;
  font-size: 12px;
  color: #bbb;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f0f5ff;
  border-radius: 8px;
}

.file-icon {
  font-size: 32px;
}

.file-details {
  flex: 1;
  text-align: left;
}

.file-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.file-size {
  font-size: 12px;
  color: #999;
}

.remove-btn {
  background: none;
  border: none;
  color: #f5576c;
  font-size: 20px;
  cursor: pointer;
  padding: 5px 10px;
}

.text-input-wrapper {
  margin-bottom: 30px;
}

textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: monospace;
  resize: vertical;
  margin-bottom: 15px;
  box-sizing: border-box;
}

textarea:focus {
  outline: none;
  border-color: #667eea;
}

.text-tips {
  background: #fffbe6;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #faad14;
  font-size: 13px;
}

.text-tips strong {
  display: block;
  margin-bottom: 8px;
  color: #fa8c16;
}

.text-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.text-tips code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  color: #667eea;
}

.config-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.config-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.action-buttons {
  text-align: center;
  margin: 30px 0;
}

.btn {
  padding: 14px 40px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn-default:hover {
  background: #e0e0e0;
}

.result-section {
  margin-top: 30px;
}

.result-card {
  padding: 25px;
  border-radius: 12px;
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.result-card.success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.result-card.error {
  background: #fff0f6;
  border: 1px solid #ffadd2;
}

.result-icon {
  font-size: 36px;
}

.result-content {
  flex: 1;
}

.result-message {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
}

.question-preview h4 {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.preview-item {
  padding: 10px;
  margin-bottom: 8px;
  background: white;
  border-radius: 6px;
  font-size: 14px;
}

.preview-number {
  font-weight: bold;
  color: #667eea;
  margin-right: 8px;
}

.more-tip {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .config-form {
    grid-template-columns: 1fr;
  }

  .method-selector {
    flex-direction: column;
  }
}
</style>
