<template>
  <div class="stats-page">
    <div class="page-header">
      <h1 class="page-title">数据统计</h1>
      <div class="header-actions">
        <div class="time-filter">
          <button 
            v-for="t in timeOptions" 
            :key="t.value" 
            class="time-btn"
            :class="{ active: timeRange === t.value }"
            @click="changeTimeRange(t.value)"
          >{{ t.label }}</button>
        </div>
        <button class="btn btn-default" @click="refreshAll">🔄 刷新</button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card card-blue">
        <div class="card-icon">📚</div>
        <div class="card-content">
          <div class="stat-value">{{ animatedStats.courseCount }}</div>
          <div class="stat-label">总课程数</div>
          <div class="stat-change positive">{{ computed_stats.courseChange }}</div>
        </div>
        <div class="card-trend">
          <svg width="60" height="30" viewBox="0 0 60 30">
            <polyline points="0,25 15,20 30,15 45,18 60,5" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
          </svg>
        </div>
      </div>
      <div class="stat-card card-green">
        <div class="card-icon">👥</div>
        <div class="card-content">
          <div class="stat-value">{{ animatedStats.studentCount }}</div>
          <div class="stat-label">在校学员</div>
          <div class="stat-change positive">{{ computed_stats.studentChange }}</div>
        </div>
        <div class="card-trend">
          <svg width="60" height="30" viewBox="0 0 60 30">
            <polyline points="0,20 15,22 30,12 45,15 60,8" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
          </svg>
        </div>
      </div>
      <div class="stat-card card-orange">
        <div class="card-icon">📝</div>
        <div class="card-content">
          <div class="stat-value">{{ animatedStats.examCount }}</div>
          <div class="stat-label">今日考试</div>
          <div class="stat-change positive">{{ computed_stats.examChange }}</div>
        </div>
        <div class="card-trend">
          <svg width="60" height="30" viewBox="0 0 60 30">
            <polyline points="0,18 15,15 30,20 45,10 60,12" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
          </svg>
        </div>
      </div>
      <div class="stat-card card-purple">
        <div class="card-icon">🏆</div>
        <div class="card-content">
          <div class="stat-value">{{ animatedStats.passRate }}%</div>
          <div class="stat-label">通过率</div>
          <div class="stat-change positive">{{ computed_stats.passChange }}</div>
        </div>
        <div class="card-trend">
          <svg width="60" height="30" viewBox="0 0 60 30">
            <polyline points="0,22 15,18 30,20 45,12 60,5" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 两栏布局：考试分析 + 数据总览 -->
    <div class="two-col">
      <!-- 考试分析 -->
      <div class="chart-section">
        <h2 class="section-title">📊 考试分析</h2>
        <div class="bar-chart">
          <div class="bar-item">
            <div class="bar-label">及格率</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: computed_stats.passRate + '%', background: 'linear-gradient(90deg,#667eea,#764ba2)' }"></div>
            </div>
            <div class="bar-value">{{ computed_stats.passRate }}%</div>
          </div>
          <div class="bar-item">
            <div class="bar-label">优秀率</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: computed_stats.excellentRate + '%', background: 'linear-gradient(90deg,#43e97b,#38f9d7)' }"></div>
            </div>
            <div class="bar-value">{{ computed_stats.excellentRate }}%</div>
          </div>
          <div class="bar-item">
            <div class="bar-label">平均分</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: (rawStats.avgScore || 0) + '%', background: 'linear-gradient(90deg,#fa709a,#fee140)' }"></div>
            </div>
            <div class="bar-value">{{ rawStats.avgScore || 0 }}分</div>
          </div>
        </div>
        <div class="exam-detail-grid">
          <div class="exam-detail-item">
            <div class="detail-num">{{ rawStats.examCount || 0 }}</div>
            <div class="detail-label">总考试次数</div>
          </div>
          <div class="exam-detail-item">
            <div class="detail-num">{{ rawStats.avgScore || 0 }}</div>
            <div class="detail-label">平均分数</div>
          </div>
          <div class="exam-detail-item">
            <div class="detail-num">{{ computed_stats.passRate }}%</div>
            <div class="detail-label">及格率</div>
          </div>
          <div class="exam-detail-item">
            <div class="detail-num">{{ computed_stats.excellentRate }}%</div>
            <div class="detail-label">优秀率</div>
          </div>
        </div>
      </div>

      <!-- 数据总览 -->
      <div class="chart-section">
        <h2 class="section-title">📋 数据总览</h2>
        <div class="overview-list">
          <div class="overview-item">
            <div class="overview-left">
              <div class="ov-icon" style="background:#f0f4ff;">👥</div>
              <span class="ov-label">总用户数</span>
            </div>
            <div class="ov-right">
              <span class="ov-num">{{ rawStats.userCount || 0 }}</span>
              <div class="mini-bar-track">
                <div class="mini-bar" style="background:#667eea;" :style="{ width: Math.min(rawStats.userCount / 100, 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-left">
              <div class="ov-icon" style="background:#fff9e6;">📚</div>
              <span class="ov-label">课程总数</span>
            </div>
            <div class="ov-right">
              <span class="ov-num">{{ rawStats.courseCount || 0 }}</span>
              <div class="mini-bar-track">
                <div class="mini-bar" style="background:#f6d365;" :style="{ width: Math.min((rawStats.courseCount || 0) / 50 * 100, 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-left">
              <div class="ov-icon" style="background:#f6ffed;">📝</div>
              <span class="ov-label">试卷总数</span>
            </div>
            <div class="ov-right">
              <span class="ov-num">{{ rawStats.paperCount || 0 }}</span>
              <div class="mini-bar-track">
                <div class="mini-bar" style="background:#43e97b;" :style="{ width: Math.min((rawStats.paperCount || 0) / 50 * 100, 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-left">
              <div class="ov-icon" style="background:#fff0f6;">❓</div>
              <span class="ov-label">题目总数</span>
            </div>
            <div class="ov-right">
              <span class="ov-num">{{ rawStats.questionCount || 0 }}</span>
              <div class="mini-bar-track">
                <div class="mini-bar" style="background:#f5576c;" :style="{ width: Math.min((rawStats.questionCount || 0) / 200 * 100, 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 在线用户 -->
    <div class="chart-section">
      <div class="section-header">
        <h2 class="section-title">🟢 在线用户</h2>
        <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
      </div>
      <div class="online-stats">
        <div class="online-count">
          <span class="number">{{ onlineUsers.length }}</span>
          <span class="label">当前在线</span>
          <div class="online-pulse"></div>
        </div>
        <div class="online-list" v-if="onlineUsers.length > 0">
          <div v-for="user in onlineUsers" :key="user.id" class="online-user">
            <div class="online-user-avatar" :style="{ background: getColor(user.id) }">
              {{ (user.nickname || user.username || '?').charAt(0).toUpperCase() }}
            </div>
            <div class="online-user-info">
              <span class="user-name">{{ user.nickname || user.username }}</span>
              <span class="user-role">{{ user.roleName || '用户' }}</span>
            </div>
            <span class="user-dot"></span>
          </div>
        </div>
        <div v-else class="empty-tip">暂无在线用户</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'

