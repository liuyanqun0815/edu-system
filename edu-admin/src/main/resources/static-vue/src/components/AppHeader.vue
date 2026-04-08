<template>
  <header class="header">
    <div class="logo" @click="goHome">
      <span>🎓</span>
      {{ systemName }}
    </div>
    <div class="header-right">
      <!-- 当天任务轮播（在日程左侧） -->
      <div v-if="todayTasksForCarousel.length > 0" class="header-task-carousel">
        <div class="task-carousel-container">
          <div class="task-carousel-track" :style="{ transform: `translateX(-${headerCarouselIndex * 100}%)` }">
            <div v-for="task in todayTasksForCarousel" :key="task.id" class="header-task-item">
              <span class="task-icon">{{ priorityIcon(task.priority) }}</span>
              <span class="task-title" :title="task.title">{{ task.title }}</span>
              <span v-if="task.startTime" class="task-time">{{ fmtTime(task.startTime) }}</span>
            </div>
          </div>
        </div>
        <div class="task-carousel-dots" v-if="todayTasksForCarousel.length > 1">
          <span v-for="(t, i) in todayTasksForCarousel" :key="i" :class="['dot', i === headerCarouselIndex ? 'active' : '']" @click="headerCarouselIndex = i"></span>
        </div>
      </div>

      <!-- 日历代办插件按钮 -->
      <div class="calendar-btn-wrap">
        <div class="calendar-btn" @click="toggleCalendar" :class="{ active: showCalendar }">
          <span class="cal-icon">📅</span>
          <span class="cal-label">日程</span>
          <span v-if="todayTaskCount > 0" class="cal-badge">{{ todayTaskCount }}</span>
        </div>

        <!-- 日历弹窗 - 完整日历视图 -->
        <div v-if="showCalendar" class="calendar-popup" @click.stop>
          <div class="cal-popup-header">
            <div class="cal-date-nav">
              <button class="cal-nav-btn" @click="changeMonth(-1)">‹</button>
              <span class="cal-current-date">{{ calendarYear }}年{{ calendarMonth + 1 }}月</span>
              <button class="cal-nav-btn" @click="changeMonth(1)">›</button>
              <button class="cal-today-btn" @click="goToCurrentMonth">今天</button>
            </div>
            <button class="cal-close" @click="showCalendar = false">&times;</button>
          </div>

          <!-- 完整日历 -->
          <div class="full-calendar">
            <div class="calendar-weekdays">
              <span v-for="day in ['日','一','二','三','四','五','六']" :key="day" class="weekday">{{ day }}</span>
            </div>
            <div class="calendar-days">
              <div 
                v-for="date in calendarDays" 
                :key="date.key"
                :class="['calendar-day', {
                  'other-month': !date.isCurrentMonth,
                  'today': date.isToday,
                  'selected': date.date === selectedDate,
                  'has-tasks': date.taskCount > 0,
                  'has-lessons': date.lessonCount > 0
                }]"
                @click="selectCalendarDate(date)"
              >
                <span class="day-number">{{ date.day }}</span>
                <div class="day-indicators">
                  <span v-if="date.taskCount > 0" class="indicator task-dot" :title="`${date.taskCount}个任务`">{{ date.taskCount }}</span>
                  <span v-if="date.lessonCount > 0" class="indicator lesson-dot" :title="`${date.lessonCount}节课`">{{ date.lessonCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 选中日期详情 -->
          <div class="selected-date-details" v-if="selectedDate">
            <div class="details-header">
              <span class="details-date">{{ formatCalDate(new Date(selectedDate)) }}</span>
              <span class="details-count" v-if="selectedDateLessons.length > 0 || selectedDateTasks.length > 0">
                {{ selectedDateLessons.length }}节课 / {{ selectedDateTasks.length }}个任务
              </span>
            </div>
            <div class="details-body">
              <div v-if="selectedDateLessons.length === 0 && selectedDateTasks.length === 0" class="details-empty">
                暂无课程或任务
              </div>
              <!-- 课程列表 -->
              <div v-if="selectedDateLessons.length > 0" class="details-section">
                <div class="details-section-title">📚 课程</div>
                <div v-for="lesson in selectedDateLessons" :key="lesson.id" class="details-item">
                  <span class="item-time">{{ fmtTime(lesson.startTime) }}-{{ fmtTime(lesson.endTime) }}</span>
                  <span class="item-name">{{ lesson.courseName }}</span>
                  <span v-if="lesson.teacherName" class="item-sub">{{ lesson.teacherName }}</span>
                </div>
              </div>
              <!-- 任务列表 -->
              <div v-if="selectedDateTasks.length > 0" class="details-section">
                <div class="details-section-title">✅ 任务</div>
                <div v-for="task in selectedDateTasks" :key="task.id" class="details-item task-item">
                  <span class="item-priority" :class="'prio-' + task.priority">{{ priorityLabel(task.priority) }}</span>
                  <span class="item-name" :class="{ 'task-done': task.status === 1 }">{{ task.title }}</span>
                  <button class="item-action" @click="toggleTaskDone(task)">{{ task.status === 1 ? '✓' : '○' }}</button>
                </div>
              </div>
            </div>
          </div>

          <div class="cal-footer">
            <router-link to="/lesson" class="link-btn" @click="showCalendar = false">查看排课 →</router-link>
          </div>
        </div>
      </div>

      <!-- 在线人数（仅非学生角色显示） -->
      <div v-if="showOnlineBadge" class="online-badge clickable" @click="showOnlineUsers = true">
        <span class="online-dot"></span>
        <span>在线 {{ onlineCount }} 人</span>
        <span class="view-icon">👁</span>
      </div>


      <div class="user-info">
        <div class="avatar" @click="goProfile">
          <img v-if="userStore.avatar" :src="userStore.avatar" alt="avatar">
          <span v-else>👤</span>
        </div>
        <span class="username">{{ userStore.nickname || userStore.username || '管理员' }}</span>
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>

    <!-- 在线用户详情弹窗 -->
    <div v-if="showOnlineUsers" class="online-modal-overlay" @click.self="showOnlineUsers = false">
      <div class="online-modal">
        <div class="modal-header">
          <h3>🟢 在线用户 ({{ onlineCount }}人)</h3>
          <button class="close-btn" @click="showOnlineUsers = false">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 根据层级展示 -->
          <div v-if="groupedUsers.length > 0">
            <div v-for="group in groupedUsers" :key="group.roleId" class="role-group">
              <!-- 组头（可点击折叠/展开） -->
              <div class="group-header" @click="toggleGroup(group.roleId)">
                <span class="group-icon">{{ group.collapsed ? '▶' : '▼' }}</span>
                <span class="group-name">{{ group.roleName }}</span>
                <span class="group-count">({{ group.users.length }}人在线)</span>
              </div>
              <!-- 组内用户列表 -->
              <div v-show="!group.collapsed" class="group-users">
                <!-- 按上级分组（如果是学生层） -->
                <template v-if="group.roleLevel === 0">
                  <div v-for="parentGroup in getParentGroups(group.users)" :key="parentGroup.parentId" class="parent-group">
                    <div class="parent-header" @click="toggleParentGroup(group.roleId, parentGroup.parentId)">
                      <span class="parent-icon">{{ parentGroup.collapsed ? '▶' : '▼' }}</span>
                      <span class="parent-name">👨‍🏫 {{ parentGroup.parentName }}</span>
                      <span class="parent-count">({{ parentGroup.students.length }})
                      </span>
                    </div>
                    <div v-show="!parentGroup.collapsed" class="student-grid">
                      <div v-for="user in parentGroup.students" :key="user.id" class="online-user-card">
                        <div class="user-avatar" :style="{ background: getColor(user.id) }">
                          {{ (user.nickname || user.username || '?').charAt(0).toUpperCase() }}
                        </div>
                        <div class="user-info-text">
                          <div class="user-name">{{ user.nickname || user.username }}</div>
                          <div class="user-role">{{ group.roleName }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
                <!-- 非学生层直接显示 -->
                <template v-else>
                  <div class="online-user-grid">
                    <div v-for="user in group.users" :key="user.id" class="online-user-card">
                      <div class="user-avatar" :style="{ background: getColor(user.id) }">
                        {{ (user.nickname || user.username || '?').charAt(0).toUpperCase() }}
                      </div>
                      <div class="user-info-text">
                        <div class="user-name">{{ user.nickname || user.username }}</div>
                        <div class="user-role">{{ group.roleName }}</div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">暂无在线用户</div>
        </div>
        <div class="modal-footer">
          <router-link to="/user" class="link-btn" @click="showOnlineUsers = false">前往用户管理 →</router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const onlineCount = ref(0)
const onlineUsers = ref([])   // 原始用户列表（含 roleLevel/roleId/parentId/parentName）
const showOnlineUsers = ref(false)
const showOnlineBadge = ref(true)
const systemName = ref('智慧教培管理系统')
const collapsedGroups = ref({})     // { roleId: true/false }
const collapsedParents = ref({})    // { 'roleId_parentId': true/false }
let ws = null
let wsTimer = null

// 日历代办插件 - 新版完整日历
const showCalendar = ref(false)
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth())
const selectedDate = ref('')
const monthData = ref({}) // 存储整月的数据 { '2026-03-13': { lessons: [], tasks: [] } }
const calLoading = ref(false)

// 头部轮播相关
const headerCarouselIndex = ref(0)
let headerCarouselTimer = null

// 选中日期的详情数据
const selectedDateLessons = computed(() => {
  if (!selectedDate.value) return []
  return monthData.value[selectedDate.value]?.lessons || []
})
const selectedDateTasks = computed(() => {
  if (!selectedDate.value) return []
  return monthData.value[selectedDate.value]?.tasks || []
})

// 当天任务（用于头部轮播）
const todayTasksForCarousel = computed(() => {
  const today = dateStr(new Date())
  return monthData.value[today]?.tasks?.filter(t => t.status !== 1) || []
})

const todayTaskCount = computed(() => todayTasksForCarousel.value.length)

function formatCalDate(d) {
  const dt = d instanceof Date ? d : new Date(d)
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${dt.getFullYear()}年${String(dt.getMonth()+1).padStart(2,'0')}月${String(dt.getDate()).padStart(2,'0')}日 周${weekDays[dt.getDay()]}`
}
function dateStr(d) {
  const dt = d instanceof Date ? d : new Date(d)
  return `${dt.getFullYear()}-${String(dt.getMonth()+1).padStart(2,'0')}-${String(dt.getDate()).padStart(2,'0')}`
}
function fmtTime(t) { return t ? String(t).substring(0, 5) : '' }
function priorityIcon(p) { return p === 3 ? '🔴' : p === 2 ? '🟡' : '🟢' }
function priorityLabel(p) { return p === 3 ? '高' : p === 2 ? '中' : '低' }

// 完整日历数据
const fullCalendarDays = computed(() => {
  const days = []
  const year = calendarYear.value
  const month = calendarMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startWeekday = firstDay.getDay()
  
  // 上月日期
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = startWeekday - 1; i >= 0; i--) {
    const d = prevMonthLastDay - i
    const dateStr = formatDateStr(year, month - 1, d)
    days.push(createDayObj(d, dateStr, false))
  }
  
  // 当月日期
  const today = new Date()
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const dateStr = formatDateStr(year, month, i)
    const isToday = year === today.getFullYear() && month === today.getMonth() && i === today.getDate()
    days.push(createDayObj(i, dateStr, true, isToday))
  }
  
  // 下月日期
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const dateStr = formatDateStr(year, month + 1, i)
    days.push(createDayObj(i, dateStr, false))
  }
  
  return days
})

function createDayObj(day, dateStr, isCurrentMonth, isToday = false) {
  const data = monthData.value[dateStr] || {}
  return {
    key: dateStr,
    day,
    date: dateStr,
    isCurrentMonth,
    isToday,
    taskCount: data.tasks?.filter(t => t.status !== 1).length || 0,
    lessonCount: data.lessons?.length || 0
  }
}

function formatDateStr(year, month, day) {
  const d = new Date(year, month, day)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadMonthData() {
  calLoading.value = true
  try {
    // 获取当前月的第一天和最后一天
    const year = calendarYear.value
    const month = calendarMonth.value
    const startDate = formatDateStr(year, month, 1)
    const endDate = formatDateStr(year, month + 1, 0)
    
    const res = await request.get('/system/lesson/calendar/month', { 
      params: { startDate, endDate } 
    })
    if (res.data.code === 200) {
      monthData.value = res.data.data || {}
    }
  } catch (e) { 
    console.error('加载日历数据失败', e)
  } finally { 
    calLoading.value = false 
  }
}

function toggleCalendar() {
  showCalendar.value = !showCalendar.value
  if (showCalendar.value) { 
    loadMonthData()
    // 默认选中今天
    const today = dateStr(new Date())
    selectedDate.value = today
  }
}

function changeMonth(delta) {
  let newMonth = calendarMonth.value + delta
  let newYear = calendarYear.value
  if (newMonth < 0) {
    newMonth = 11
    newYear--
  } else if (newMonth > 11) {
    newMonth = 0
    newYear++
  }
  calendarMonth.value = newMonth
  calendarYear.value = newYear
  loadMonthData()
}

function goToCurrentMonth() {
  const now = new Date()
  calendarYear.value = now.getFullYear()
  calendarMonth.value = now.getMonth()
  selectedDate.value = dateStr(now)
  loadMonthData()
}

function selectCalendarDate(dateObj) {
  selectedDate.value = dateObj.date
}

async function toggleTaskDone(task) {
  const s = task.status === 1 ? 0 : 1
  try { 
    await request.put(`/system/task/${task.id}/status`, null, { params: { status: s } })
    task.status = s
    // 刷新数据
    loadMonthData()
  } catch (e) {}
}

// 头部轮播控制
function startHeaderCarousel() {
  stopHeaderCarousel()
  headerCarouselTimer = setInterval(() => {
    if (todayTasksForCarousel.value.length > 1) {
      headerCarouselIndex.value = (headerCarouselIndex.value + 1) % todayTasksForCarousel.value.length
    }
  }, 4000)
}

function stopHeaderCarousel() {
  if (headerCarouselTimer) {
    clearInterval(headerCarouselTimer)
    headerCarouselTimer = null
  }
}

onMounted(() => {
  startHeaderCarousel()
})

onUnmounted(() => {
  stopHeaderCarousel()
})

const colors = ['#667eea','#f5576c','#43e97b','#fa709a','#4facfe','#f6d365']
function getColor(id) { return colors[(id || 0) % colors.length] }

// 按角色分组（取用户列表中的 roleLevel/roleId/roleName 字段）
const groupedUsers = computed(() => {
  const map = {}
  for (const user of onlineUsers.value) {
    const key = user.roleId || 0
    if (!map[key]) {
      map[key] = {
        roleId: user.roleId || 0,
        roleName: user.roleName || '未知角色',
        roleLevel: user.roleLevel !== undefined ? user.roleLevel : -1,
        users: [],
        collapsed: collapsedGroups.value[key] || false
      }
    }
    map[key].users.push(user)
  }
  // 按 roleLevel 降序排列（高权限在前）
  return Object.values(map).sort((a, b) => b.roleLevel - a.roleLevel)
})

// 将学生列表按上级教师分组
function getParentGroups(students) {
  const map = {}
  for (const s of students) {
    const pid = s.parentId || 0
    const pname = s.parentName || '未分配教师'
    if (!map[pid]) {
      const groupKey = (s.roleId || 0) + '_' + pid
      map[pid] = {
        parentId: pid,
        parentName: pname,
        students: [],
        collapsed: collapsedParents.value[groupKey] || false
      }
    }
    map[pid].students.push(s)
  }
  return Object.values(map)
}

// 折叠/展开角色组
function toggleGroup(roleId) {
  collapsedGroups.value[roleId] = !collapsedGroups.value[roleId]
  // 同时更新 groupedUsers 中的 collapsed（触发响应式）
  const group = groupedUsers.value.find(g => g.roleId === roleId)
  if (group) group.collapsed = collapsedGroups.value[roleId]
}

// 折叠/展开教师分组下的学生
function toggleParentGroup(roleId, parentId) {
  const key = roleId + '_' + parentId
  collapsedParents.value[key] = !collapsedParents.value[key]
}

function goHome() {
  router.push('/dashboard')
}

function goProfile() {
  router.push('/setting')
}

async function logout() {
  if (ws) ws.close()
  const userId = userStore.userId
  userStore.logout()
  try {
    await request.post('/auth/logout', null, { params: { userId } })
  } catch (e) { /* ignore */ }
  router.push('/login')
}

async function fetchOnlineUsers() {
  try {
    const res = await request.get('/stats/online')
    if (res.data.code === 200) {
      const data = res.data.data
      showOnlineBadge.value = data.isVisible !== false
      onlineUsers.value = data.userList || []
      onlineCount.value = data.visibleOnline || onlineUsers.value.length
    }
  } catch (e) { /* ignore */ }
}

// 加载系统名称
async function loadSystemName() {
  try {
    const res = await request.get('/system/setting/all')
    if (res.data.code === 200 && res.data.data?.basic?.systemName) {
      systemName.value = res.data.data.basic.systemName
    }
  } catch (e) { /* ignore */ }
}

function initWebSocket() {
  const token = localStorage.getItem('token')
  if (!token) return

  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = location.hostname === 'localhost' ? 'localhost:8080' : location.host
  const url = `${protocol}//${host}/ws/activity?token=${token}`

  try {
    ws = new WebSocket(url)
    ws.onopen = () => {
      // 心跳
      wsTimer = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send('ping')
        }
      }, 30000)
      // 初始获取在线用户
      fetchOnlineUsers()
    }
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.onlineCount !== undefined) {
          onlineCount.value = data.onlineCount
        }
        if (data.type === 'activity' || data.type === 'broadcast') {
          // 新活动通知（可扩展）
        }
        // 收到消息时刷新在线用户
        if (data.type === 'online_update') {
          fetchOnlineUsers()
        }
      } catch (e) { /* pong等非 JSON 消息 */ }
    }
    ws.onerror = () => {
      // 静默失败
    }
    ws.onclose = () => {
      if (wsTimer) clearInterval(wsTimer)
    }
  } catch (e) {
    // WebSocket 不可用时不影响页面
  }
}

