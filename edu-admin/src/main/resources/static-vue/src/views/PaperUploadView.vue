<template>
  <div class="paper-upload-page">
    <div class="page-header">
      <h1 class="page-title">试卷上传</h1>
    </div>

    <div class="upload-container">
      <div class="upload-area" @click="triggerUpload" :class="{ 'has-file': uploadFile }">
        <input type="file" ref="fileInput" accept=".doc,.docx,.pdf,.txt" @change="handleFileChange" style="display:none">
        <template v-if="uploadFile">
          <span class="file-icon">📄</span>
          <span class="file-name">{{ uploadFile.name }}</span>
        </template>
        <template v-else>
          <span class="upload-icon">📁</span>
          <span class="upload-text">点击选择试卷文件</span>
          <span class="upload-hint">支持 .doc, .docx, .pdf, .txt 格式</span>
        </template>
      </div>

      <div class="form-group">
        <label>试卷名称</label>
        <input v-model="paperName" type="text" placeholder="请输入试卷名称">
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>考试科目</label>
          <select v-model="subject">
            <option value="">请选择科目</option>
            <option v-for="s in subjectOptions" :key="s.itemValue" :value="s.itemValue">{{ s.itemName || s.itemLabel }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>试卷类型</label>
          <select v-model="paperType">
            <option value="">请选择类型</option>
            <option v-for="t in paperTypeOptions" :key="t.itemValue" :value="t.itemValue">{{ t.itemName || t.itemLabel }}</option>
          </select>
        </div>
      </div>

      <div class="form-actions">
        <button class="btn btn-primary" :disabled="!uploadFile || !paperName || uploading" @click="doUpload">
          {{ uploading ? '上传解析中...' : '上传并解析' }}
        </button>
      </div>
    </div>

    <div v-if="parseResult" class="parse-result">
      <h3>解析结果</h3>
      <div class="result-info">
        <p>识别到 <strong>{{ parseResult.questionCount }}</strong> 道题目</p>
        <p v-if="parseResult.questions && parseResult.questions.length > 0">
          <button class="btn btn-info" @click="showQuestions">查看题目详情</button>
          <button class="btn btn-primary" @click="saveToQuestionBank">保存到题库</button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const { toast } = useMessage()
const configStore = useConfigStore()

const uploadFile = ref(null)
const fileInput = ref(null)
const paperName = ref('')
const subject = ref('')
const paperType = ref('')
const uploading = ref(false)
const parseResult = ref(null)

const subjectOptions = computed(() => configStore.examSubjects)
const paperTypeOptions = computed(() => configStore.paperTypes)

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    uploadFile.value = file
    if (!paperName.value) {
      paperName.value = file.name.replace(/\.[^/.]+$/, '')
    }
  }
}

async function doUpload() {
  if (!uploadFile.value || !paperName.value) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    formData.append('name', paperName.value)
    formData.append('subject', subject.value)
    formData.append('type', paperType.value)

    const res = await request.post('/exam/paper/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.code === 200) {
      parseResult.value = res.data.data
      toast.success('试卷解析成功')
    }
  } catch (e) {
    toast.error('上传失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    uploading.value = false
  }
}

function showQuestions() {
  // 显示题目详情
  toast.info('题目详情功能开发中')
}

async function saveToQuestionBank() {
  if (!parseResult.value?.questions) return
  
  try {
    const res = await request.post('/exam/question/batch', {
      questions: parseResult.value.questions,
      subject: subject.value
    })
    if (res.data.code === 200) {
      toast.success('已保存到题库')
      parseResult.value = null
      uploadFile.value = null
      paperName.value = ''
    }
  } catch (e) {
    toast.error('保存失败: ' + (e.response?.data?.msg || e.message))
  }
}

onMounted(() => {
  configStore.loadConfigs()
})
</script>

<style scoped>
.paper-upload-page {
  padding: 20px;
  max-width: 800px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.upload-container {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.upload-area:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.upload-area.has-file {
  border-color: #52c41a;
  background: #f6ffed;
}

.upload-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 13px;
  color: #999;
}

.file-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.file-name {
  font-size: 16px;
  color: #333;
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.btn {
  padding: 10px 30px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  margin: 0 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-info {
  background: #e6f7ff;
  color: #1890ff;
}

.parse-result {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-top: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.parse-result h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
}

.result-info p {
  margin: 10px 0;
  color: #666;
}
</style>
