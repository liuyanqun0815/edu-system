<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">邮件模板管理</h1>
      <button class="btn btn-primary" @click="openAdd">+ 新建模板</button>
    </div>

    <!-- 筛选 -->
    <div class="search-bar">
      <select v-model="filterRole" class="search-select" @change="loadList">
        <option value="">全部角色</option>
        <option value="teacher">教师</option>
        <option value="parent">家长</option>
        <option value="student">学生</option>
        <option value="all">通用</option>
      </select>
      <select v-model="filterStatus" class="search-select" @change="loadList">
        <option value="">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">禁用</option>
      </select>
      <button class="btn btn-primary" @click="loadList">查询</button>
    </div>

    <!-- 列表 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>模板编码</th>
            <th>模板名称</th>
            <th>邮件主题</th>
            <th>适用角色</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tpl in list" :key="tpl.id">
            <td><code class="code-tag">{{ tpl.templateCode }}</code></td>
            <td>{{ tpl.templateName }}</td>
            <td class="subject-cell">{{ tpl.subject }}</td>
            <td>
              <span :class="['role-tag', 'role-' + tpl.roleType]">{{ roleLabel(tpl.roleType) }}</span>
            </td>
            <td>
              <span :class="['status-tag', tpl.status === 1 ? 'status-enabled' : 'status-disabled']">
                {{ tpl.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(tpl.createTime) }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="openEdit(tpl)">编辑</button>
              <button class="action-btn action-warn" @click="toggleStatus(tpl)">
                {{ tpl.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="action-btn action-delete" @click="handleDelete(tpl)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="list.length === 0" class="empty-tip">暂无邮件模板数据</div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑模板' : '新建模板' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>模板编码 <span class="required">*</span></label>
                <input v-model="form.templateCode" type="text" placeholder="如：lesson_notify_teacher" required :disabled="isEdit">
                <div class="form-hint">唯一标识，创建后不可修改</div>
              </div>
              <div class="form-group">
                <label>模板名称 <span class="required">*</span></label>
                <input v-model="form.templateName" type="text" placeholder="请输入模板名称" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>适用角色 <span class="required">*</span></label>
                <select v-model="form.roleType" required>
                  <option value="teacher">教师</option>
                  <option value="parent">家长</option>
                  <option value="student">学生</option>
                  <option value="all">通用（所有角色）</option>
                </select>
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>邮件主题 <span class="required">*</span></label>
              <input v-model="form.subject" type="text" placeholder="支持变量如 ${courseName}，${lessonDate}" required>
            </div>
            <div class="form-group">
              <label>支持的变量（JSON数组，仅说明）</label>
              <input v-model="form.variables" type="text" placeholder='如：["lessonDate","courseName","teacherName"]'>
            </div>
            <div class="form-group">
              <label>邮件正文 <span class="required">*</span>（支持HTML和变量 ${xxx}）</label>
              <textarea v-model="form.body" rows="12" placeholder="请输入HTML格式的邮件正文..." required></textarea>
            </div>
            <!-- 变量说明提示 -->
            <div class="var-hint">
              <strong>常用变量：</strong>
              <code>${lessonDate}</code> 上课日期 &nbsp;
              <code>${startTime}</code> 开始时间 &nbsp;
              <code>${endTime}</code> 结束时间 &nbsp;
              <code>${courseName}</code> 课程名称 &nbsp;
              <code>${location}</code> 上课地点 &nbsp;
              <code>${teacherName}</code> 教师名称 &nbsp;
              <code>${studentName}</code> 学生名称 &nbsp;
              <code>${parentName}</code> 家长名称 &nbsp;
              <code>${studentCount}</code> 学生人数
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref([])
const filterRole = ref('')
const filterStatus = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const defaultForm = () => ({
  templateCode: '', templateName: '', subject: '', body: '',
  variables: '', roleType: 'teacher', status: 1
})
const form = ref(defaultForm())

onMounted(() => loadList())

async function loadList() {
  try {
    const params = {}
    if (filterRole.value) params.roleType = filterRole.value
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await request.get('/system/email-template/list', { params })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { console.error(e) }
}

function openAdd() {
  isEdit.value = false
  form.value = defaultForm()
  showModal.value = true
}

function openEdit(tpl) {
  isEdit.value = true
  form.value = { ...tpl }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/system/email-template', form.value)
    } else {
      await request.post('/system/email-template', form.value)
    }
    closeModal()
    await loadList()
  } catch (e) {
    alert('保存失败：' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(tpl) {
  const newStatus = tpl.status === 1 ? 0 : 1
  try {
    await request.put(`/system/email-template/${tpl.id}/status`, null, { params: { status: newStatus } })
    tpl.status = newStatus
  } catch (e) { alert('操作失败') }
}

async function handleDelete(tpl) {
  if (!confirm(`确认删除模板"${tpl.templateName}"？`)) return
  try {
    await request.delete(`/system/email-template/${tpl.id}`)
    await loadList()
  } catch (e) { alert('删除失败') }
}

function roleLabel(r) {
  const m = { teacher: '教师', parent: '家长', student: '学生', all: '通用' }
  return m[r] || r
}

function formatDate(d) {
  if (!d) return '—'
  return String(d).substring(0, 10)
}
</script>

<style scoped>
.page { padding: 24px; }
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title { font-size: 20px; font-weight: 700; color: #333; margin: 0; }
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.search-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 120px;
}
.table-container {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  background: #f8f9fa;
  padding: 12px 14px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  border-bottom: 1px solid #eee;
}
.data-table td {
  padding: 12px 14px;
  font-size: 13px;
  border-bottom: 1px solid #f5f5f5;
  color: #333;
}
.subject-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.code-tag {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  color: #667eea;
}
.role-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}
.role-teacher { background: #e6f7ff; color: #1890ff; }
.role-parent { background: #fff7e6; color: #fa8c16; }
.role-student { background: #f6ffed; color: #52c41a; }
.role-all { background: #f0f0f0; color: #666; }
.status-tag { padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.status-enabled { background: #f6ffed; color: #52c41a; }
.status-disabled { background: #fff1f0; color: #f5222d; }
.actions { display: flex; gap: 6px; flex-wrap: wrap; }
.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
}
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-warn { background: #fff7e6; color: #fa8c16; }
.action-delete { background: #fff1f0; color: #f5222d; }
.empty-tip { text-align: center; color: #999; padding: 40px; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: flex-start; justify-content: center;
  padding: 60px 20px 20px; z-index: 200; overflow-y: auto;
}
.modal {
  background: white; border-radius: 12px; width: 680px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
}
.modal-large { width: 780px; }
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #f0f0f0;
}
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 24px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-group label { font-size: 14px; font-weight: 500; color: #555; }
.form-group input, .form-group select, .form-group textarea {
  padding: 9px 12px;
  border: 1px solid #ddd;
  border-radius: 7px;
  font-size: 14px;
  font-family: inherit;
  transition: border 0.2s;
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline: none; border-color: #667eea;
}
.form-group textarea { resize: vertical; line-height: 1.5; }
.form-hint { font-size: 12px; color: #999; }
.required { color: #f5222d; }
.var-hint {
  background: #f8f9ff;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
  line-height: 2;
}
.var-hint code {
  background: #e6f0ff;
  color: #667eea;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 11px;
}
.form-actions { display: flex; gap: 12px; justify-content: flex-end; }
.btn {
  padding: 9px 20px; border: none; border-radius: 7px;
  font-size: 14px; cursor: pointer; transition: all 0.2s;
}
.btn-primary { background: #667eea; color: white; }
.btn-primary:hover { background: #5a6fd6; }
.btn-primary:disabled { background: #aaa; cursor: not-allowed; }
.btn-default { background: #f5f5f5; color: #555; }
.btn-default:hover { background: #eee; }
</style>