onMounted(() => {
  initWebSocket()
  fetchOnlineUsers()
  loadSystemName()
  userStore.fetchUserInfo()
  // 页面点击关闭日历
  document.addEventListener('click', handleOutsideCalendar)
  // 预加载今日日历数据（获取任务数）
  loadMonthData()
  // 启动头部轮播
  startHeaderCarousel()
})

onUnmounted(() => {
  if (wsTimer) clearInterval(wsTimer)
  if (ws) ws.close()
  stopHeaderCarousel()
  document.removeEventListener('click', handleOutsideCalendar)
})

function handleOutsideCalendar(e) {
  const wrap = document.querySelector('.calendar-btn-wrap')
  if (wrap && !wrap.contains(e.target)) {
    showCalendar.value = false
    stopCarousel()
  }
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 30px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  flex-shrink: 0;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.online-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 13px;
  color: rgba(255,255,255,0.95);
}

.online-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  box-shadow: 0 0 6px #52c41a;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  font-size: 14px;
}

.logout-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.3);
}

/* 在线徽章可点击 */
.online-badge.clickable {
  cursor: pointer;
  transition: all 0.2s;
}
.online-badge.clickable:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-1px);
}
.view-icon {
  font-size: 11px;
  opacity: 0.7;
  margin-left: 4px;
}

