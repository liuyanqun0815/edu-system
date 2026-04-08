<template>
  <div class="menu-page">
    <div class="page-header">
      <h1 class="page-title">菜单管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建菜单</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width: 60px">ID</th>
            <th>菜单名称</th>
            <th>图标</th>
            <th>类型</th>
            <th>路径</th>
            <th>排序</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="menu in menuTree" :key="menu.id">
            <tr :class="{ 'parent-row': menu.children && menu.children.length }">
              <td>{{ menu.id }}</td>
              <td>
                <span class="menu-name" :style="{ paddingLeft: '0px' }">
                  {{ menu.menuName }}
                </span>
              </td>
              <td><span class="menu-icon">{{ menu.icon }}</span></td>
              <td>
                <span :class="['type-tag', `type-${menu.menuType}`]">
                  {{ menuTypeMap[menu.menuType] }}
                </span>
              </td>
              <td><code>{{ menu.path || '-' }}</code></td>
              <td>{{ menu.sortOrder }}</td>
              <td>
                <span :class="['status-tag', menu.status === 1 ? 'status-enabled' : 'status-disabled']">
                  {{ menu.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(menu.createTime) }}</td>
              <td class="actions">
                <button class="action-btn action-edit" @click="handleEdit(menu)">编辑</button>
                <button class="action-btn action-delete" @click="handleDelete(menu)">删除</button>
              </td>
            </tr>
            <!-- 子菜单 -->
            <tr
              v-for="subMenu in menu.children"
              :key="subMenu.id"
              class="sub-row"
            >
              <td>{{ subMenu.id }}</td>
              <td>
                <span class="menu-name" style="padding-left: 30px;">
                  <span class="sub-indicator">└─</span> {{ subMenu.menuName }}
                </span>
              </td>
              <td><span class="menu-icon">{{ subMenu.icon }}</span></td>
              <td>
                <span :class="['type-tag', `type-${subMenu.menuType}`]">
                  {{ menuTypeMap[subMenu.menuType] }}
                </span>
              </td>
              <td><code>{{ subMenu.path || '-' }}</code></td>
              <td>{{ subMenu.sortOrder }}</td>
              <td>
                <span :class="['status-tag', subMenu.status === 1 ? 'status-enabled' : 'status-disabled']">
                  {{ subMenu.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(subMenu.createTime) }}</td>
              <td class="actions">
                <button class="action-btn action-edit" @click="handleEdit(subMenu)">编辑</button>
                <button class="action-btn action-delete" @click="handleDelete(subMenu)">删除</button>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑菜单' : '新建菜单' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>菜单名称 <span class="required">*</span></label>
                <input v-model="form.menuName" type="text" placeholder="请输入菜单名称" required>
              </div>
              <div class="form-group">
                <label>上级菜单</label>
                <select v-model="form.parentId">
                  <option :value="0">无（作为一级菜单）</option>
                  <option v-for="menu in parentMenus" :key="menu.id" :value="menu.id">
                    {{ menu.menuName }}
                  </option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>菜单类型 <span class="required">*</span></label>
                <select v-model="form.menuType" required>
                  <option :value="1">目录</option>
                  <option :value="2">菜单</option>
                  <option :value="3">按钮</option>
                </select>
              </div>
              <div class="form-group">
                <label>图标</label>
                <input v-model="form.icon" type="text" placeholder="如：📚">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>路由路径</label>
                <input v-model="form.path" type="text" placeholder="如：/course">
              </div>
              <div class="form-group">
                <label>组件路径</label>
                <input v-model="form.component" type="text" placeholder="如：CourseView">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>权限标识 <span class="perm-hint">(按钮类型必填)</span></label>
                <input v-model="form.permission" type="text" placeholder="如：user:assign:role">
              </div>
              <div class="form-group">
                <label>排序</label>
                <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm } = useMessage()

const menuTypeMap = { 1: '目录', 2: '菜单', 3: '按钮' }

// 数据列表
const menuList = ref([])
const menuTree = ref([])

// 弹窗控制
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

