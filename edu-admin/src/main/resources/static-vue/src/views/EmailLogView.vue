<template>
  <div class="email-log-page">
    <div class="page-header">
      <h1 class="page-title">邮件发送日志</h1>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input v-model="searchForm.toEmail" type="text" class="search-input" placeholder="收件人邮箱">
      <select v-model="searchForm.sendStatus" class="search-select">
        <option value="">全部状态</option>
        <option :value="1">发送成功</option>
        <option :value="0">发送失败</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 列表 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>发件人</th>
            <th>收件人</th>
            <th>邮件主题</th>
            <th>发送状态</th>
            <th>发送时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in list" :key="log.id">
            <td>{{ log.fromEmail || '-' }}</td>
            <td>{{ log.toEmail }}</td>
            <td class="subject-cell">{{ log.subject }}</td>
            <td>
              <span :class="['status-tag', log.sendStatus === 1 ? 'status-success' : 'status-fail']">
                {{ log.sendStatus === 1 ? '✓ 成功' : '✗ 失败' }}
              </span>
            </td>
            <td>{{ formatDateTime(log.sendTime) }}</td>
            <td class="actions">
              <button class="action-btn action-view" @click="viewDetail(log)">查看</button>
              <button v-if="log.sendStatus === 0" class="action-btn action-retry" @click="viewError(log)">错误</button>
              <button class="action-btn action-delete" @click="handleDelete(log)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="list.length === 0" class="empty-tip">暂无邮件日志数据</div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <span class="total">共 {{ total }} 条</span>
      <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">上一页</button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">下一页</button>
      <select v-model="pageSize" class="page-size" @change="handleSizeChange">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>邮件详情</h3>
          <button class="close-btn" @click="closeDetailModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <label>发件人：</label>
              <span>{{ currentLog.fromEmail || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>收件人：</label>
              <span>{{ currentLog.toEmail }}</span>
            </div>
            <div class="detail-item full-width">
              <label>邮件主题：</label>
              <span>{{ currentLog.subject }}</span>
            </div>
            <div class="detail-item">
              <label>发送状态：</label>
              <span :class="currentLog.sendStatus === 1 ? 'text-success' : 'text-fail'">
                {{ currentLog.sendStatus === 1 ? '发送成功' : '发送失败' }}
              </span>
            </div>
            <div class="detail-item">
              <label>发送时间：</label>
              <span>{{ formatDateTime(currentLog.sendTime) }}</span>
            </div>
          </div>

          <div v-if="currentLog.sendStatus === 0 && currentLog.errorMsg" class="error-section">
            <label>错误信息：</label>
            <pre class="error-message">{{ currentLog.errorMsg }}</pre>
          </div>

          <div class="content-section">
            <label>邮件内容：</label>
            <div class="email-content" v-html="currentLog.content"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm: confirmDialog } = useMessage()

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

const searchForm = reactive({
  toEmail: '',
  sendStatus: ''
})

const showDetailModal = ref(false)
const currentLog = ref({})

onMounted(() => loadList())

async function loadList() {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchForm.toEmail) params.toEmail = searchForm.toEmail
    if (searchForm.sendStatus !== '') params.sendStatus = searchForm.sendStatus

    const res = await request.get('/system/email-log/page', { params })
    if (res.data.code === 200) {
      list.value = res.data.data.rows || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('加载邮件日志失败', e)
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function resetSearch() {
  searchForm.toEmail = ''
  searchForm.sendStatus = ''
  handleSearch()
}

function handlePageChange(p) {
  pageNum.value = p
  loadList()
}

function handleSizeChange() {
  pageNum.value = 1
  loadList()
}

function viewDetail(log) {
  currentLog.value = log
  showDetailModal.value = true
}

function viewError(log) {
  if (log.errorMsg) {
    alert('错误信息：\n' + log.errorMsg)
  } else {
    toast.info('无错误信息')
  }
}

async function handleDelete(log) {
  const confirmed = await confirmDialog({
    title: '确认删除',
    message: `确定删除该邮件日志吗？`,
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    await request.delete(`/system/email-log/${log.id}`)
    toast.success('删除成功')
    loadList()
  } catch (e) {
    toast.error('删除失败')
  }
}

function closeDetailModal() {
  showDetailModal.value = false
}

function formatDateTime(d) {
  if (!d) return '-'
  return String(d).replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.email-log-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8f9fa;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e0e0e0;
}

.data-table td {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.data-table tr:hover {
  background: #fafafa;
}

.subject-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-success {
  background: #e6ffed;
  color: #43e97b;
}

.status-fail {
  background: #fff0f6;
  color: #f5576c;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-view {
  background: #e6f7ff;
  color: #1890ff;
}

.action-retry {
  background: #fff7e6;
  color: #fa8c16;
}

.action-delete {
  background: #fff0f6;
  color: #f5576c;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.total {
  color: #666;
  font-size: 14px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.page-size {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  padding: 60px 20px;
  color: #999;
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
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-large {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 25px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 25px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-weight: 600;
  color: #555;
  min-width: 80px;
  margin-right: 10px;
}

.detail-item span {
  color: #666;
}

.text-success {
  color: #43e97b;
  font-weight: 600;
}

.text-fail {
  color: #f5576c;
  font-weight: 600;
}

.error-section {
  margin-bottom: 25px;
  padding: 15px;
  background: #fff0f6;
  border-radius: 8px;
  border-left: 4px solid #f5576c;
}

.error-section label {
  display: block;
  font-weight: 600;
  color: #f5576c;
  margin-bottom: 10px;
}

.error-message {
  margin: 0;
  padding: 15px;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  white-space: pre-wrap;
  word-break: break-all;
}

.content-section {
  margin-top: 20px;
}

.content-section label {
  display: block;
  font-weight: 600;
  color: #555;
  margin-bottom: 10px;
}

.email-content {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  line-height: 1.8;
  color: #333;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}
</style>
