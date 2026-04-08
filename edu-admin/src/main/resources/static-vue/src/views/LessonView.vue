<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">课程排课</h1>
      <button class="btn btn-primary" @click="openAdd">+ 新增排课</button>
    </div>

    <!-- 日期筛选 -->
    <div class="search-bar">
      <div class="date-picker-wrapper">
        <input 
          :value="formatDateForInput(filterDate)" 
          type="text" 
          class="search-input date-input" 
          placeholder="选择日期"
          readonly
          @click="showDatePicker = !showDatePicker"
        >
        <div v-if="showDatePicker" class="date-picker-dropdown">
          <div class="date-picker-header">
            <button class="nav-btn" @click="prevMonth">&lt;</button>
            <span class="current-month">{{ pickerYear }}年{{ pickerMonth + 1 }}月</span>
            <button class="nav-btn" @click="nextMonth">&gt;</button>
          </div>
          <div class="date-picker-weekdays">
            <span v-for="day in ['日','一','二','三','四','五','六']" :key="day" class="weekday">{{ day }}</span>
          </div>
          <div class="date-picker-days">
            <span 
              v-for="date in calendarDays" 
              :key="date.key"
              :class="['day-cell', {
                'other-month': !date.isCurrentMonth,
                'selected': date.date === filterDate,
                'today': date.isToday
              }]"
              @click="selectDate(date.date)"
            >
              {{ date.day }}
            </span>
          </div>
        </div>
      </div>
      <button class="btn btn-default" @click="goToday">今天</button>
      <button class="btn btn-primary" @click="loadList">查询</button>
    </div>

    <!-- 排课列表 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>课程名称</th>
            <th v-if="currentRoleLevel >= ROLE_LEVEL.ADMIN">授课教师</th>
            <th>上课时间</th>
            <th>上课地点</th>
            <th v-if="currentRoleLevel >= ROLE_LEVEL.TEACHER">状态</th>
            <th v-if="currentRoleLevel >= ROLE_LEVEL.TEACHER">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="lesson in list" :key="lesson.id">
            <td>{{ lesson.courseName }}</td>
            <td v-if="currentRoleLevel >= ROLE_LEVEL.ADMIN">{{ lesson.teacherName }}</td>
            <td>
              <div>{{ lesson.lessonDate }}</div>
              <div class="time-range">{{ lesson.startTime ? lesson.startTime.substring(0,5) : '' }} — {{ lesson.endTime ? lesson.endTime.substring(0,5) : '' }}</div>
            </td>
            <td class="loc-cell">{{ lesson.location || '—' }}</td>
            <td v-if="currentRoleLevel >= ROLE_LEVEL.TEACHER">
              <span :class="['status-tag', lesson.status === 1 ? 'status-enabled' : 'status-disabled']">
                {{ lesson.status === 1 ? '正常' : '取消' }}
              </span>
            </td>
            <td v-if="currentRoleLevel >= ROLE_LEVEL.TEACHER" class="actions">
              <button class="action-btn action-edit" @click="openEdit(lesson)">编辑</button>
              <button v-if="currentRoleLevel >= ROLE_LEVEL.ADMIN" class="action-btn action-delete" @click="handleDelete(lesson)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="list.length === 0" class="empty-tip">暂无排课数据</div>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑排课' : '新增排课' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>课程名称 <span class="required">*</span></label>
                <input v-model="form.courseName" type="text" placeholder="请输入课程名称" required>
              </div>
              <div class="form-group">
                <label>授课教师 <span class="required">*</span></label>
                <select v-model="form.teacherId" required @change="onTeacherChange">
                  <option :value="null" disabled>请选择教师</option>
                  <option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.nickname || t.username }}</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>上课日期 <span class="required">*</span></label>
                <div class="date-picker-wrapper form-date-picker">
                  <input 
                    :value="formatDateForInput(form.lessonDate)" 
                    type="text" 
                    class="date-input" 
                    placeholder="选择日期"
                    readonly
                    required
                    @click="showFormDatePicker = !showFormDatePicker"
                  >
                  <div v-if="showFormDatePicker" class="date-picker-dropdown">
                    <div class="date-picker-header">
                      <button class="nav-btn" @click="prevFormMonth">&lt;</button>
                      <span class="current-month">{{ formPickerYear }}年{{ formPickerMonth + 1 }}月</span>
                      <button class="nav-btn" @click="nextFormMonth">&gt;</button>
                    </div>
                    <div class="date-picker-weekdays">
                      <span v-for="day in ['日','一','二','三','四','五','六']" :key="day" class="weekday">{{ day }}</span>
                    </div>
                    <div class="date-picker-days">
                      <span 
                        v-for="date in formCalendarDays" 
                        :key="date.key"
                        :class="['day-cell', {
                          'other-month': !date.isCurrentMonth,
                          'selected': date.date === form.lessonDate,
                          'today': date.isToday
                        }]"
                        @click="selectFormDate(date.date)"
                      >
                        {{ date.day }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="1">正常</option>
                  <option :value="0">取消</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>开始时间 <span class="required">*</span></label>
                <input v-model="form.startTime" type="time" required>
              </div>
              <div class="form-group">
                <label>结束时间 <span class="required">*</span></label>
                <input v-model="form.endTime" type="time" required>
              </div>
            </div>
            <div class="form-group">
              <label>上课地点</label>
              <input v-model="form.location" type="text" placeholder="请输入详细地址（用于通勤时间估算）">
            </div>
            <div class="form-group">
              <label>备注</label>
              <input v-model="form.remark" type="text" placeholder="备注说明">
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
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

// 角色等级映射
const ROLE_LEVEL = { STUDENT: 0, TEACHER: 1, ADMIN: 2, SUPER_ADMIN: 3 }

// 当前用户角色等级（从后端获取）
const currentRoleLevel = ref(ROLE_LEVEL.SUPER_ADMIN)

const list = ref([])
const teachers = ref([])
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const showDatePicker = ref(false)
const showFormDatePicker = ref(false)

// 列表日期选择器相关
const pickerYear = ref(new Date().getFullYear())
const pickerMonth = ref(new Date().getMonth())

// 表单日期选择器相关
const formPickerYear = ref(new Date().getFullYear())
const formPickerMonth = ref(new Date().getMonth())

const formCalendarDays = computed(() => {
  const days = []
  const firstDay = new Date(formPickerYear.value, formPickerMonth.value, 1)
  const lastDay = new Date(formPickerYear.value, formPickerMonth.value + 1, 0)
  const startWeekday = firstDay.getDay()
  
  const prevMonthLastDay = new Date(formPickerYear.value, formPickerMonth.value, 0).getDate()
  for (let i = startWeekday - 1; i >= 0; i--) {
    const d = prevMonthLastDay - i
    days.push({
      key: `prev-${d}`,
      day: d,
      date: formatDateStr(formPickerYear.value, formPickerMonth.value - 1, d),
      isCurrentMonth: false,
      isToday: false
    })
  }
  
  const today = new Date()
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push({
      key: `curr-${i}`,
      day: i,
      date: formatDateStr(formPickerYear.value, formPickerMonth.value, i),
      isCurrentMonth: true,
      isToday: formPickerYear.value === today.getFullYear() && 
               formPickerMonth.value === today.getMonth() && 
               i === today.getDate()
    })
  }
  
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      key: `next-${i}`,
      day: i,
      date: formatDateStr(formPickerYear.value, formPickerMonth.value + 1, i),
      isCurrentMonth: false,
      isToday: false
    })
  }
  
  return days
})

