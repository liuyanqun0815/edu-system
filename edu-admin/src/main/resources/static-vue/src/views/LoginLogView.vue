<template>
  <div class="login-log-page">
    <div class="page-header">
      <h1>登录日志</h1>
      <p class="subtitle">查看用户登录记录，按权限递减规则展示</p>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item">
          <label>用户名</label>
          <input type="text" v-model="filterUsername" placeholder="输入用户名搜索" @keyup.enter="search" />
        </div>
        <div class="filter-actions">
          <button class="btn-primary" @click="search">搜索</button>
          <button class="btn-default" @click="reset">重置</button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>登录时间</th>
            <th>登出时间</th>
            <th>在线时长</th>
            <th>登录IP</th>
            <th>设备信息</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="loading-cell">
              <span class="loading-spinner"></span>
              加载中...
            </td>
          </tr>
          <tr v-else-if="logs.length === 0">
            <td colspan="8" class="empty-cell">暂无登录日志</td>
          </tr>
          <tr v-else v-for="log in logs" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.username || '-' }}</td>
            <td>{{ log.nickname || '-' }}</td>
            <td>{{ formatTime(log.loginTime) }}</td>
            <td>{{ formatTime(log.logoutTime) }}</td>
            <td>{{ formatDuration(log.duration) }}</td>
            <td>{{ log.loginIp || '-' }}</td>
            <td class="ua-cell" :title="log.userAgent">{{ formatUserAgent(log.userAgent) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="total > 0">
      <div class="pagination-info">
        共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <button class="btn-page" :disabled="currentPage <= 1" @click="goPage(1)">首页</button>
        <button class="btn-page" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <span class="page-num">{{ currentPage }}</span>
        <button class="btn-page" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
        <button class="btn-page" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">末页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterUsername = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

async function fetchLogs() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterUsername.value) {
      params.username = filterUsername.value
    }
    
    const res = await request.get('/auth/login-logs', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      logs.value = data.records || []
      total.value = data.total || 0
    }
  } catch (e) {
    console.error('获取登录日志失败', e)
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
  fetchLogs()
}

function reset() {
  filterUsername.value = ''
  currentPage.value = 1
  fetchLogs()
}

function goPage(page) {
  currentPage.value = page
  fetchLogs()
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatDuration(minutes) {
  if (!minutes) return '-'
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
}

function formatUserAgent(ua) {
  if (!ua) return '-'
  // 简化显示
  if (ua.includes('Chrome')) return 'Chrome浏览器'
  if (ua.includes('Firefox')) return 'Firefox浏览器'
  if (ua.includes('Safari')) return 'Safari浏览器'
  if (ua.includes('Edge')) return 'Edge浏览器'
  if (ua.includes('Mobile')) return '移动端'
  return '其他设备'
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.login-log-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.filter-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.filter-item input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  width: 200px;
  transition: border-color 0.2s;
}

.filter-item input:focus {
  outline: none;
  border-color: #667eea;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.btn-primary {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-default {
  padding: 8px 20px;
  background: white;
  color: #666;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-default:hover {
  border-color: #667eea;
  color: #667eea;
}

.table-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8f9fa;
  padding: 14px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #eee;
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  color: #555;
  border-bottom: 1px solid #f0f0f0;
}

.data-table tbody tr:hover {
  background: #fafbfc;
}

.loading-cell, .empty-cell {
  text-align: center;
  padding: 40px !important;
  color: #999;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #e0e0e0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ua-cell {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-page {
  padding: 6px 12px;
  background: white;
  color: #666;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-num {
  padding: 6px 12px;
  background: #667eea;
  color: white;
  border-radius: 4px;
  font-size: 13px;
  min-width: 36px;
  text-align: center;
}
</style>
