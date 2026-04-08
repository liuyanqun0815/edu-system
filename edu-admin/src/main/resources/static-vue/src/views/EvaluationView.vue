<template>
  <div class="evaluation-page">
    <div class="page-header">
      <h1 class="page-title">评价管理</h1>
    </div>

    <div class="search-bar">
      <input v-model="searchForm.content" type="text" class="search-input" placeholder="评价内容" @keyup.enter="handleSearch">
      <select v-model="searchForm.rating" class="search-select">
        <option value="">全部评分</option>
        <option :value="5">5星</option>
        <option :value="4">4星</option>
        <option :value="3">3星</option>
        <option :value="2">2星</option>
        <option :value="1">1星</option>
      </select>
      <select v-model="searchForm.status" class="search-select">
        <option value="">全部状态</option>
        <option :value="0">待回复</option>
        <option :value="1">已回复</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>评分</th>
            <th>内容</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="evaluationList.length === 0">
            <td colspan="6" class="empty-cell">
              <div class="empty-state">
                <div class="empty-icon">💬</div>
                <div class="empty-text">暂无评价数据</div>
              </div>
            </td>
          </tr>
          <tr v-for="item in evaluationList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.userName || '-' }}</td>
            <td>
              <span class="rating">{{ '★'.repeat(item.rating || 0) }}{{ '☆'.repeat(5 - (item.rating || 0)) }}</span>
            </td>
            <td class="content-cell">{{ item.content }}</td>
            <td>
              <span :class="['status-tag', item.status === 0 ? 'status-pending' : 'status-replied']">
                {{ item.status === 0 ? '待回复' : '已回复' }}
              </span>
            </td>
            <td class="actions">
              <button v-if="item.status === 0" class="action-btn action-warn" @click="handleReply(item)">回复</button>
              <button class="action-btn action-delete" @click="handleDelete(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-left">
          <span class="total">共 <em>{{ total }}</em> 条</span>
        </div>
        <div class="pagination-center">
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(1)" title="首页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/></svg>
          </button>
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
            <span>上一页</span>
          </button>
          <span class="page-info"><strong>{{ pageNum }}</strong> / {{ totalPages }}</span>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">
            <span>下一页</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
          </button>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(totalPages)" title="末页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 17l5-5-5-5M6 17l5-5-5-5"/></svg>
          </button>
        </div>
        <div class="pagination-right">
          <select v-model="pageSize" class="page-size" @change="handleSizeChange">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="showReplyModal" class="modal-overlay" @click.self="closeReplyModal">
      <div class="modal">
        <div class="modal-header">
          <h3>回复评价</h3>
          <button class="close-btn" @click="closeReplyModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="evaluation-info">
            <p><strong>用户：</strong>{{ currentEvaluation?.userName }}</p>
            <p><strong>评分：</strong>{{ '★'.repeat(currentEvaluation?.rating || 0) }}</p>
            <p><strong>内容：</strong>{{ currentEvaluation?.content }}</p>
          </div>
          <form @submit.prevent="handleReplySubmit">
            <div class="form-group">
              <label>回复内容 <span class="required">*</span></label>
              <textarea v-model="replyForm.reply" rows="4" placeholder="请输入回复内容" required></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeReplyModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '提交中...' : '提交回复' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm } = useMessage()

const searchForm = reactive({ content: '', rating: '', status: '' })
const evaluationList = ref([])
const showReplyModal = ref(false)
const submitting = ref(false)
const currentEvaluation = ref(null)
const replyForm = reactive({ reply: '' })
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

async function fetchEvaluationList() {
  try {
    const res = await request.get('/system/evaluation/page', {
      params: {
        pageNum: pageNum.value, pageSize: pageSize.value,
        content: searchForm.content || undefined,
        rating: searchForm.rating !== '' ? searchForm.rating : undefined,
        status: searchForm.status !== '' ? searchForm.status : undefined
      }
    })
    if (res.data.code === 200) {
      evaluationList.value = res.data.data?.rows || res.data.data || []
      total.value = res.data.data?.total || 0
    }
  } catch (e) { console.error('获取评价列表失败', e) }
}

function handleSearch() { pageNum.value = 1; fetchEvaluationList() }
function resetSearch() { searchForm.content = ''; searchForm.rating = ''; searchForm.status = ''; handleSearch() }

function handlePageChange(p) { pageNum.value = p; fetchEvaluationList() }
function handleSizeChange() { pageNum.value = 1; fetchEvaluationList() }

function handleReply(item) {
  currentEvaluation.value = item
  replyForm.reply = ''
  showReplyModal.value = true
}

function closeReplyModal() { showReplyModal.value = false; currentEvaluation.value = null }

async function handleReplySubmit() {
  submitting.value = true
  try {
    await request.post(`/system/evaluation/${currentEvaluation.value.id}/reply`, { reply: replyForm.reply })
    toast.success('回复成功')
    closeReplyModal()
    fetchEvaluationList()
  } catch (e) { toast.error(e.response?.data?.msg || '回复失败') }
  finally { submitting.value = false }
}

async function handleDelete(item) {
  const confirmed = await confirm({
    title: '删除确认',
    message: '确定要删除该评价吗？',
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/system/evaluation/${item.id}`)
    toast.success('删除成功')
    fetchEvaluationList()
  } catch (e) { toast.error(e.response?.data?.msg || '删除失败') }
}

onMounted(fetchEvaluationList)
</script>

<style scoped>
.evaluation-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }
.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-default { background: #f0f0f0; color: #666; }
.btn-danger { background: #ff4d4f; color: white; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.search-input, .search-select { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; }
.table-container { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.data-table th { background: #f8f9fa; font-weight: 600; color: #333; }
.rating { color: #faad14; }
.content-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.status-tag { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.status-pending { background: #fff7e6; color: #fa8c16; }
.status-replied { background: #f6ffed; color: #52c41a; }
.actions { display: flex; gap: 8px; }
.action-btn { padding: 4px 10px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.action-btn:hover { opacity: 0.85; }
.action-warn { background: #fff7e6; color: #fa8c16; }
.action-warn:hover { background: #fa8c16; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }
.empty-cell { text-align: center; padding: 60px 20px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; }
.empty-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.6; }
.empty-text { font-size: 14px; color: #999; }
.pagination { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-top: 1px solid #f0f0f0; background: #fafbfc; border-radius: 0 0 12px 12px; }
.pagination-left, .pagination-center, .pagination-right { display: flex; align-items: center; gap: 8px; }
.total { font-size: 13px; color: #666; }
.total em { font-style: normal; color: #667eea; font-weight: 600; }
.page-btn { display: inline-flex; align-items: center; gap: 4px; padding: 8px 14px; border: 1px solid #e8e8e8; background: #fff; border-radius: 8px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.25s; }
.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.page-btn:first-child, .page-btn:last-child { padding: 8px 10px; }
.page-info { padding: 0 12px; font-size: 13px; color: #666; }
.page-info strong { color: #667eea; font-size: 15px; font-weight: 600; }
.page-size { padding: 8px 32px 8px 12px; border: 1px solid #e8e8e8; border-radius: 8px; font-size: 13px; color: #666; background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L2 4h8z'/%3E%3C/svg%3E") no-repeat right 10px center; cursor: pointer; outline: none; appearance: none; transition: all 0.2s; }
.page-size:hover { border-color: #667eea; }
.page-size:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 500px; max-height: 90vh; overflow: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }
.evaluation-info { background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.evaluation-info p { margin: 8px 0; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #333; }
.required { color: #ff4d4f; }
.form-group textarea { width: 100%; padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; resize: vertical; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
</style>