/* 在线用户弹窗 */
.online-modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 70px 20px 20px 20px;
  z-index: 200;
}
.online-modal {
  background: white;
  border-radius: 12px;
  width: 360px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  color: #333;
}
.online-modal .modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.online-modal .modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.online-modal .close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #999;
}
.online-modal .modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.online-user-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.online-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}
.online-user-card:hover {
  background: #f0f2f5;
}
.online-user-card .user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}
.online-user-card .user-info-text {
  flex: 1;
}
.online-user-card .user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.online-user-card .user-role {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.online-modal .modal-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}
.link-btn {
  color: #667eea;
  font-size: 13px;
  text-decoration: none;
}
.link-btn:hover {
  text-decoration: underline;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}

/* 角色分组 */
.role-group {
  margin-bottom: 10px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}
.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f8f9fa;
  cursor: pointer;
  user-select: none;
  font-size: 14px;
  font-weight: 600;
  color: #444;
  transition: background 0.2s;
}
.group-header:hover {
  background: #eef0f5;
}
.group-icon {
  font-size: 11px;
  color: #888;
}
.group-name {
  flex: 1;
  color: #333;
}
.group-count {
  font-size: 12px;
  color: #888;
  font-weight: normal;
}
.group-users {
  padding: 8px 10px;
}

