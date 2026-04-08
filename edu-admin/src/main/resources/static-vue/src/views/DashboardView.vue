<template>
  <div class="dashboard">
    <!-- 顶部欢迎栏：时钟 + 欢迎语 + 弹幕通知 -->
    <div class="welcome-bar">
      <div class="clock-area">
        <div class="clock-time">{{ clockTime }}</div>
        <div class="clock-date">{{ clockDate }}</div>
      </div>
      <div class="welcome-text">
        <span class="welcome-hi">{{ greeting }}，</span>
        <span class="welcome-name">{{ userName }}！</span>
        <span class="welcome-sub">今天也要加油哦 🎉</span>
      </div>
      <div class="marquee-bar" v-if="marqueeList.length > 0">
        <span class="marquee-icon">📢</span>
        <div class="marquee-wrap">
          <span class="marquee-text" :style="{ animationDuration: marqueeList.length * 6 + 's' }">
            <span v-for="(item, i) in marqueeList" :key="i" class="marquee-item">{{ item }}&nbsp;&nbsp;&nbsp;&nbsp;</span>
          </span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">总课程数</div>
        <div class="stat-value">{{ stats.courseCount }}</div>
        <div class="stat-change">↑ {{ stats.courseChange }}% 较上月</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">在校学员</div>
        <div class="stat-value">{{ stats.studentCount.toLocaleString() }}</div>
        <div class="stat-change">↑ {{ stats.studentChange }}% 较上月</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日考试</div>
        <div class="stat-value">{{ stats.examCount }}</div>
        <div class="stat-change">↑ {{ stats.examChange }} 场新增</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">通过率</div>
        <div class="stat-value">{{ stats.passRate }}%</div>
        <div class="stat-change">↑ {{ stats.passChange }}% 较上月</div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-access">
      <router-link to="/course" class="quick-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
        <div class="quick-icon">📚</div>
        <div class="quick-title">课程管理</div>
        <div class="quick-desc">管理课程、章节、内容</div>
        <div class="quick-stat"><span class="quick-stat-num">{{ stats.courseCount }}</span> 门课程</div>
      </router-link>
      <router-link to="/exam" class="quick-card" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
        <div class="quick-icon">📝</div>
        <div class="quick-title">考试管理</div>
        <div class="quick-desc">创建考试、组卷、评分</div>
        <div class="quick-stat"><span class="quick-stat-num">{{ stats.examCount }}</span> 场今日考试</div>
      </router-link>
      <router-link to="/user" class="quick-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
        <div class="quick-icon">👥</div>
        <div class="quick-title">用户管理</div>
        <div class="quick-desc">学员、教师、管理员</div>
        <div class="quick-stat"><span class="quick-stat-num">{{ stats.studentCount }}</span> 名学员</div>
      </router-link>
    </div>

    <!-- 最近活动 -->
    <div class="bottom-section">
      <div class="section activity-section">
        <div class="section-header">
          <h2 class="section-title">📢 最近活动</h2>
          <router-link to="/activity" class="view-all">查看全部</router-link>
        </div>
        <div class="activity-list">
          <div v-for="(item, index) in activities" :key="index" class="activity-item">
            <div class="activity-icon">{{ item.icon }}</div>
            <div class="activity-content">
              <div class="activity-title">{{ item.title }}</div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
          </div>
          <div v-if="activities.length === 0" class="empty-tip">暂无活动记录</div>
        </div>
      </div>

      <!-- 轮播通知区域 -->
      <div class="section notice-section">
        <div class="section-header">
          <h2 class="section-title">📣 公告通知</h2>
        </div>
        <div class="notice-carousel">
          <div
            v-for="(notice, idx) in noticeList"
            :key="idx"
            class="notice-item"
            :class="{ active: idx === currentNoticeIdx }"
          >
            <div class="notice-tag">{{ notice.tag }}</div>
            <div class="notice-title">{{ notice.title }}</div>
            <div class="notice-time">{{ notice.time }}</div>
          </div>
          <div v-if="noticeList.length === 0" class="empty-tip">暂无公告</div>
        </div>
        <div class="notice-dots" v-if="noticeList.length > 1">
          <span
            v-for="(_, idx) in noticeList"
            :key="idx"
            :class="['dot', { active: idx === currentNoticeIdx }]"
            @click="currentNoticeIdx = idx"
          ></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import request from '@/utils/request'

