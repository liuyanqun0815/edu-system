<template>
  <div class="activity-page">
    <div class="page-header">
      <h1 class="page-title">活动管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建活动</button>
    </div>

    <div class="search-bar">
      <input v-model="searchForm.title" type="text" class="search-input" placeholder="活动标题" @keyup.enter="handleSearch">
      <select v-model="searchForm.status" class="search-select">
        <option value="">全部状态</option>
        <option :value="0">草稿</option>
        <option :value="1">已发布</option>
        <option :value="2">已结束</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>活动标题</th>
            <th>类型</th>
            <th>活动时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in activityList" :key="activity.id">
            <td>{{ activity.id }}</td>
            <td>
              <div class="activity-title-cell">
                <span class="type-icon" :style="{ background: activity.color || '#667eea' }">{{ activity.icon || typeIconMap[activity.activityType] }}</span>
                <span class="activity-title-text">
                  {{ activity.title }}
                  <span v-if="activity.isTop === 1" class="top-badge">置顶</span>
                </span>
              </div>
            </td>
            <td>
              <span :class="['type-badge', `type-${activity.activityType}`]">{{ typeMap[activity.activityType] }}</span>
            </td>
            <td>
              <div class="time-cell">
                <div class="time-row">
                  <span class="time-label">开始：</span>
                  <span>{{ formatDate(activity.startTime) || formatDate(activity.publishTime) }}</span>
                </div>
                <div class="time-row">
                  <span class="time-label">结束：</span>
                  <span>{{ formatDate(activity.endTime) || '长期' }}</span>
                </div>
              </div>
            </td>
            <td>
              <span :class="['status-tag', `status-${activity.status}`]">{{ statusMap[activity.status] }}</span>
            </td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(activity)">编辑</button>
              <button class="action-btn action-delete" @click="handleDelete(activity)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-wide">
        <div class="modal-header">
          <h3>{{ isEdit ? '✏️ 编辑活动' : '➕ 新建活动' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-section">
              <div class="section-title">基本信息</div>
              <div class="form-group">
                <label>活动标题 <span class="required">*</span></label>
                <input v-model="form.title" type="text" placeholder="请输入活动标题" required>
              </div>
              <div class="form-group">
                <label>活动内容</label>
                <textarea v-model="form.content" rows="4" placeholder="请输入活动详细内容"></textarea>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>活动类型</label>
                  <select v-model="form.activityType">
                    <option :value="1">📢 通知</option>
                    <option :value="2">🎉 活动</option>
                    <option :value="3">📝 考试</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>状态</label>
                  <select v-model="form.status">
                    <option :value="0">草稿</option>
                    <option :value="1">已发布</option>
                    <option :value="2">已结束</option>
                  </select>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>活动图标</label>
                  <div class="icon-selector">
                    <div 
                      v-for="icon in iconOptions" 
                      :key="icon" 
                      class="icon-option"
                      :class="{ active: form.icon === icon }"
                      @click="form.icon = icon"
                    >{{ icon }}</div>
                  </div>
                </div>
                <div class="form-group">
                  <label>主题颜色</label>
                  <div class="color-selector">
                    <div 
                      v-for="color in colorOptions" 
                      :key="color" 
                      class="color-option"
                      :style="{ background: color }"
                      :class="{ active: form.color === color }"
                      @click="form.color = color"
                    ></div>
                  </div>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="switch-label">
                    <span>是否置顶</span>
                    <label class="switch">
                      <input type="checkbox" v-model="form.isTop" :true-value="1" :false-value="0">
                      <span class="slider"></span>
                    </label>
                  </label>
                </div>
                <div class="form-group">
                  <label>排序值</label>
                  <input v-model.number="form.sort" type="number" placeholder="数值越小越靠前" min="0">
                </div>
              </div>
            </div>

            <div class="form-section">
              <div class="section-title">
                活动时间
                <span class="section-hint">（不填则使用默认值）</span>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>开始时间</label>
                  <input v-model="form.startTime" type="datetime-local" class="datetime-input">
                  <div class="field-hint">
                    <span class="hint-icon">💡</span>
                    不填则默认为发布时间
                  </div>
                </div>
                <div class="form-group">
                  <label>结束时间</label>
                  <input v-model="form.endTime" type="datetime-local" class="datetime-input">
                  <div class="field-hint">
                    <span class="hint-icon">💡</span>
                    不填则默认为长期有效
                  </div>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存活动' }}
              </button>
            </div>
          </form>
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

const typeMap = { 1: '通知', 2: '活动', 3: '考试' }
const typeIconMap = { 1: '📢', 2: '🎉', 3: '📝' }
const statusMap = { 0: '草稿', 1: '已发布', 2: '已结束' }

const searchForm = reactive({ title: '', status: '' })
const activityList = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive({ id: null, title: '', content: '', activityType: 1, status: 0, icon: '', color: '#667eea', isTop: 0, sort: 0, startTime: '', endTime: '' })

const iconOptions = ['📢', '🎉', '📝', '🔔', '📚', '💡', '⭐', '🎯', '🏆', '📅', '📌', '✨']
const colorOptions = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#43e97b', '#fa8c16', '#eb2f96', '#722ed1']

async function fetchActivityList() {
  try {
    const params = {}
    if (searchForm.title) params.title = searchForm.title
    if (searchForm.status !== '') params.status = searchForm.status
    const res = await request.get('/system/activity/list', { params })
    if (res.data.code === 200) activityList.value = res.data.data || []
  } catch (e) { console.error('获取活动列表失败', e) }
}

function handleSearch() { fetchActivityList() }
function resetSearch() { searchForm.title = ''; searchForm.status = ''; fetchActivityList() }

function openAddModal() {
  isEdit.value = false
  Object.assign(form, { id: null, title: '', content: '', activityType: 1, status: 0, icon: '', color: '#667eea', isTop: 0, sort: 0, startTime: '', endTime: '' })
  showModal.value = true
}

function handleEdit(activity) {
  isEdit.value = true
  Object.assign(form, { 
    ...activity, 
    startTime: formatDateTimeLocal(activity.startTime), 
    endTime: formatDateTimeLocal(activity.endTime),
    icon: activity.icon || '',
    color: activity.color || '#667eea',
    isTop: activity.isTop || 0,
    sort: activity.sort || 0
  })
  showModal.value = true
}

function closeModal() { showModal.value = false }

function formatDateTimeLocal(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().slice(0, 16)
}

async function handleSubmit() {
  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await request.put('/system/activity', data)
      toast.success('修改成功')
    } else {
      await request.post('/system/activity', data)
      toast.success('新增成功')
    }
    closeModal()
    fetchActivityList()
  } catch (e) { toast.error(e.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(activity) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除活动 <b>"${activity.title}"</b> 吗？`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/system/activity/${activity.id}`)
    toast.success('删除成功')
    fetchActivityList()
  } catch (e) { toast.error(e.response?.data?.msg || '删除失败') }
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

onMounted(fetchActivityList)
</script>

<style scoped>
.activity-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }
.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; transition: all 0.2s; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(102,126,234,0.4); }
.btn-default { background: #f0f0f0; color: #666; }

.search-bar { display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); flex-wrap: wrap; }
.search-input, .search-select { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; }
.search-input { width: 200px; }
.search-input:focus, .search-select:focus { border-color: #667eea; }

.table-container { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.data-table th { background: #f8f9fa; font-weight: 600; color: #333; font-size: 13px; }
.data-table tr:hover { background: #fafbfc; }

.activity-title-cell { display: flex; align-items: center; gap: 10px; }
.type-icon { font-size: 16px; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 8px; color: white; }
.activity-title-text { display: flex; align-items: center; gap: 8px; }
.top-badge { background: #ff4d4f; color: white; font-size: 10px; padding: 2px 6px; border-radius: 4px; font-weight: 500; }

.type-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.type-1 { background: #e6f7ff; color: #1890ff; }
.type-2 { background: #fff7e6; color: #fa8c16; }
.type-3 { background: #f9f0ff; color: #722ed1; }

.time-cell { font-size: 13px; }
.time-row { display: flex; gap: 4px; }
.time-label { color: #999; }

.status-tag { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-0 { background: #f0f0f0; color: #666; }
.status-1 { background: #f6ffed; color: #52c41a; }
.status-2 { background: #e6f7ff; color: #1890ff; }

.actions { display: flex; gap: 8px; }
.action-btn { padding: 5px 12px; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 600px; max-height: 90vh; overflow: auto; }
.modal-wide { max-width: 650px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; color: #333; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }

/* 表单分组 */
.form-section { margin-bottom: 24px; }
.section-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; gap: 8px; }
.section-hint { font-size: 12px; font-weight: 400; color: #999; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #555; font-weight: 500; }
.required { color: #ff4d4f; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 14px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; transition: border-color 0.2s; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color: #667eea; }
.datetime-input { background: #fafbfc; }

.field-hint { margin-top: 6px; font-size: 12px; color: #999; display: flex; align-items: center; gap: 4px; }
.hint-icon { font-size: 12px; }

.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #f0f0f0; }

/* 图标选择器 */
.icon-selector { display: flex; flex-wrap: wrap; gap: 8px; }
.icon-option { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border: 2px solid #e0e0e0; border-radius: 8px; cursor: pointer; font-size: 18px; transition: all 0.2s; }
.icon-option:hover { border-color: #667eea; background: #f5f7ff; }
.icon-option.active { border-color: #667eea; background: #667eea; }

/* 颜色选择器 */
.color-selector { display: flex; flex-wrap: wrap; gap: 8px; }
.color-option { width: 28px; height: 28px; border-radius: 50%; cursor: pointer; border: 2px solid transparent; transition: all 0.2s; }
.color-option:hover { transform: scale(1.1); }
.color-option.active { border-color: #333; box-shadow: 0 0 0 2px white, 0 0 0 4px #333; }

/* 开关样式 */
.switch-label { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0 !important; }
.switch { position: relative; display: inline-block; width: 44px; height: 22px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .3s; border-radius: 22px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; }
input:checked + .slider { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
input:checked + .slider:before { transform: translateX(22px); }

@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>