const rawStats = ref({})
const onlineUsers = ref([])
const timeRange = ref('week')
const lastUpdateTime = ref('-')
const animatedStats = ref({ courseCount: 0, studentCount: 0, examCount: 0, passRate: 0 })
let refreshTimer = null

const timeOptions = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]

const colors = ['#667eea','#f5576c','#43e97b','#fa709a','#4facfe','#f6d365']
function getColor(id) { return colors[(id || 0) % colors.length] }

const computed_stats = computed(() => {
  const d = rawStats.value
  const courses = d.courses || {}
  const students = d.students || {}
  const exams = d.exams || {}
  const passRate = d.passRate || {}
  const excellentRate = d.excellentRate || {}

  const courseCount = courses.current ?? d.courseCount ?? 0
  const studentCount = students.current ?? d.studentCount ?? 0
  const examCount = exams.current ?? d.examCount ?? 0
  const pr = passRate.current ?? d.passRate ?? 0
  const er = excellentRate.current ?? d.excellentRate ?? 0

  const courseChange = courses.lastMonth > 0
    ? `↑ ${Math.round((courseCount - courses.lastMonth) / courses.lastMonth * 100)}% 较上月`
    : '–'
  const studentChange = students.lastMonth > 0
    ? `↑ ${Math.round((studentCount - students.lastMonth) / students.lastMonth * 100)}% 较上月`
    : '–'
  const examChange = `↑ ${(exams.current ?? 0) - (exams.yesterday ?? 0)} 场新增`
  const passChange = passRate.lastMonth != null
    ? `↑ ${(pr - passRate.lastMonth).toFixed(1)}% 较上月`
    : '–'

  return { courseCount, studentCount, examCount, passRate: pr, excellentRate: er, courseChange, studentChange, examChange, passChange }
})

async function fetchStats() {
  try {
    const res = await request.get('/stats/dashboard', { params: { range: timeRange.value } })
    if (res.data.code === 200) {
      rawStats.value = res.data.data || {}
      animateNumbers()
    }
  } catch (e) { console.error('获取统计数据失败', e) }
}

async function fetchOnlineUsers() {
  try {
    const res = await request.get('/stats/online')
    if (res.data.code === 200) {
      onlineUsers.value = res.data.data?.userList || []
      lastUpdateTime.value = new Date().toLocaleTimeString()
    }
  } catch (e) { console.error('获取在线用户失败', e) }
}