// 时钟
const clockTime = ref('')
const clockDate = ref('')
let clockTimer = null

function updateClock() {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  clockTime.value = `${h}:${m}:${s}`
  const weeks = ['日', '一', '二', '三', '四', '五', '六']
  const y = now.getFullYear(), mo = now.getMonth() + 1, d = now.getDate()
  clockDate.value = `${y}年${mo}月${d}日 星期${weeks[now.getDay()]}`
}

// 问候语
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '小心夸夸'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const userName = ref('管理员')

// 弹幕/满动列表
const marqueeList = ref([])

// 公告轮播
const noticeList = ref([])
const currentNoticeIdx = ref(0)
let noticeTimer = null

// 统计数据
const stats = ref({
  courseCount: 0,
  studentCount: 0,
  examCount: 0,
  passRate: 0,
  courseChange: 0,
  studentChange: 0,
  examChange: 0,
  passChange: 0
})

const activities = ref([])

onMounted(async () => {
  // 启动时钟
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  // 获取用户名
  try {
    const uRes = await request.get('/system/user/info')
    if (uRes.data.code === 200) {
      const u = uRes.data.data
      userName.value = u?.nickname || u?.username || '管理员'
    }
  } catch (e) { /* ignore */ }

  // 获取统计数据
  try {
    const res = await request.get('/stats/dashboard')
    if (res.data.code === 200) {
      const d = res.data.data
      const courses = d.courses || {}
      const students = d.students || {}
      const exams = d.exams || {}
      const passRate = d.passRate || {}
      stats.value = {
        courseCount: courses.current ?? d.courseCount ?? 0,
        studentCount: students.current ?? d.studentCount ?? 0,
        examCount: exams.current ?? d.examCount ?? 0,
        passRate: passRate.current ?? d.passRate ?? 0,
        courseChange: courses.lastMonth > 0
          ? Math.round((courses.current - courses.lastMonth) / courses.lastMonth * 100)
          : (d.courseChange ?? 0),
        studentChange: students.lastMonth > 0
          ? Math.round((students.current - students.lastMonth) / students.lastMonth * 100)
          : (d.studentChange ?? 0),
        examChange: (exams.current ?? 0) - (exams.yesterday ?? 0),
        passChange: ((passRate.current ?? 0) - (passRate.lastMonth ?? 0)).toFixed(1)
      }
    }
  } catch (e) {
    console.warn('获取统计数据失败', e)
    stats.value = { courseCount: 128, studentCount: 2560, examCount: 12, passRate: 86, courseChange: 12, studentChange: 8, examChange: 3, passChange: 5 }
  }

  // 获取最近活动
  try {
    const res = await request.get('/system/activity/list', {
      params: { pageSize: 5, status: 1 }
    })
    if (res.data.code === 200) {
      const list = res.data.data?.rows || res.data.data || []
      activities.value = list.slice(0, 5).map(item => ({
        icon: item.activityType === 1 ? '📢' : item.activityType === 2 ? '🎉' : '📝',
        title: item.title,
        time: formatTimeAgo(item.createTime)
      }))
      // 将活动标题放入弹幕
      marqueeList.value = activities.value.map(a => a.title)
    }
  } catch (e) {
    console.warn('获取活动列表失败', e)
    activities.value = [
      { icon: '📚', title: '新课程《Java高级编程》已发布', time: '2026-03-13 10:00' },
      { icon: '📝', title: '期末考试已结束', time: '2026-03-13 09:00' },
      { icon: '👤', title: '新学员张三注册', time: '2026-03-12 16:30' },
    ]
    marqueeList.value = activities.value.map(a => a.title)
  }

  // 公告通知（取活动数据用作公告）
  try {
    const res = await request.get('/system/activity/list', {
      params: { pageSize: 6, status: 1, activityType: 1 }
    })
    if (res.data.code === 200) {
      const list = res.data.data?.rows || res.data.data || []
      noticeList.value = list.slice(0, 6).map(item => ({
        tag: '📢 公告',
        title: item.title,
        time: formatTimeAgo(item.createTime)
      }))
    }
    if (noticeList.value.length === 0) {
      noticeList.value = activities.value.slice(0, 4).map(a => ({ tag: '📢 活动', title: a.title, time: a.time }))
    }
  } catch (e) {
    noticeList.value = activities.value.slice(0, 4).map(a => ({ tag: '📢 活动', title: a.title, time: a.time }))
  }

  // 公告轮播定时
  if (noticeList.value.length > 1) {
    noticeTimer = setInterval(() => {
      currentNoticeIdx.value = (currentNoticeIdx.value + 1) % noticeList.value.length
    }, 4000)
  }
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (noticeTimer) clearInterval(noticeTimer)
})

