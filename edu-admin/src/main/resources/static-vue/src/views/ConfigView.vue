<template>
  <div class="config-page">
    <div class="page-header">
      <h1 class="page-title">配置管理</h1>
      <div class="header-actions">
        <button class="btn btn-default" @click="fetchCategoryList">🔄 刷新</button>
        <button class="btn btn-primary" @click="openAddModal">+ 新增分类</button>
      </div>
    </div>

    <div class="search-bar">
      <input v-model="searchForm.name" type="text" class="search-input" placeholder="搜索分类名称" @keyup.enter="handleSearch">
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 配置分类卡片 -->
    <div class="config-grid">
      <div v-for="category in filteredCategories" :key="category.id" class="config-card">
        <div class="card-header" :style="{ background: getCategoryColor(category.code) }">
          <div class="card-icon">{{ category.icon || getCategoryIcon(category.code) }}</div>
          <div class="card-title-wrap">
            <div class="card-title">{{ category.name }}</div>
            <div class="card-code">{{ category.code }}</div>
          </div>
          <div class="card-status">
            <label class="mini-switch">
              <input type="checkbox" :checked="category.status === 1" @change="toggleCategoryStatus(category)">
              <span class="mini-slider"></span>
            </label>
          </div>
          <div class="card-actions">
            <button class="icon-btn" @click="handleEdit(category)" title="编辑">✏️</button>
            <button class="icon-btn" @click="handleManageItems(category)" title="管理配置项">⚙️</button>
            <button class="icon-btn danger" @click="handleDelete(category)" title="删除">🗑️</button>
          </div>
        </div>
        
        <div class="card-body">
          <div class="config-items-list">
            <div v-for="item in (category.items || [])" :key="item.id" class="config-item">
              <div class="item-header">
                <span class="item-name">
                  <span v-if="item.itemColor" class="item-color-dot" :style="{ background: item.itemColor }"></span>
                  {{ item.itemName || item.itemLabel }}
                </span>
                <span class="item-value-badge">{{ item.itemValue }}</span>
              </div>
              <div class="item-meta">
                <span class="item-sort">排序: {{ item.sortOrder || item.sort }}</span>
                <span class="item-status" :class="{ disabled: item.status === 0 }">{{ item.status === 0 ? '禁用' : '启用' }}</span>
              </div>
            </div>
            <div v-if="!category.items || category.items.length === 0" class="empty-items">
              <span class="empty-icon">📭</span>
              <span>暂无配置项</span>
            </div>
          </div>
          <div class="card-footer">
            <span class="item-count">{{ (category.items || []).length }} 项配置</span>
            <button class="add-item-btn" @click="handleManageItems(category)">+ 添加配置项</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '✏️ 编辑分类' : '➕ 新增分类' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>分类编码 <span class="required">*</span></label>
              <input v-model="form.code" type="text" placeholder="如：course_category" required :disabled="isEdit">
              <div class="field-hint">编码用于程序内部标识，创建后不可修改</div>
            </div>
            <div class="form-group">
              <label>分类名称 <span class="required">*</span></label>
              <input v-model="form.name" type="text" placeholder="如：课程分类" required>
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="form.description" rows="2" placeholder="请输入分类描述"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 配置项管理弹窗 -->
    <div v-if="showItemModal" class="modal-overlay" @click.self="closeItemModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>⚙️ 管理配置项 - {{ currentCategory?.name }}</h3>
          <button class="close-btn" @click="closeItemModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="item-add-form">
            <div class="form-row">
              <div class="form-group flex-1">
                <input v-model="itemForm.itemName" type="text" placeholder="配置项名称" class="inline-input">
              </div>
              <div class="form-group flex-1">
                <input v-model="itemForm.itemValue" type="text" placeholder="配置项值" class="inline-input">
              </div>
              <div class="form-group" style="width: 100px;">
                <input v-model.number="itemForm.sortOrder" type="number" placeholder="排序" class="inline-input">
              </div>
              <div class="form-group" style="width: 80px;">
                <input v-model="itemForm.itemColor" type="color" class="color-picker" title="颜色">
              </div>
              <button class="btn btn-primary btn-sm" @click="addItem">添加</button>
            </div>
          </div>
          
          <div class="items-table">
            <div class="items-header">
              <span class="col-name">名称</span>
              <span class="col-value">值</span>
              <span class="col-sort">排序</span>
              <span class="col-action">操作</span>
            </div>
            <div class="items-body">
              <div v-for="(item, index) in currentItems" :key="item.id || index" class="item-row">
                <span class="col-name">
                  <input v-model="item.itemName" class="inline-edit">
                </span>
                <span class="col-value">
                  <input v-model="item.itemValue" class="inline-edit">
                </span>
                <span class="col-sort">
                  <input v-model.number="item.sortOrder" type="number" class="inline-edit sort">
                </span>
                <span class="col-color">
                  <input v-model="item.itemColor" type="color" class="color-picker-sm">
                </span>
                <span class="col-status">
                  <label class="mini-switch">
                    <input type="checkbox" :checked="item.status !== 0" @change="item.status = item.status === 0 ? 1 : 0">
                    <span class="mini-slider"></span>
                  </label>
                </span>
                <span class="col-action">
                  <button class="btn-danger-sm" @click="removeItem(index)">删除</button>
                </span>
              </div>
              <div v-if="currentItems.length === 0" class="empty-row">暂无配置项，请添加</div>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closeItemModal">取消</button>
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="saveItems">{{ submitting ? '保存中...' : '保存配置项' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm } = useMessage()

const searchForm = reactive({ name: '' })
const categoryList = ref([])
const showModal = ref(false)
const showItemModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentCategory = ref(null)
const currentItems = ref([])
const form = reactive({ id: null, code: '', name: '', description: '' })
const itemForm = reactive({ itemName: '', itemValue: '', sortOrder: 0, itemColor: '', status: 1 })

// 分类颜色映射
const categoryColors = {
  'course_category': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'exam_subject': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'user_role': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'question_type': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'difficulty_level': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'paper_type': 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  'grade_level': 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'course_status': 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'exam_status': 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'user_status': 'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)'
}

const categoryIcons = {
  'course_category': '📚',
  'exam_subject': '📝',
  'user_role': '👤',
  'question_type': '❓',
  'difficulty_level': '⭐',
  'paper_type': '📄',
  'grade_level': '🎓',
  'course_status': '📊',
  'exam_status': '📋',
  'user_status': '👥'
}

function getCategoryColor(code) {
  return categoryColors[code] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

function getCategoryIcon(code) {
  return categoryIcons[code] || '📁'
}

const filteredCategories = computed(() => {
  if (!searchForm.name) return categoryList.value
  return categoryList.value.filter(c => 
    c.name.includes(searchForm.name) || c.code.includes(searchForm.name)
  )
})

async function fetchCategoryList() {
  try {
    const res = await request.get('/system/config/category/list')
    if (res.data.code === 200) categoryList.value = res.data.data || []
  } catch (e) { console.error('获取配置分类失败', e) }
}

function handleSearch() { /* 前端过滤 */ }
function resetSearch() { searchForm.name = '' }

function openAddModal() {
  isEdit.value = false
  Object.assign(form, { id: null, code: '', name: '', description: '' })
  showModal.value = true
}

function handleEdit(category) {
  isEdit.value = true
  Object.assign(form, { ...category })
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/system/config/category', form)
      toast.success('修改成功')
    } else {
      await request.post('/system/config/category', form)
      toast.success('新增成功')
    }
    closeModal()
    fetchCategoryList()
  } catch (e) { toast.error(e.response?.data?.msg || '操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(category) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除分类 <b>"${category.name}"</b> 吗？<br><small style="color:#999">该分类下的所有配置项也将被删除！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/system/config/category/${category.id}`)
    toast.success('删除成功')
    fetchCategoryList()
  } catch (e) { toast.error(e.response?.data?.msg || '删除失败') }
}

function handleManageItems(category) {
  currentCategory.value = category
  currentItems.value = category.items ? JSON.parse(JSON.stringify(category.items)) : []
  itemForm.itemName = ''
  itemForm.itemValue = ''
  itemForm.sortOrder = currentItems.value.length
  showItemModal.value = true
}

function closeItemModal() { showItemModal.value = false; currentCategory.value = null; currentItems.value = [] }

function addItem() {
  if (!itemForm.itemName || !itemForm.itemValue) {
    toast.warning('请输入配置项名称和值')
    return
  }
  currentItems.value.push({ ...itemForm, id: null, status: 1 })
  itemForm.itemName = ''
  itemForm.itemValue = ''
  itemForm.sortOrder = currentItems.value.length
  itemForm.itemColor = ''
}

function removeItem(index) { currentItems.value.splice(index, 1) }

async function saveItems() {
  submitting.value = true
  try {
    await request.post(`/system/config/category/${currentCategory.value.id}/items`, { items: currentItems.value })
    toast.success('保存成功')
    closeItemModal()
    fetchCategoryList()
  } catch (e) { toast.error(e.response?.data?.msg || '保存失败') }
  finally { submitting.value = false }
}

// 切换分类状态
async function toggleCategoryStatus(category) {
  const newStatus = category.status === 1 ? 0 : 1
  try {
    await request.put(`/system/config/category/${category.id}`, { ...category, status: newStatus })
    category.status = newStatus
    toast.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (e) {
    toast.error('操作失败')
  }
}

onMounted(fetchCategoryList)
</script>

<style scoped>
.config-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }

.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; transition: all 0.2s; }
.btn-sm { padding: 8px 16px; font-size: 13px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(102,126,234,0.4); }
.btn-default { background: #f0f0f0; color: #666; }
.btn-danger-sm { padding: 5px 12px; border: none; border-radius: 4px; background: #ff4d4f; color: white; font-size: 12px; cursor: pointer; }

.search-bar { display: flex; gap: 12px; margin-bottom: 24px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.search-input { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; width: 240px; }
.search-input:focus { border-color: #667eea; }

/* 配置卡片网格 */
.config-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }

.config-card { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: transform 0.2s, box-shadow 0.2s; }
.config-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }

.card-header { padding: 16px 20px; display: flex; align-items: center; gap: 12px; color: white; }
.card-icon { font-size: 28px; }
.card-title-wrap { flex: 1; }
.card-title { font-size: 16px; font-weight: 600; }
.card-code { font-size: 12px; opacity: 0.8; margin-top: 2px; }
.card-actions { display: flex; gap: 6px; }
.icon-btn { background: rgba(255,255,255,0.2); border: none; width: 28px; height: 28px; border-radius: 6px; cursor: pointer; font-size: 14px; transition: background 0.2s; }
.icon-btn:hover { background: rgba(255,255,255,0.3); }
.icon-btn.danger:hover { background: rgba(255,0,0,0.3); }

.card-body { padding: 16px 20px; }
.config-items-list { max-height: 200px; overflow-y: auto; }
.config-item { padding: 10px 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 8px; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.item-name { font-size: 14px; font-weight: 500; color: #333; }
.item-value-badge { font-size: 12px; background: #e6f7ff; color: #1890ff; padding: 2px 8px; border-radius: 4px; }
.item-meta { font-size: 12px; color: #999; }
.empty-items { text-align: center; padding: 20px; color: #999; display: flex; flex-direction: column; align-items: center; gap: 8px; }
.empty-icon { font-size: 32px; }

.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.item-count { font-size: 13px; color: #999; }
.add-item-btn { background: none; border: 1px dashed #667eea; color: #667eea; padding: 5px 12px; border-radius: 6px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.add-item-btn:hover { background: #667eea; color: white; border-style: solid; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 500px; max-height: 90vh; overflow: auto; }
.modal-large { max-width: 700px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; color: #333; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }

.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #555; font-weight: 500; }
.required { color: #ff4d4f; }
.form-group input, .form-group textarea { width: 100%; padding: 10px 14px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-group input:focus, .form-group textarea:focus { border-color: #667eea; }
.field-hint { margin-top: 6px; font-size: 12px; color: #999; }

.form-row { display: flex; gap: 10px; align-items: flex-end; }
.flex-1 { flex: 1; }
.inline-input { padding: 10px 12px !important; }
.inline-edit { padding: 6px 10px; border: 1px solid #e0e0e0; border-radius: 4px; font-size: 13px; width: 100%; box-sizing: border-box; }
.inline-edit.sort { width: 60px; text-align: center; }

.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #f0f0f0; }

/* 配置项表格 */
.items-table { border: 1px solid #f0f0f0; border-radius: 8px; overflow: hidden; margin-top: 16px; }
.items-header { display: flex; background: #f8f9fa; padding: 10px 16px; font-size: 13px; font-weight: 600; color: #666; }
.items-body { max-height: 300px; overflow-y: auto; }
.item-row { display: flex; padding: 10px 16px; border-bottom: 1px solid #f0f0f0; align-items: center; }
.item-row:last-child { border-bottom: none; }
.col-name { flex: 1; }
.col-value { flex: 1; }
.col-sort { width: 80px; text-align: center; }
.col-action { width: 70px; text-align: center; }
.empty-row { text-align: center; padding: 30px; color: #999; }

/* 头部操作区 */
.header-actions { display: flex; gap: 10px; }

/* 迷你开关 */
.mini-switch { position: relative; display: inline-block; width: 36px; height: 18px; }
.mini-switch input { opacity: 0; width: 0; height: 0; }
.mini-slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .3s; border-radius: 18px; }
.mini-slider:before { position: absolute; content: ""; height: 12px; width: 12px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; }
input:checked + .mini-slider { background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%); }
input:checked + .mini-slider:before { transform: translateX(18px); }

/* 卡片状态区 */
.card-status { margin-right: 8px; }

/* 配置项状态 */
.item-status { margin-left: 10px; font-size: 11px; padding: 2px 6px; border-radius: 4px; background: #f6ffed; color: #52c41a; }
.item-status.disabled { background: #f0f0f0; color: #999; }

/* 颜色点 */
.item-color-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; margin-right: 6px; }

/* 颜色选择器 */
.color-picker { width: 100%; height: 38px; padding: 2px; border: 1px solid #e0e0e0; border-radius: 6px; cursor: pointer; }
.color-picker-sm { width: 32px; height: 24px; padding: 0; border: none; border-radius: 4px; cursor: pointer; }

/* 配置项表格列 */
.col-color { width: 50px; text-align: center; }
.col-status { width: 50px; text-align: center; }
</style>