function prevFormMonth() {
  if (formPickerMonth.value === 0) {
    formPickerMonth.value = 11
    formPickerYear.value--
  } else {
    formPickerMonth.value--
  }
}

function nextFormMonth() {
  if (formPickerMonth.value === 11) {
    formPickerMonth.value = 0
    formPickerYear.value++
  } else {
    formPickerMonth.value++
  }
}

function selectFormDate(date) {
  form.value.lessonDate = date
  showFormDatePicker.value = false
}

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(pickerYear.value, pickerMonth.value, 1)
  const lastDay = new Date(pickerYear.value, pickerMonth.value + 1, 0)
  const startWeekday = firstDay.getDay()
  
  // 上月日期
  const prevMonthLastDay = new Date(pickerYear.value, pickerMonth.value, 0).getDate()
  for (let i = startWeekday - 1; i >= 0; i--) {
    const d = prevMonthLastDay - i
    days.push({
      key: `prev-${d}`,
      day: d,
      date: formatDateStr(pickerYear.value, pickerMonth.value - 1, d),
      isCurrentMonth: false,
      isToday: false
    })
  }
  
  // 当月日期
  const today = new Date()
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push({
      key: `curr-${i}`,
      day: i,
      date: formatDateStr(pickerYear.value, pickerMonth.value, i),
      isCurrentMonth: true,
      isToday: pickerYear.value === today.getFullYear() && 
               pickerMonth.value === today.getMonth() && 
               i === today.getDate()
    })
  }
  
  // 下月日期
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      key: `next-${i}`,
      day: i,
      date: formatDateStr(pickerYear.value, pickerMonth.value + 1, i),
      isCurrentMonth: false,
      isToday: false
    })
  }
  
  return days
})

