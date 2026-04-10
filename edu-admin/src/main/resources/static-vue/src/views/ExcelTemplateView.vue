<template>
  <div class="excel-template-page">
    <div class="page-header">
      <h1 class="page-title">Excel模板管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建模板</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <select v-model="searchForm.businessModule" class="search-select">
        <option value="">全部模块</option>
        <option value="question">题库</option>
        <option value="course">课程</option>
        <option value="exam">考试</option>
      </select>
      <select v-model="searchForm.templateType" class="search-select">
        <option value="">全部类型</option>
        <option :value="1">导入模板</option>
        <option :value="2">导出模板</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>模板编码</th>
            <th>模板名称</th>
            <th>类型</th>
            <th>业务模块</th>
            <th>Sheet名称</th>
            <th>列数</th>
            <th>状态</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="template in templateList" :key="template.id">
            <td><code>{{ template.templateCode }}</code></td>
            <td>{{ template.templateName }}</td>
            <td>
              <span :class="['type-tag', `type-${template.templateType}`]">
                {{ template.templateType === 1 ? '导入' : '导出' }}
              </span>
            </td>
            <td>{{ moduleMap[template.businessModule] || template.businessModule }}</td>
            <td>{{ template.sheetName }}</td>
            <td>{{ getTemplateColumns(template.id).length }}</td>
            <td>
              <span :class="['status-tag', `status-${template.status}`]">
                {{ template.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ template.sortOrder }}</td>
            <td class="actions">
              <button class="action-btn action-view" @click="handlePreview(template)">预览</button>
              <button class="action-btn action-edit" @click="handleEdit(template)">编辑</button>
              <button class="action-btn action-download" @click="handleDownload(template)">下载</button>
              <button class="action-btn action-delete" @click="handleDelete(template)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 预览/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑模板' : '新建模板' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 模板基本信息 -->
          <div class="form-section">
            <h4>模板基本信息</h4>
            <div class="form-row">
              <div class="form-group">
                <label>模板编码 *</label>
                <input v-model="form.template.templateCode" type="text" placeholder="唯一标识,如: question_import" :disabled="isEdit">
              </div>
              <div class="form-group">
                <label>模板名称 *</label>
                <input v-model="form.template.templateName" type="text" placeholder="如: 题库导入模板">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>模板类型 *</label>
                <select v-model="form.template.templateType">
                  <option :value="1">导入模板</option>
                  <option :value="2">导出模板</option>
                </select>
              </div>
              <div class="form-group">
                <label>业务模块 *</label>
                <select v-model="form.template.businessModule">
                  <option value="question">题库</option>
                  <option value="course">课程</option>
                  <option value="exam">考试</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>Sheet名称</label>
                <input v-model="form.template.sheetName" type="text" placeholder="Sheet1">
              </div>
              <div class="form-group">
                <label>排序</label>
                <input v-model.number="form.template.sortOrder" type="number" placeholder="0">
              </div>
            </div>
          </div>

          <!-- 样式配置 -->
          <div class="form-section">
            <h4>样式配置</h4>
            <div class="form-row">
              <div class="form-group">
                <label>表头行高</label>
                <input v-model.number="form.template.headerRowHeight" type="number" placeholder="25">
              </div>
              <div class="form-group">
                <label>内容行高</label>
                <input v-model.number="form.template.contentRowHeight" type="number" placeholder="20">
              </div>
              <div class="form-group">
                <label>默认列宽</label>
                <input v-model.number="form.template.defaultColumnWidth" type="number" placeholder="20">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>表头背景色</label>
                <input v-model="form.template.headerBgColor" type="color" class="color-picker">
              </div>
              <div class="form-group">
                <label>表头字体颜色</label>
                <input v-model="form.template.headerFontColor" type="color" class="color-picker">
              </div>
            </div>
          </div>

          <!-- 列配置 -->
          <div class="form-section">
            <div class="section-header">
              <h4>列配置</h4>
              <button class="btn btn-sm btn-primary" @click="addColumn">+ 添加列</button>
            </div>
            <table class="column-table">
              <thead>
                <tr>
                  <th width="60">排序</th>
                  <th width="120">字段名</th>
                  <th width="150">列标题</th>
                  <th width="80">列宽</th>
                  <th width="100">数据类型</th>
                  <th width="60">必填</th>
                  <th width="60">显示</th>
                  <th width="100">对齐</th>
                  <th>示例数据</th>
                  <th width="80">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(col, index) in form.columns" :key="index">
                  <td><input v-model.number="col.sortOrder" type="number" class="sm-input"></td>
                  <td><input v-model="col.fieldName" type="text" placeholder="字段名"></td>
                  <td><input v-model="col.columnTitle" type="text" placeholder="列标题"></td>
                  <td><input v-model.number="col.columnWidth" type="number" class="sm-input" placeholder="默认"></td>
                  <td>
                    <select v-model="col.dataType">
                      <option value="String">String</option>
                      <option value="Integer">Integer</option>
                      <option value="Long">Long</option>
                      <option value="Double">Double</option>
                      <option value="BigDecimal">BigDecimal</option>
                      <option value="Date">Date</option>
                    </select>
                  </td>
                  <td><input v-model.number="col.required" type="checkbox" :true-value="1" :false-value="0"></td>
                  <td><input v-model.number="col.visible" type="checkbox" :true-value="1" :false-value="0"></td>
                  <td>
                    <select v-model="col.alignment">
                      <option value="left">左</option>
                      <option value="center">中</option>
                      <option value="right">右</option>
                    </select>
                  </td>
                  <td><input v-model="col.exampleValue" type="text" placeholder="示例值"></td>
                  <td>
                    <button class="action-btn action-delete" @click="removeColumn(index)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closeModal">取消</button>
            <button type="button" class="btn btn-primary" @click="handleSave">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <div v-if="showPreviewModal" class="modal-overlay" @click.self="showPreviewModal = false">
      <div class="modal modal-xlarge">
        <div class="modal-header">
          <h3>模板预览 - {{ previewData.template?.templateName }}</h3>
          <button class="close-btn" @click="showPreviewModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="preview-info">
            <p><strong>模板编码:</strong> {{ previewData.template?.templateCode }}</p>
            <p><strong>业务模块:</strong> {{ moduleMap[previewData.template?.businessModule] }}</p>
            <p><strong>列数:</strong> {{ previewData.columns?.length }}</p>
          </div>
          <table class="preview-table">
            <thead>
              <tr>
                <th v-for="col in previewData.columns" :key="col.id" :style="{width: col.columnWidth + 'px'}">
                  {{ col.columnTitle }}
                  <span v-if="col.required" style="color: red;">*</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td v-for="col in previewData.columns" :key="col.id">
                  {{ col.exampleValue || '-' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm } = useMessage()

const moduleMap = {
  question: '题库',
  course: '课程',
  exam: '考试'
}

const searchForm = reactive({
  businessModule: '',
  templateType: ''
})

const templateList = ref([])
const showModal = ref(false)
const showPreviewModal = ref(false)
const isEdit = ref(false)
const previewData = ref({})

const form = reactive({
  template: {
    templateCode: '',
    templateName: '',
    templateType: 1,
    businessModule: 'question',
    sheetName: 'Sheet1',
    headerRowHeight: 25,
    contentRowHeight: 20,
    defaultColumnWidth: 20,
    headerBgColor: '#4472C4',
    headerFontColor: '#FFFFFF',
    status: 1,
    sortOrder: 0
  },
  columns: []
})

async function fetchTemplateList() {
  try {
    const res = await request.get('/system/excel-template/list', {
      params: searchForm
    })
    if (res.data.code === 200) {
      templateList.value = res.data.data
    }
  } catch (e) {
    console.error('获取模板列表失败', e)
  }
}

function getTemplateColumns(templateId) {
  // 简化处理,实际应单独请求
  return []
}

function handleSearch() {
  fetchTemplateList()
}

function resetSearch() {
  searchForm.businessModule = ''
  searchForm.templateType = ''
  fetchTemplateList()
}

function openAddModal() {
  isEdit.value = false
  form.template = {
    templateCode: '',
    templateName: '',
    templateType: 1,
    businessModule: 'question',
    sheetName: 'Sheet1',
    headerRowHeight: 25,
    contentRowHeight: 20,
    defaultColumnWidth: 20,
    headerBgColor: '#4472C4',
    headerFontColor: '#FFFFFF',
    status: 1,
    sortOrder: 0
  }
  form.columns = []
  showModal.value = true
}

async function handleEdit(template) {
  isEdit.value = true
  try {
    const res = await request.get(`/system/excel-template/${template.id}`)
    if (res.data.code === 200) {
      form.template = { ...res.data.data.template }
      form.columns = res.data.data.columns || []
      showModal.value = true
    }
  } catch (e) {
    toast.error('获取模板详情失败')
  }
}

function closeModal() {
  showModal.value = false
}

function addColumn() {
  form.columns.push({
    columnIndex: form.columns.length,
    fieldName: '',
    columnTitle: '',
    columnWidth: null,
    dataType: 'String',
    required: 0,
    visible: 1,
    alignment: 'center',
    exampleValue: '',
    sortOrder: form.columns.length
  })
}

function removeColumn(index) {
  form.columns.splice(index, 1)
}

async function handleSave() {
  try {
    await request.post('/system/excel-template', form)
    toast.success('保存成功')
    closeModal()
    fetchTemplateList()
  } catch (e) {
    toast.error(e.response?.data?.msg || '保存失败')
  }
}

async function handlePreview(template) {
  try {
    const res = await request.get(`/system/excel-template/preview/${template.templateCode}`)
    if (res.data.code === 200) {
      previewData.value = res.data.data
      showPreviewModal.value = true
    }
  } catch (e) {
    toast.error('预览失败')
  }
}

function handleDownload(template) {
  window.open(`/system/excel-template/download/${template.templateCode}`, '_blank')
}

async function handleDelete(template) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除模板 <b>"${template.templateName}"</b> 吗?`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    await request.delete(`/system/excel-template/${template.id}`)
    toast.success('删除成功')
    fetchTemplateList()
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

onMounted(() => {
  fetchTemplateList()
})
</script>

<style scoped>
.excel-template-page {
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
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-default {
  background: #f0f0f0;
  color: #666;
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

.search-select {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
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
}

.type-tag,
.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.type-1 {
  background: #e6f7ff;
  color: #1890ff;
}

.type-2 {
  background: #f6ffed;
  color: #52c41a;
}

.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.status-0 {
  background: #f0f0f0;
  color: #999;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.action-view {
  background: #e6f7ff;
  color: #1890ff;
}

.action-edit {
  background: #f6ffed;
  color: #52c41a;
}

.action-download {
  background: #fff7e6;
  color: #fa8c16;
}

.action-delete {
  background: #fff0f6;
  color: #ff4d4f;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-large {
  width: 900px;
}

.modal-xlarge {
  width: 1200px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.form-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.form-group input,
.form-group select {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.color-picker {
  width: 80px;
  height: 40px;
  padding: 2px;
  cursor: pointer;
}

.column-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.column-table th,
.column-table td {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.column-table th {
  background: #e6f7ff;
  font-weight: 600;
  font-size: 13px;
}

.column-table input,
.column-table select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
}

.sm-input {
  width: 60px !important;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.preview-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f5ff;
  border-radius: 8px;
}

.preview-info p {
  margin: 8px 0;
  color: #666;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
}

.preview-table th,
.preview-table td {
  padding: 12px;
  border: 1px solid #e0e0e0;
  text-align: center;
}

.preview-table th {
  background: #4472C4;
  color: white;
  font-weight: 600;
}

.preview-table td {
  background: white;
}
</style>