/* 教师分组下的学生折叠 */
.parent-group {
  margin-bottom: 6px;
}
.parent-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #f0f4ff;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  transition: background 0.2s;
}
.parent-header:hover {
  background: #e4eaff;
}
.parent-icon {
  font-size: 10px;
  color: #888;
}
.parent-name {
  flex: 1;
  color: #555;
  font-weight: 500;
}
.parent-count {
  font-size: 12px;
  color: #888;
}
.student-grid {
  padding: 6px 6px 2px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.online-user-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ===== 日历代办插件 ===== */
.calendar-btn-wrap { position: relative; }
.calendar-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 13px;
  color: rgba(255,255,255,0.95);
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.calendar-btn:hover, .calendar-btn.active {
  background: rgba(255,255,255,0.28);
  transform: translateY(-1px);
}
.cal-badge {
  background: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 11px;
  font-weight: 700;
  min-width: 16px;
  text-align: center;
}
.calendar-popup {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 360px;
  max-height: 520px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 300;
  color: #333;
  animation: calPopIn 0.18s ease;
}
@keyframes calPopIn {
  from { opacity: 0; transform: translateY(-6px) scale(0.97); }
  to   { opacity: 1; transform: none; }
}
.cal-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
  background: #f8f9ff;
}
.cal-date-nav { display: flex; align-items: center; gap: 6px; }
.cal-nav-btn {
  background: none;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 16px;
  color: #555;
  transition: all 0.2s;
}
.cal-nav-btn:hover { background: #667eea; color: white; border-color: #667eea; }
.cal-current-date { font-size: 13px; font-weight: 600; color: #333; }
.cal-today-btn {
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
  cursor: pointer;
}
.cal-today-btn:hover { background: #5a6fd6; }
.cal-close { background: none; border: none; font-size: 22px; cursor: pointer; color: #999; }
.cal-close:hover { color: #333; }
.task-carousel {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 10px 14px 6px;
  overflow: hidden;
}
.task-carousel-inner { overflow: hidden; border-radius: 8px; }
.task-slide { display: flex; transition: transform 0.4s ease; }
.task-card {
  min-width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.15);
  border-radius: 8px;
  padding: 8px 12px;
  color: white;
  font-size: 13px;
}
.task-card.done { opacity: 0.5; text-decoration: line-through; }
.task-prio { font-size: 12px; }
.task-title { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.task-time { font-size: 11px; opacity: 0.8; }
.task-done-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
}
.task-done-btn:hover { background: rgba(255,255,255,0.35); }
.carousel-dots { display: flex; justify-content: center; gap: 5px; margin-top: 6px; padding-bottom: 2px; }
.dot { width: 5px; height: 5px; border-radius: 50%; background: rgba(255,255,255,0.4); cursor: pointer; }
.dot.active { background: white; }
.cal-body { flex: 1; overflow-y: auto; padding: 12px 14px; }
.cal-loading, .cal-empty {
  text-align: center;
  color: #999;
  padding: 30px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.cal-empty span:first-child { font-size: 28px; }
.cal-section { margin-bottom: 14px; }
.cal-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}
.lesson-card {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  background: #f8f9ff;
  border-radius: 8px;
  border-left: 3px solid #667eea;
  margin-bottom: 8px;
  font-size: 13px;
}
.lesson-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
  gap: 2px;
}
.time-sep { font-size: 10px; color: #ccc; }
.lesson-info { flex: 1; }
.lesson-name { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 4px; }
.lesson-sub { font-size: 12px; color: #666; display: flex; gap: 10px; }
.lesson-loc { font-size: 11px; color: #888; margin-top: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 7px;
  background: #fafafa;
  margin-bottom: 6px;
  font-size: 13px;
}
.task-item:hover { background: #f0f2ff; }
.task-item.task-done { opacity: 0.5; }
.task-check {
  background: none;
  border: 1.5px solid #ccc;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  color: #667eea;
  flex-shrink: 0;
}
.task-check:hover { border-color: #667eea; }
.task-done .task-check { background: #667eea; border-color: #667eea; color: white; }
.task-detail { flex: 1; overflow: hidden; }
.task-item-title { font-size: 13px; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.task-done .task-item-title { text-decoration: line-through; color: #999; }
.task-item-time { font-size: 11px; color: #999; margin-top: 1px; }
.task-prio-tag { font-size: 11px; padding: 1px 6px; border-radius: 10px; font-weight: 600; flex-shrink: 0; }
.prio-3 { background: #fff1f0; color: #cf1322; }
.prio-2 { background: #fffbe6; color: #d48806; }
.prio-1 { background: #f6ffed; color: #389e0d; }
.cal-footer { padding: 10px 16px; border-top: 1px solid #f0f0f0; text-align: right; }

/* ===== 头部任务轮播 ===== */
.header-task-carousel {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.1);
  border-radius: 20px;
  padding: 4px 12px;
  max-width: 280px;
}

.task-carousel-container {
  width: 200px;
  overflow: hidden;
}

.task-carousel-track {
  display: flex;
  transition: transform 0.5s ease;
}

.header-task-item {
  min-width: 100%;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255,255,255,0.95);
}

.header-task-item .task-icon { font-size: 11px; }

.header-task-item .task-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-task-item .task-time {
  font-size: 11px;
  color: rgba(255,255,255,0.7);
  white-space: nowrap;
}

.task-carousel-dots {
  display: flex;
  gap: 4px;
}

.task-carousel-dots .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255,255,255,0.4);
  cursor: pointer;
}

.task-carousel-dots .dot.active {
  background: white;
}

/* ===== 完整日历样式 ===== */
.full-calendar {
  padding: 12px 16px;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.calendar-weekdays .weekday {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 44px;
}

.calendar-day:hover {
  background: #f5f5f5;
}

.calendar-day .day-number {
  font-size: 13px;
  color: #333;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.calendar-day.other-month .day-number {
  color: #ccc;
}

.calendar-day.today .day-number {
  background: #667eea;
  color: white;
  font-weight: 600;
}

.calendar-day.selected {
  background: #f0f2ff;
}

.calendar-day.selected .day-number {
  background: #667eea;
  color: white;
  font-weight: 600;
}

.day-indicators {
  display: flex;
  gap: 2px;
  margin-top: 2px;
}

.day-indicators .indicator {
  font-size: 9px;
  padding: 0 4px;
  border-radius: 6px;
  min-width: 14px;
  text-align: center;
}

.day-indicators .task-dot {
  background: #ff4d4f;
  color: white;
}

.day-indicators .lesson-dot {
  background: #52c41a;
  color: white;
}

/* ===== 选中日期详情 ===== */
.selected-date-details {
  border-top: 1px solid #f0f0f0;
  max-height: 200px;
  overflow-y: auto;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f8f9ff;
  border-bottom: 1px solid #f0f0f0;
}

.details-date {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.details-count {
  font-size: 12px;
  color: #667eea;
}

.details-body {
  padding: 12px 16px;
}

.details-empty {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 20px 0;
}

.details-section {
  margin-bottom: 12px;
}

.details-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 8px;
}

.details-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}

.details-item .item-time {
  color: #667eea;
  font-weight: 500;
  white-space: nowrap;
}

.details-item .item-name {
  flex: 1;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.details-item .item-sub {
  color: #888;
  font-size: 11px;
  white-space: nowrap;
}

.details-item .item-priority {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 8px;
  font-weight: 600;
}

.details-item .item-priority.prio-3 { background: #fff1f0; color: #cf1322; }
.details-item .item-priority.prio-2 { background: #fffbe6; color: #d48806; }
.details-item .item-priority.prio-1 { background: #f6ffed; color: #389e0d; }

.details-item .item-name.task-done {
  text-decoration: line-through;
  color: #999;
}

.details-item .item-action {
  background: none;
  border: 1.5px solid #ccc;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 11px;
  color: #667eea;
  flex-shrink: 0;
}

.details-item .item-action:hover {
  border-color: #667eea;
}

/* 隐藏旧的日历样式 */
.task-carousel,
.cal-body,
.cal-section,
.lesson-card,
.task-item {
  display: none;
}
</style>