function formatDateStr(year, month, day) {
  const d = new Date(year, month, day)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDateForInput(dateStr) {
  if (!dateStr) return ''
  // 确保显示 yyyy-MM-dd 格式
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return `${parts[0]}-${parts[1]}-${parts[2]}`
  }
  return dateStr
}

function prevMonth() {
  if (pickerMonth.value === 0) {
    pickerMonth.value = 11
    pickerYear.value--
  } else {
    pickerMonth.value--
  }
}

function nextMonth() {
  if (pickerMonth.value === 11) {
    pickerMonth.value = 0
    pickerYear.value++
  } else {
    pickerMonth.value++
  }
}

function selectDate(date) {
  filterDate.value = date
  showDatePicker.value = false
  loadList()
}

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
const filterDate = ref(today())

const defaultForm = () => ({
  courseName: '', courseId: null, teacherId: null, teacherName: '',
  location: '', lessonDate: today(), startTime: '09:00', endTime: '10:30',
  status: 1, remark: ''
})
const form = ref(defaultForm())

onMounted(() => { loadList(); loadTeachers(); loadCurrentUserRole() })

async function loadCurrentUserRole() {
  try {
    const res = await request.get('/system/user/info')
    if (res.data.code === 200 && res.data.data) {
      // 从用户信息中获取角色等级
      const user = res.data.data
      if (user.roleLevel !== undefined) {
        currentRoleLevel.value = user.roleLevel
      }
    }
  } catch (e) {}
}

