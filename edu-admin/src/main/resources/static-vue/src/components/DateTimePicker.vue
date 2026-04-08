<template>
  <div class="datetime-picker-wrapper" :class="{ 'has-value': displayValue }">
    <input 
      ref="inputRef"
      type="text" 
      class="datetime-input"
      :value="displayValue"
      :placeholder="placeholder"
      readonly
      @click="togglePicker"
    >
    <span class="input-icon" @click="togglePicker">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
        <line x1="16" y1="2" x2="16" y2="6"></line>
        <line x1="8" y1="2" x2="8" y2="6"></line>
        <line x1="3" y1="10" x2="21" y2="10"></line>
      </svg>
    </span>
    <span v-if="clearable && displayValue" class="clear-btn" @click="clearValue">×</span>
    
    <transition name="picker-fade">
      <div v-if="showPicker" class="picker-dropdown" :class="placement">
        <!-- 日期选择 -->
        <div class="picker-header">
          <button class="nav-btn" @click="prevMonth">&lt;</button>
          <div class="current-date">
            <select v-model="currentYear" class="year-select">
              <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}年</option>
            </select>
            <select v-model="currentMonth" class="month-select">
              <option v-for="m in 12" :key="m" :value="m - 1">{{ m }}月</option>
            </select>
          </div>
          <button class="nav-btn" @click="nextMonth">&gt;</button>
        </div>
        
        <div class="picker-body">
          <div class="weekdays">
            <span v-for="d in weekDays" :key="d" class="weekday">{{ d }}</span>
          </div>
          <div class="days-grid">
            <button 
              v-for="(day, idx) in days" 
              :key="idx"
              :class="['day-btn', { 
                'other-month': day.otherMonth,
                'today': day.isToday,
                'selected': day.isSelected,
                'disabled': day.disabled
              }]"
              :disabled="day.disabled"
              @click="selectDay(day)"
            >
              {{ day.date }}
            </button>
          </div>
        </div>
        
        <!-- 时间选择 -->
        <div v-if="type === 'datetime'" class="time-picker">
          <div class="time-inputs">
            <select v-model="selectedHour" class="time-select">
              <option v-for="h in 24" :key="h" :value="h - 1">{{ String(h - 1).padStart(2, '0') }}</option>
            </select>
            <span class="time-sep">:</span>
            <select v-model="selectedMinute" class="time-select">
              <option v-for="m in 60" :key="m" :value="m - 1">{{ String(m - 1).padStart(2, '0') }}</option>
            </select>
          </div>
        </div>
        
        <div class="picker-footer">
          <button class="footer-btn now-btn" @click="selectNow">此刻</button>
          <button class="footer-btn confirm-btn" @click="confirm">确定</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Date], default: '' },
  type: { type: String, default: 'datetime' }, // date | datetime
  placeholder: { type: String, default: '请选择日期时间' },
  clearable: { type: Boolean, default: true },
  placement: { type: String, default: 'bottom-start' },
  minDate: { type: [String, Date], default: null },
  maxDate: { type: [String, Date], default: null }
})

const emit = defineEmits(['update:modelValue', 'change'])

const inputRef = ref(null)
const showPicker = ref(false)
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth())
const selectedHour = ref(0)
const selectedMinute = ref(0)
const selectedDate = ref(null)

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const yearOptions = computed(() => {
  const current = new Date().getFullYear()
  return Array.from({ length: 10 }, (_, i) => current - 5 + i)
})

const displayValue = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  if (props.type === 'date') {
    return `${y}-${m}-${day}`
  }
  const h = String(selectedHour.value).padStart(2, '0')
  const min = String(selectedMinute.value).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
})

const days = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const result = []
  
  // 上月日期
  const firstDayWeek = firstDay.getDay()
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    result.push({
      date: prevMonthLastDay - i,
      otherMonth: true,
      disabled: false,
      fullDate: new Date(year, month - 1, prevMonthLastDay - i)
    })
  }
  
  // 当月日期
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const d = new Date(year, month, i)
    d.setHours(0, 0, 0, 0)
    result.push({
      date: i,
      otherMonth: false,
      isToday: d.getTime() === today.getTime(),
      isSelected: selectedDate.value && isSameDay(d, selectedDate.value),
      disabled: isDisabled(d),
      fullDate: d
    })
  }
  
  // 下月日期
  const remaining = 42 - result.length
  for (let i = 1; i <= remaining; i++) {
    result.push({
      date: i,
      otherMonth: true,
      disabled: false,
      fullDate: new Date(year, month + 1, i)
    })
  }
  
  return result
})