function formatTimeAgo(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

/* 欢迎栏 */
.welcome-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 22px 30px;
  margin-bottom: 28px;
  color: white;
  box-shadow: 0 4px 20px rgba(102,126,234,0.35);
}

.clock-area {
  flex-shrink: 0;
  text-align: center;
  border-right: 1px solid rgba(255,255,255,0.3);
  padding-right: 24px;
}

.clock-time {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 2px;
  font-family: 'Courier New', monospace;
  line-height: 1.1;
}

.clock-date {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.welcome-text {
  flex-shrink: 0;
}

.welcome-hi {
  font-size: 20px;
  font-weight: 600;
}

.welcome-name {
  font-size: 20px;
  font-weight: 700;
}

.welcome-sub {
  display: block;
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

/* 弹幕滚动 */
.marquee-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255,255,255,0.15);
  border-radius: 8px;
  padding: 8px 14px;
  overflow: hidden;
}

.marquee-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.marquee-wrap {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
}

.marquee-text {
  display: inline-block;
  animation: marquee-scroll linear infinite;
  font-size: 14px;
  opacity: 0.95;
}

@keyframes marquee-scroll {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #667eea;
  margin: 10px 0;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

.stat-change {
  font-size: 12px;
  color: #52c41a;
  margin-top: 5px;
}

/* 快捷入口 */
.quick-access {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.quick-card {
  border-radius: 16px;
  padding: 30px;
  color: white;
  text-decoration: none;
  transition: transform 0.3s, box-shadow 0.3s;
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.quick-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.quick-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.quick-desc {
  font-size: 14px;
  opacity: 0.9;
}

/* 底部双栏布局 */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.section-title {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.view-all {
  color: #667eea;
  font-size: 14px;
  text-decoration: none;
}

.view-all:hover {
  text-decoration: underline;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.activity-icon {
  font-size: 24px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #333;
}

.activity-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 公告轮播 */
.notice-carousel {
  min-height: 120px;
  position: relative;
}

.notice-item {
  display: none;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 10px;
  border-left: 4px solid #667eea;
}

.notice-item.active {
  display: block;
  animation: fade-in 0.4s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.notice-tag {
  font-size: 12px;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 8px;
}

.notice-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  line-height: 1.5;
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.notice-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 14px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s;
}

.dot.active {
  background: #667eea;
  width: 20px;
  border-radius: 4px;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px;
}

.quick-stat {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255,255,255,0.2);
  font-size: 13px;
  color: rgba(255,255,255,0.9);
}

.quick-stat-num {
  font-size: 20px;
  font-weight: 800;
  margin-right: 4px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-access {
    grid-template-columns: repeat(2, 1fr);
  }
  .bottom-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-bar {
    flex-wrap: wrap;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .quick-access {
    grid-template-columns: 1fr;
  }
}
</style>
