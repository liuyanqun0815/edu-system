<template>
  <div class="role-page">
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建角色</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>角色名称</th>
            <th>角色编码</th>
            <th>描述</th>
            <th>用户数量</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roleList" :key="role.id">
            <td>{{ role.id }}</td>
            <td>{{ role.roleName }}</td>
            <td><code>{{ role.roleCode }}</code></td>
            <td>{{ role.description || '-' }}</td>
            <td>{{ role.userCount || 0 }}</td>
            <td>
              <span :class="['status-tag', role.status === 1 ? 'status-enabled' : 'status-disabled']">
                {{ role.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(role.createTime) }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(role)">编辑</button>
              <button class="action-btn action-role" @click="handlePermission(role)">权限配置</button>
              <button
                :class="['action-btn', role.status === 1 ? 'action-disable' : 'action-enable']"
                @click="handleToggleStatus(role)"
              >
                {{ role.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="action-btn action-delete" @click="handleDelete(role)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑角色' : '新建角色' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>角色名称 <span class="required">*</span></label>
              <input v-model="form.roleName" type="text" placeholder="请输入角色名称" required>
            </div>
            <div class="form-group">
              <label>角色编码 <span class="required">*</span></label>
              <input
                v-model="form.roleCode"
                type="text"
                :disabled="isEdit"
                placeholder="请输入角色编码，如：admin、teacher"
                required
              >
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="form.description" rows="3" placeholder="请输入角色描述"></textarea>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
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

    <!-- 权限配置弹窗 -->
    <div v-if="showPermissionModal" class="modal-overlay" @click.self="closePermissionModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>配置权限 - {{ currentRole?.roleName }}</h3>
          <button class="close-btn" @click="closePermissionModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="permission-tree">
            <div
              v-for="menu in menuTree"
              :key="menu.id"
              class="menu-item"
            >
              <label class="menu-checkbox">
                <input
                  type="checkbox"
                  :checked="isMenuSelected(menu.id)"
                  @change="toggleMenu(menu.id)"
                >
                <span class="menu-icon">{{ menu.icon }}</span>
                <span class="menu-label">{{ menu.menuName }}</span>
              </label>
              <div v-if="menu.children && menu.children.length" class="sub-menu">
                <label
                  v-for="subMenu in menu.children"
                  :key="subMenu.id"
                  class="menu-checkbox sub"
                >
                  <input
                    type="checkbox"
                    :checked="isMenuSelected(subMenu.id)"
                    @change="toggleMenu(subMenu.id)"
                  >
                  <span class="menu-label">{{ subMenu.menuName }}</span>
                </label>
              </div>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closePermissionModal">取消</button>
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="savePermissions">
              {{ submitting ? '保存中...' : '保存权限' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'

const { toast, confirm } = useMessage()

// 数据列表
const roleList = ref([])
const menuTree = ref([])
const selectedMenus = ref([])

// 弹窗控制
const showModal = ref(false)
const showPermissionModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentRole = ref(null)

// 表单数据
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 获取角色列表
async function fetchRoleList() {
  try {
    const res = await request.get('/system/role/list')
    if (res.data.code === 200) {
      roleList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取角色列表失败', e)
    toast.error('获取角色列表失败')
  }
}

// 获取菜单树
async function fetchMenuTree() {
  try {
    const res = await request.get('/system/menu/tree')
    if (res.data.code === 200) {
      menuTree.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取菜单树失败', e)
  }
}

// 获取角色权限
async function fetchRoleMenus(roleId) {
  try {
    const res = await request.get(`/system/role/${roleId}/menus`)
    if (res.data.code === 200) {
      selectedMenus.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取角色权限失败', e)
    selectedMenus.value = []
  }
}

// 打开新增弹窗
function openAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function handleEdit(role) {
  isEdit.value = true
  currentRole.value = role
  Object.assign(form, {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    description: role.description,
    status: role.status
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
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
}

// 提交表单
async function handleSubmit() {
  submitting.value = true
  try {
    const data = {
      roleName: form.roleName,
      roleCode: form.roleCode,
      description: form.description,
      status: form.status
    }

    if (isEdit.value) {
      data.id = form.id
      const res = await request.put('/system/role', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeModal()
        fetchRoleList()
      } else {
        toast.error(res.data.msg || '修改失败')
      }
    } else {
      const res = await request.post('/system/role', data)
      if (res.data.code === 200) {
        toast.success('新增成功')
        closeModal()
        fetchRoleList()
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

// 切换状态
async function handleToggleStatus(role) {
  const action = role.status === 1 ? '禁用' : '启用'
  const confirmed = await confirm({
    title: `${action}确认`,
    message: `确定要${action}角色 <b>"${role.roleName}"</b> 吗？`,
    type: 'warning',
    confirmText: action
  })
  if (!confirmed) return

  try {
    const res = await request.put('/system/role', {
      id: role.id,
      status: role.status === 1 ? 0 : 1
    })
    if (res.data.code === 200) {
      toast.success(`${action}成功`)
      fetchRoleList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  }
}

// 删除角色
async function handleDelete(role) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除角色 <b>"${role.roleName}"</b> 吗？<br><small style="color:#999">此操作不可恢复！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/system/role/${role.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchRoleList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 打开权限配置
async function handlePermission(role) {
  currentRole.value = role
  selectedMenus.value = []
  await fetchMenuTree()
  await fetchRoleMenus(role.id)
  showPermissionModal.value = true
}

// 关闭权限弹窗
function closePermissionModal() {
  showPermissionModal.value = false
  currentRole.value = null
  selectedMenus.value = []
}

// 判断是否选中
function isMenuSelected(menuId) {
  return selectedMenus.value.includes(menuId)
}

// 切换菜单选择
function toggleMenu(menuId) {
  const index = selectedMenus.value.indexOf(menuId)
  if (index > -1) {
    selectedMenus.value.splice(index, 1)
  } else {
    selectedMenus.value.push(menuId)
  }
}

// 保存权限
async function savePermissions() {
  if (!currentRole.value) return

  submitting.value = true
  try {
    const res = await request.post(`/system/role/${currentRole.value.id}/menus`, {
      menuIds: selectedMenus.value
    })
    if (res.data.code === 200) {
      toast.success('权限配置成功')
      closePermissionModal()
    } else {
      toast.error(res.data.msg || '配置失败')
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '配置失败')
  } finally {
    submitting.value = false
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

onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-page {
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

.btn-success {
  background: #52c41a;
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

code {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
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
  flex-wrap: wrap;
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
.action-role { background: #f9f0ff; color: #722ed1; }
.action-role:hover { background: #722ed1; color: #fff; }
.action-disable { background: #fff1f0; color: #ff4d4f; }
.action-disable:hover { background: #ff4d4f; color: #fff; }
.action-enable { background: #f6ffed; color: #52c41a; }
.action-enable:hover { background: #52c41a; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

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
  max-width: 500px;
  max-height: 90vh;
  overflow: auto;
}

.modal-large {
  max-width: 600px;
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

.form-group {
  margin-bottom: 20px;
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
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #667eea;
}

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 权限树样式 */
.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.menu-item {
  margin-bottom: 10px;
}

.menu-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.menu-checkbox:hover {
  background: #f5f5f5;
}

.menu-checkbox input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.menu-icon {
  font-size: 18px;
}

.menu-label {
  font-size: 14px;
  color: #333;
}

.sub-menu {
  margin-left: 40px;
  margin-top: 5px;
}

.menu-checkbox.sub {
  padding: 8px;
}
</style>