function isSameDay(d1, d2) {
  if (!d1 || !d2) return false
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}

function isDisabled(date) {
  if (props.minDate) {
    const min = new Date(props.minDate)
    min.setHours(0, 0, 0, 0)
    if (date < min) return true
  }
  if (props.maxDate) {
    const max = new Date(props.maxDate)
    max.setHours(23, 59, 59, 999)
    if (date > max) return true
  }
  return false
}

function togglePicker() {
  showPicker.value = !showPicker.value
}

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

function selectDay(day) {
  if (day.disabled || day.otherMonth) return
  selectedDate.value = day.fullDate
}

function selectNow() {
  const now = new Date()
  selectedDate.value = now
  selectedHour.value = now.getHours()
  selectedMinute.value = now.getMinutes()
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth()
  confirm()
}

function clearValue() {
  selectedDate.value = null
  emit('update:modelValue', '')
  emit('change', '')
}

function confirm() {
  if (!selectedDate.value) return
  
  const d = new Date(selectedDate.value)
  if (props.type === 'datetime') {
    d.setHours(selectedHour.value, selectedMinute.value, 0)
  } else {
    d.setHours(0, 0, 0)
  }
  
  const value = props.type === 'datetime' 
    ? d.toISOString().slice(0, 16)
    : d.toISOString().slice(0, 10)
  
  emit('update:modelValue', value)
  emit('change', value)
  showPicker.value = false
}

// 点击外部关闭
function handleClickOutside(e) {
  if (inputRef.value && !inputRef.value.closest('.datetime-picker-wrapper').contains(e.target)) {
    showPicker.value = false
  }
}

// 初始化
watch(() => props.modelValue, (val) => {
  if (val) {
    const d = new Date(val)
    if (!isNaN(d.getTime())) {
      selectedDate.value = d
      currentYear.value = d.getFullYear()
      currentMonth.value = d.getMonth()
      if (props.type === 'datetime') {
        selectedHour.value = d.getHours()
        selectedMinute.value = d.getMinutes()
      }
    }
  }
}, { immediate: true })

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.datetime-picker-wrapper {
  position: relative;
  display: inline-block;
  width: 100%;
}

.datetime-input {
  width: 100%;
  padding: 10px 35px 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background: #fff;
  cursor: pointer;
  outline: none;
  transition: all 0.2s;
}

.datetime-input:hover {
  border-color: #c0c0c0;
}

.datetime-picker-wrapper.has-value .datetime-input,
.datetime-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.datetime-input::placeholder {
  color: #bfbfbf;
}

.input-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.clear-btn {
  position: absolute;
  right: 35px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #d9d9d9;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.datetime-picker-wrapper:hover .clear-btn {
  opacity: 1;
}

.picker-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 1000;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  min-width: 280px;
}

.picker-dropdown.bottom-end {
  left: auto;
  right: 0;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.nav-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.current-date {
  display: flex;
  gap: 8px;
}

.year-select, .month-select {
  padding: 4px 8px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  outline: none;
}

.year-select option, .month-select option {
  background: #fff;
  color: #333;
}

.picker-body {
  padding: 12px;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px 0;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.day-btn {
  width: 100%;
  aspect-ratio: 1;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.day-btn:hover:not(.disabled):not(.other-month) {
  background: #f0f2ff;
  color: #667eea;
}

.day-btn.other-month {
  color: #ccc;
}

.day-btn.today {
  color: #667eea;
  font-weight: 600;
}

.day-btn.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.day-btn.disabled {
  color: #d9d9d9;
  cursor: not-allowed;
}

.time-picker {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}

.time-inputs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.time-select {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  cursor: pointer;
}

.time-select:focus {
  border-color: #667eea;
}

.time-sep {
  font-size: 18px;
  font-weight: bold;
  color: #666;
}

.picker-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

.footer-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.now-btn {
  background: #f0f0f0;
  color: #666;
}

.now-btn:hover {
  background: #e0e0e0;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.35);
}

/* 动画 */
.picker-fade-enter-active,
.picker-fade-leave-active {
  transition: all 0.2s ease;
}

.picker-fade-enter-from,
.picker-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