function changeTimeRange(range) {
  timeRange.value = range
  fetchStats()
}

function refreshAll() {
  fetchStats()
  fetchOnlineUsers()
}

// 数字动画
function animateNumbers() {
  const target = computed_stats.value
  const duration = 800
  const steps = 30
  const interval = duration / steps
  let step = 0
  
  const timer = setInterval(() => {
    step++
    const progress = step / steps
    const ease = 1 - Math.pow(1 - progress, 3)
    
    animatedStats.value = {
      courseCount: Math.round(target.courseCount * ease),
      studentCount: Math.round(target.studentCount * ease),
      examCount: Math.round(target.examCount * ease),
      passRate: Math.round(target.passRate * ease)
    }
    
    if (step >= steps) clearInterval(timer)
  }, interval)
}

onMounted(() => {
  fetchStats()
  fetchOnlineUsers()
  // 每30秒刷新在线用户
  refreshTimer = setInterval(fetchOnlineUsers, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.stats-page { padding: 20px; }
.page-header { margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }

/* 顶部统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  color: white;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.card-blue  { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.card-green { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.card-orange { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
.card-purple { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }

.card-icon { font-size: 40px; }
.card-content { flex: 1; }
.stat-value { font-size: 32px; font-weight: bold; line-height: 1.1; }
.stat-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }
.stat-change { font-size: 12px; opacity: 0.8; margin-top: 4px; }
.stat-change.positive { color: #b7eb8f; }
.stat-change.negative { color: #ffa39e; }

/* 卡片趋势图 */
.card-trend { position: absolute; right: 15px; bottom: 10px; opacity: 0.6; }

/* 头部操作区 */
.header-actions { display: flex; align-items: center; gap: 20px; }
.time-filter { display: flex; background: #f0f0f0; border-radius: 8px; padding: 4px; }
.time-btn { padding: 6px 14px; border: none; background: transparent; border-radius: 6px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.2s; }
.time-btn.active { background: white; color: #667eea; font-weight: 500; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.btn { padding: 8px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn-default { background: #f0f0f0; color: #666; }

/* 区块头部 */
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f0f0f0; }
.section-header .section-title { margin: 0; padding: 0; border: none; }
.last-update { font-size: 12px; color: #999; }

/* 两列布局 */
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.section-title { font-size: 18px; color: #333; margin: 0 0 20px 0; padding-bottom: 15px; border-bottom: 1px solid #f0f0f0; }

/* 进度条图表 */
.bar-chart { display: flex; flex-direction: column; gap: 18px; margin-bottom: 20px; }
.bar-item { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 60px; font-size: 13px; color: #666; flex-shrink: 0; }
.bar-track { flex: 1; height: 10px; background: #f0f0f0; border-radius: 5px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 5px; transition: width 0.8s ease; }
.bar-value { width: 50px; text-align: right; font-size: 13px; color: #333; font-weight: 600; }

.exam-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.exam-detail-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 14px;
  text-align: center;
}

.detail-num { font-size: 22px; font-weight: bold; color: #667eea; }
.detail-label { font-size: 12px; color: #999; margin-top: 4px; }

/* 总览列表 */
.overview-list { display: flex; flex-direction: column; gap: 16px; }
.overview-item { display: flex; align-items: center; justify-content: space-between; }
.overview-left { display: flex; align-items: center; gap: 10px; }
.ov-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.ov-label { font-size: 14px; color: #555; }
.ov-right { display: flex; align-items: center; gap: 12px; }
.ov-num { font-size: 18px; font-weight: bold; color: #333; min-width: 50px; text-align: right; }
.mini-bar-track { width: 100px; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; }
.mini-bar { height: 100%; border-radius: 3px; transition: width 0.8s ease; }

/* 在线用户 */
.online-stats { display: flex; gap: 30px; align-items: flex-start; }

.online-count {
  position: relative;
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  min-width: 130px;
  flex-shrink: 0;
}

.online-count .number { display: block; font-size: 48px; font-weight: bold; line-height: 1; }
.online-count .label { display: block; font-size: 13px; opacity: 0.9; margin-top: 6px; }

.online-pulse {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 10px;
  height: 10px;
  background: #52c41a;
  border-radius: 50%;
  box-shadow: 0 0 8px #52c41a;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.online-list {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}

.online-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #f8f9fa;
  border-radius: 10px;
}

.online-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.online-user-info { flex: 1; }
.user-name { display: block; font-size: 13px; color: #333; }
.user-role { display: block; font-size: 11px; color: #999; margin-top: 2px; }
.user-dot { width: 8px; height: 8px; background: #52c41a; border-radius: 50%; flex-shrink: 0; }

.empty-tip { color: #999; padding: 20px; text-align: center; }

@media (max-width: 1200px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .stats-cards { grid-template-columns: 1fr; }
  .online-stats { flex-direction: column; }
}
</style>