async function loadList() {
  try {
    const res = await request.get('/system/lesson/page', { params: { date: filterDate.value } })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { console.error(e) }
}

async function loadTeachers() {
  try {
    // 获取教师角色的用户（roleLevel >= 1）
    const res = await request.get('/system/user/teachers')
    if (res.data.code === 200) {
      teachers.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载教师列表失败', e)
  }
}

function goToday() { 
  filterDate.value = today()
  const d = new Date()
  pickerYear.value = d.getFullYear()
  pickerMonth.value = d.getMonth()
  loadList() 
}

function onTeacherChange() {
  const t = teachers.value.find(u => u.id === form.value.teacherId)
  if (t) form.value.teacherName = t.nickname || t.username
}

function openAdd() {
  isEdit.value = false
  form.value = defaultForm()
  form.value.lessonDate = filterDate.value
  // 同步日期选择器到当前表单日期
  const d = new Date(form.value.lessonDate)
  formPickerYear.value = d.getFullYear()
  formPickerMonth.value = d.getMonth()
  showModal.value = true
}

function openEdit(lesson) {
  isEdit.value = true
  form.value = {
    ...lesson,
    startTime: lesson.startTime ? String(lesson.startTime).substring(0, 5) : '09:00',
    endTime: lesson.endTime ? String(lesson.endTime).substring(0, 5) : '10:30',
  }
  // 同步日期选择器到当前表单日期
  const d = new Date(form.value.lessonDate || today())
  formPickerYear.value = d.getFullYear()
  formPickerMonth.value = d.getMonth()
  showModal.value = true
}

function closeModal() { 
  showModal.value = false 
  showFormDatePicker.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/system/lesson', form.value)
    } else {
      await request.post('/system/lesson', form.value)
    }
    closeModal()
    await loadList()
  } catch (e) {
    alert('保存失败：' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

async function handleDelete(lesson) {
  if (!confirm(`确认删除"${lesson.courseName}"这节课？`)) return
  try {
    await request.delete(`/system/lesson/${lesson.id}`)
    await loadList()
  } catch (e) { alert('删除失败') }
}
</script>

<style scoped>
.page { padding: 24px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-title { font-size: 20px; font-weight: 700; color: #333; margin: 0; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.search-input {
  padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; min-width: 160px;
}
.table-container {
  background: white; border-radius: 10px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  background: #f8f9fa; padding: 12px 14px; text-align: left;
  font-size: 13px; font-weight: 600; color: #666; border-bottom: 1px solid #eee;
}
.data-table td { padding: 12px 14px; font-size: 13px; border-bottom: 1px solid #f5f5f5; }
.time-range { font-size: 12px; color: #667eea; margin-top: 2px; }
.loc-cell { max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.status-tag { padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.status-enabled { background: #f6ffed; color: #52c41a; }
.status-disabled { background: #fff1f0; color: #f5222d; }
.actions { display: flex; gap: 6px; }
.action-btn { padding: 4px 10px; border: none; border-radius: 5px; font-size: 12px; cursor: pointer; }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-delete { background: #fff1f0; color: #f5222d; }
.empty-tip { text-align: center; color: #999; padding: 40px; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); display: flex;
  align-items: flex-start; justify-content: center;
  padding: 60px 20px 20px; z-index: 200;
}
.modal {
  background: white; border-radius: 12px; width: 640px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #f0f0f0;
}
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 24px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 0; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-group label { font-size: 14px; font-weight: 500; color: #555; }
.form-group input, .form-group select {
  padding: 9px 12px; border: 1px solid #ddd; border-radius: 7px;
  font-size: 14px; transition: border 0.2s;
}
.form-group input:focus, .form-group select:focus { outline: none; border-color: #667eea; }
.required { color: #f5222d; }
.form-actions { display: flex; gap: 12px; justify-content: flex-end; }
.btn { padding: 9px 20px; border: none; border-radius: 7px; font-size: 14px; cursor: pointer; }
.btn-primary { background: #667eea; color: white; }
.btn-primary:hover { background: #5a6fd6; }
.btn-primary:disabled { background: #aaa; cursor: not-allowed; }
.btn-default { background: #f5f5f5; color: #555; }
.btn-default:hover { background: #eee; }

/* 日期选择器样式 */
.date-picker-wrapper {
  position: relative;
  display: inline-block;
}

.date-input {
  width: 140px;
  cursor: pointer;
  background: white;
  text-align: center;
  font-family: 'Courier New', monospace;
  letter-spacing: 0.5px;
}

.date-picker-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 100;
  width: 280px;
  padding: 12px;
}

.date-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
}

.nav-btn {
  background: #f5f5f5;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.nav-btn:hover {
  background: #667eea;
  color: white;
}

.current-month {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.date-picker-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px;
}

.date-picker-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
}

.day-cell:hover {
  background: #f0f0f0;
}

.day-cell.other-month {
  color: #ccc;
}

.day-cell.today {
  border: 1px solid #667eea;
  color: #667eea;
  font-weight: 600;
}

.day-cell.selected {
  background: #667eea;
  color: white;
  font-weight: 600;
}

.day-cell.selected:hover {
  background: #5a6fd6;
}

/* 表单内日期选择器样式 */
.form-date-picker {
  width: 100%;
}

.form-date-picker .date-input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #ddd;
  border-radius: 7px;
  font-size: 14px;
  cursor: pointer;
  background: white;
  text-align: left;
  font-family: 'Courier New', monospace;
}

.form-date-picker .date-input:focus {
  outline: none;
  border-color: #667eea;
}
</style>