// 表单数据
const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuType: 2,
  icon: '',
  path: '',
  component: '',
  permission: '',
  sortOrder: 0,
  status: 1
})

// 可作为父菜单的列表（一级菜单）
const parentMenus = computed(() => {
  return menuList.value.filter(m => m.parentId === 0 || m.parentId === null)
})

// 获取菜单列表
async function fetchMenuList() {
  try {
    const res = await request.get('/system/menu/list')
    if (res.data.code === 200) {
      menuList.value = res.data.data || []
      buildMenuTree()
    }
  } catch (e) {
    console.error('获取菜单列表失败', e)
    toast.error('获取菜单列表失败')
  }
}

// 构建菜单树
function buildMenuTree() {
  const tree = []
  const map = {}

  // 先创建映射
  menuList.value.forEach(menu => {
    map[menu.id] = { ...menu, children: [] }
  })

  // 构建树
  menuList.value.forEach(menu => {
    if (menu.parentId === 0 || menu.parentId === null) {
      tree.push(map[menu.id])
    } else if (map[menu.parentId]) {
      map[menu.parentId].children.push(map[menu.id])
    }
  })

  // 排序
  tree.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  tree.forEach(node => {
    if (node.children) {
      node.children.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    }
  })

  menuTree.value = tree
}

// 打开新增弹窗
function openAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function handleEdit(menu) {
  isEdit.value = true
  Object.assign(form, {
    id: menu.id,
    parentId: menu.parentId || 0,
    menuName: menu.menuName,
    menuType: menu.menuType,
    icon: menu.icon || '',
    path: menu.path || '',
    component: menu.component || '',
    permission: menu.permission || '',
    sortOrder: menu.sortOrder || 0,
    status: menu.status
  })
  showModal.value = true
}

// 关闭弹窗
function closeModal() {
  showModal.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: null, parentId: 0, menuName: '',
    menuType: 2, icon: '', path: '',
    component: '', permission: '', sortOrder: 0, status: 1
  })
}

// 提交表单
async function handleSubmit() {
  submitting.value = true
  try {
    const data = {
      parentId: form.parentId || 0,
      menuName: form.menuName,
      menuType: form.menuType,
      icon: form.icon,
      path: form.path,
      component: form.component,
      permission: form.permission || null,
      sort: form.sortOrder,
      status: form.status
    }

    if (isEdit.value) {
      data.id = form.id
      const res = await request.put('/system/menu', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeModal()
        fetchMenuList()
      } else {
        toast.error(res.data.msg || '修改失败')
      }
    } else {
      const res = await request.post('/system/menu', data)
      if (res.data.code === 200) {
        toast.success('新增成功')
        closeModal()
        fetchMenuList()
      } else {
        toast.error(res.data.msg || '新增失败')
      }
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除菜单
async function handleDelete(menu) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除菜单 <b>"${menu.menuName}"</b> 吗？<br><small style="color:#999">此操作不可恢复！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/system/menu/${menu.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchMenuList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 格式化日期时间
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

onMounted(fetchMenuList)
</script>

<style scoped>
.menu-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.btn {
  padding: 10px 25px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn-default:hover {
  background: #e0e0e0;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table tr:hover {
  background: #f8f9fa;
}

.parent-row {
  background: #fafafa;
  font-weight: 500;
}

.sub-row {
  background: white;
}

.sub-row .menu-name {
  color: #666;
}

.sub-indicator {
  color: #999;
  margin-right: 5px;
}

.menu-icon {
  font-size: 18px;
}

code {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.type-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.type-1 {
  background: #e6f7ff;
  color: #1890ff;
}

.type-2 {
  background: #f6ffed;
  color: #52c41a;
}

.type-3 {
  background: #fff7e6;
  color: #fa8c16;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-enabled {
  background: #f6ffed;
  color: #52c41a;
}

.status-disabled {
  background: #fff1f0;
  color: #ff4d4f;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn:hover { opacity: 0.85; transform: translateY(-1px); }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

.perm-hint {
  font-size: 11px;
  color: #999;
  font-weight: normal;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.required {
  color: #ff4d4f;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #667eea;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
</style>
