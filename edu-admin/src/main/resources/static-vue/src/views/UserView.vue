<template>
  <div class="user-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <button v-if="hasPermission('user:add')" class="btn btn-primary" @click="openAddModal">+ 新建用户</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchForm.username"
        type="text"
        class="search-input"
        placeholder="用户名"
        @keyup.enter="handleSearch"
      >
      <input
        v-model="searchForm.nickname"
        type="text"
        class="search-input"
        placeholder="昵称"
        @keyup.enter="handleSearch"
      >
      <select v-model="searchForm.status" class="search-select">
        <option value="">全部状态</option>
        <option value="1">启用</option>
        <option value="0">禁用</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户信息</th>
            <th>邮箱/手机</th>
            <th>所属上级</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in userList" :key="user.id">
            <td>
              <div class="user-info">
                <img v-if="user.avatar" :src="user.avatar" class="user-avatar" alt="头像">
                <div v-else class="user-avatar-letter" :style="{ background: getAvatarColor(user) }">
                  {{ getUserInitial(user) }}
                </div>
                <div class="user-meta">
                  <div class="user-name">{{ user.username }}</div>
                  <div class="user-nick">{{ user.nickname || '—' }}</div>
                </div>
              </div>
            </td>
            <td>
              <div class="contact-info">
                <div>{{ user.email || '—' }}</div>
                <div class="phone-text">{{ user.phone || '—' }}</div>
              </div>
            </td>
            <td>
              <span v-if="user.parentId" class="parent-tag">
                {{ getParentName(user.parentId) }}
              </span>
              <span v-else class="no-parent">—</span>
            </td>
            <td>
              <span :class="['status-tag', user.status === 1 ? 'status-enabled' : 'status-disabled']">
                {{ user.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(user)">编辑</button>
              <button v-if="hasPermission('user:assign:role')" class="action-btn action-role" @click="handleAssignRole(user)">分配角色</button>
              <button v-if="hasPermission('user:assign:perm')" class="action-btn action-perm" @click="handleAssignPermission(user)">分配权限</button>
              <button v-if="hasPermission('user:reset:pwd')" class="action-btn action-warn" @click="handleResetPwd(user)">重置密码</button>
              <button
                :class="['action-btn', user.status === 1 ? 'action-disable' : 'action-enable']"
                @click="handleToggleStatus(user)"
              >{{ user.status === 1 ? '禁用' : '启用' }}</button>
              <button v-if="hasPermission('user:delete')" class="action-btn action-delete" @click="handleDelete(user)">删除</button>
              <button class="action-btn action-parent" @click="handleManageParent(user)">👨‍👧 家长</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <span class="total">共 {{ total }} 条</span>
        <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">上一页</button>
        <template v-for="p in pageNumbers" :key="p">
          <button
            v-if="p !== '...'"
            :class="['page-num', { active: p === pageNum }]"
            @click="handlePageChange(p)"
          >{{ p }}</button>
          <span v-else class="page-ellipsis">…</span>
        </template>
        <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">下一页</button>
        <select v-model="pageSize" class="page-size" @change="handleSizeChange">
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
          <option :value="50">50条/页</option>
        </select>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑用户' : '新建用户' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>用户名 <span class="required">*</span></label>
                <input
                  v-model="form.username"
                  type="text"
                  :disabled="isEdit"
                  placeholder="请输入用户名"
                  required
                >
              </div>
              <div class="form-group">
                <label>昵称</label>
                <input v-model="form.nickname" type="text" placeholder="请输入昵称">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="form.email" type="email" placeholder="请输入邮箱">
              </div>
              <div class="form-group">
                <label>手机号</label>
                <input v-model="form.phone" type="tel" placeholder="请输入手机号">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>性别</label>
                <select v-model="form.sex">
                  <option :value="0">未知</option>
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>所属上级 <small class="hint-text">（只能选择比自己高一级的用户）</small></label>
              <select v-model="form.parentId">
                <option :value="null">无（顶级用户）</option>
                <option v-for="u in selectableUsers" :key="u.id" :value="u.id">
                  {{ u.nickname || u.username }} ({{ roleLevelMap[u.roleLevel] || '未知' }})
                </option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>身份证号</label>
                <input v-model="form.idCard" type="text" placeholder="请输入身份证号">
              </div>
              <div class="form-group">
                <label>出生日期</label>
                <input v-model="form.birthday" type="date" placeholder="请选择出生日期">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>入学/入职日期</label>
                <input v-model="form.joinDate" type="date" placeholder="请选择入学/入职日期">
              </div>
              <div class="form-group">
                <label>年级</label>
                <select v-model="form.gradeId">
                  <option :value="null">请选择年级</option>
                  <option v-for="grade in gradeList" :key="grade.id" :value="grade.id">
                    {{ grade.itemName || grade.itemLabel || grade.name }}
                  </option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>常用地址</label>
              <input v-model="form.address" type="text" placeholder="请输入常用地址（用于通勤时间估算）">
            </div>
            <div v-if="!isEdit" class="form-row">
              <div class="form-group">
                <label>密码 <span class="required">*</span></label>
                <input v-model="form.password" type="password" placeholder="请输入密码" required>
              </div>
              <div class="form-group">
                <label>确认密码 <span class="required">*</span></label>
                <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" required>
              </div>
            </div>
            <div class="form-group">
              <label>头像</label>
              <div class="avatar-upload">
                <img v-if="form.avatar" :src="form.avatar" class="preview-avatar">
                <div v-else class="avatar-placeholder">暂无头像</div>
                <input type="file" accept="image/*" @change="handleAvatarUpload">
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

    <!-- 重置密码弹窗 -->
    <div v-if="showPwdModal" class="modal-overlay" @click.self="closePwdModal">
      <div class="modal modal-small">
        <div class="modal-header">
          <h3>重置密码</h3>
          <button class="close-btn" @click="closePwdModal">&times;</button>
        </div>
        <div class="modal-body">
          <p class="tip">正在为 <strong>{{ currentUser?.username }}</strong> 重置密码</p>
          <form @submit.prevent="handleResetPwdSubmit">
            <div class="form-group">
              <label>新密码 <span class="required">*</span></label>
              <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" required>
            </div>
            <div class="form-group">
              <label>确认密码 <span class="required">*</span></label>
              <input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" required>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closePwdModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '重置中...' : '确认重置' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 分配角色弹窗 -->
    <div v-if="showRoleModal" class="modal-overlay" @click.self="closeRoleModal">
      <div class="modal">
        <div class="modal-header">
          <h3>分配角色 - {{ currentUser?.username }}</h3>
          <button class="close-btn" @click="closeRoleModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="tip-box">
            <p>⚠️ 说明：系统会根据用户的角色判断可见活动和菜单权限。一个用户可有多个角色。</p>
          </div>
          <div class="role-list">
            <div v-for="role in allRoles" :key="role.id" class="role-item">
              <input
                type="checkbox"
                :id="'role' + role.id"
                :checked="selectedRoleIds.includes(role.id)"
                @change="toggleRole(role.id, $event.target.checked)"
              >
              <label :for="'role' + role.id" class="role-label">
                <strong>{{ role.roleName }}</strong>
                <span class="role-code">{{ role.roleCode }}</span>
              </label>
              <span class="role-desc">{{ role.description || '' }}</span>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closeRoleModal">取消</button>
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="saveUserRoles">
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分配权限弹窗 -->
    <div v-if="showPermModal" class="modal-overlay" @click.self="closePermModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>分配用户权限 - {{ currentUser?.username }}</h3>
          <button class="close-btn" @click="closePermModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="tip-box info">
            <p>💡 提示：用户级别权限优先级高于角色权限。如果为用户单独分配了权限，将使用用户权限；否则使用角色权限。</p>
          </div>
          
          <!-- 菜单权限 -->
          <div class="perm-section">
            <div class="perm-section-title">
              <span class="section-icon">📁</span>
              菜单权限
              <span class="section-badge">{{ menuPermissions.length }}</span>
            </div>
            <div class="permission-tree">
              <div v-for="menu in menuPermissions" :key="menu.id" class="perm-item">
                <label class="perm-checkbox" :class="{ 'is-menu': menu.menuType === 2, 'is-dir': menu.menuType === 1 }">
                  <input
                    type="checkbox"
                    :checked="selectedMenuIds.includes(menu.id)"
                    @change="toggleMenu(menu.id, $event.target.checked)"
                  >
                  <span class="perm-icon">{{ menu.icon || (menu.menuType === 1 ? '📂' : '📄') }}</span>
                  <span class="perm-name">{{ menu.menuName }}</span>
                  <span class="perm-type-tag" :class="'type-' + menu.menuType">{{ menuTypeText[menu.menuType] }}</span>
                </label>
                <div v-if="menu.children && menu.children.length" class="perm-children">
                  <label
                    v-for="child in menu.children"
                    :key="child.id"
                    class="perm-checkbox sub"
                    :class="{ 'is-menu': child.menuType === 2, 'is-button': child.menuType === 3 }"
                  >
                    <input
                      type="checkbox"
                      :checked="selectedMenuIds.includes(child.id)"
                      @change="toggleMenu(child.id, $event.target.checked)"
                    >
                    <span class="perm-icon">{{ child.icon || (child.menuType === 3 ? '🔘' : '📄') }}</span>
                    <span class="perm-name">{{ child.menuName }}</span>
                    <span v-if="child.permission" class="perm-code">{{ child.permission }}</span>
                    <span class="perm-type-tag" :class="'type-' + child.menuType">{{ menuTypeText[child.menuType] }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 按钮权限 -->
          <div class="perm-section" v-if="buttonPermissions.length > 0">
            <div class="perm-section-title">
              <span class="section-icon">🔘</span>
              按钮权限
              <span class="section-badge">{{ buttonPermissions.length }}</span>
            </div>
            <div class="permission-tree button-tree">
              <div v-for="btn in buttonPermissions" :key="btn.id" class="perm-item">
                <label class="perm-checkbox is-button">
                  <input
                    type="checkbox"
                    :checked="selectedMenuIds.includes(btn.id)"
                    @change="toggleMenu(btn.id, $event.target.checked)"
                  >
                  <span class="perm-icon">🔘</span>
                  <span class="perm-name">{{ btn.menuName }}</span>
                  <span v-if="btn.permission" class="perm-code">{{ btn.permission }}</span>
                  <span class="perm-type-tag type-3">按钮</span>
                </label>
              </div>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-default" @click="closePermModal">取消</button>
            <button type="button" class="btn btn-primary" :disabled="submitting" @click="saveUserPermissions">
              {{ submitting ? '保存中...' : '保存权限' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 家长关联弹窗 -->
    <div v-if="showParentModal" class="modal-overlay" @click.self="closeParentModal">
      <div class="modal">
        <div class="modal-header">
          <h3>家长信息 - {{ currentUser?.username }}</h3>
          <button class="close-btn" @click="closeParentModal">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 已绑定的家长列表 -->
          <div v-if="userParents.length > 0" class="parent-list">
            <div class="section-title">已关联家长</div>
            <div v-for="p in userParents" :key="p.id" class="parent-card">
              <div class="parent-card-info">
                <div class="parent-name">{{ p.name }} <span class="relation-tag">{{ p.relation || '' }}</span></div>
                <div class="parent-contacts">
                  <span v-if="p.phone">📱 {{ p.phone }}</span>
                  <span v-if="p.wechat">💬 {{ p.wechat }}</span>
                  <span v-if="p.email">📧 {{ p.email }}</span>
                </div>
              </div>
              <button class="action-btn action-delete" @click="unbindParent(p)">解绑</button>
            </div>
          </div>
          <div v-else class="no-parent-tip">暂未关联家长</div>

          <!-- 绑定/新增家长表单 -->
          <div class="section-title mt-16">新增/绑定家长</div>
          <form @submit.prevent="bindParent">
            <div class="form-row">
              <div class="form-group">
                <label>姓名 <span class="required">*</span></label>
                <input v-model="parentForm.name" type="text" placeholder="家长姓名" required>
              </div>
              <div class="form-group">
                <label>关系</label>
                <select v-model="parentForm.relation">
                  <option value="父亲">父亲</option>
                  <option value="母亲">母亲</option>
                  <option value="祖父">祖父</option>
                  <option value="祖母">祖母</option>
                  <option value="其他">其他</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>手机号</label>
                <input v-model="parentForm.phone" type="tel" placeholder="联系手机">
              </div>
              <div class="form-group">
                <label>微信号</label>
                <input v-model="parentForm.wechat" type="text" placeholder="微信号">
              </div>
            </div>
            <div class="form-group">
              <label>邮箱（用于接收上课提醒）</label>
              <input v-model="parentForm.email" type="email" placeholder="邮箱地址">
            </div>
            <div class="form-group">
              <label>备注</label>
              <input v-model="parentForm.remark" type="text" placeholder="备注说明">
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeParentModal">关闭</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存并关联' }}
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
import { usePermission } from '@/composables/usePermission'

const { toast, confirm } = useMessage()
const { hasPermission } = usePermission()

const defaultAvatar = null  // 使用字母头像替代外部CDN

// 获取用户名首字母（用于默认头像）
function getUserInitial(user) {
  const name = user.nickname || user.username || '?'
  return name.charAt(0).toUpperCase()
}

// 默认头像背景色
const avatarColors = ['#667eea','#f5576c','#43e97b','#fa709a','#4facfe','#f093fb','#f6d365','#96fbc4']
function getAvatarColor(user) {
  const id = user.id || 0
  return avatarColors[id % avatarColors.length]
}

const sexMap = { 0: '未知', 1: '男', 2: '女' }

// 菜单类型文本映射
const menuTypeText = { 1: '目录', 2: '菜单', 3: '按钮' }

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  status: ''
})

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

// 数据列表
const userList = ref([])
// 所有用户（用于显示上级名称）
const allUsersMap = ref({})  // { id: { username, nickname } }

// 弹窗控制
const showModal = ref(false)
const showPwdModal = ref(false)
const showRoleModal = ref(false)
const showPermModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentUser = ref(null)

// 角色分配相关
const allRoles = ref([])
const selectedRoleIds = ref([])

// 权限分配相关
const menuTree = ref([])
const selectedMenuIds = ref([])

// 分离菜单和按钮权限
const menuPermissions = computed(() => {
  return menuTree.value.filter(m => m.menuType === 1 || m.menuType === 2)
})

const buttonPermissions = computed(() => {
  // 从菜单树中提取所有按钮类型（menuType=3）
  const buttons = []
  menuTree.value.forEach(menu => {
    if (menu.children) {
      menu.children.forEach(child => {
        if (child.menuType === 3) {
          buttons.push(child)
        }
      })
    }
  })
  return buttons
})

// 家长关联相关
const showParentModal = ref(false)
const userParents = ref([])
const parentForm = ref({ name: '', relation: '父亲', phone: '', wechat: '', email: '', remark: '' })

// 年级列表
const gradeList = ref([])

// 加载年级列表
async function loadGradeList() {
  try {
    const res = await request.get('/system/config/items', { params: { category: 'grade' } })
    if (res.data.code === 200) {
      gradeList.value = res.data.data || []
    }
  } catch (e) { /* ignore */ }
}

async function handleManageParent(user) {
  currentUser.value = user
  userParents.value = []
  parentForm.value = { name: '', relation: '父亲', phone: '', wechat: '', email: '', remark: '' }
  showParentModal.value = true
  try {
    const res = await request.get(`/system/parent/by-student/${user.id}`)
    if (res.data.code === 200) userParents.value = res.data.data || []
  } catch (e) { /* ignore */ }
}

function closeParentModal() {
  showParentModal.value = false
  userParents.value = []
}

async function bindParent() {
  submitting.value = true
  try {
    // 新建家长并关联
    const res = await request.post('/system/parent', {
      ...parentForm.value,
      studentId: currentUser.value.id
    })
    if (res.data.code === 200) {
      toast.success('家长关联成功')
      // 刷新家长列表
      const listRes = await request.get(`/system/parent/by-student/${currentUser.value.id}`)
      if (listRes.data.code === 200) userParents.value = listRes.data.data || []
      parentForm.value = { name: '', relation: '父亲', phone: '', wechat: '', email: '', remark: '' }
    } else {
      toast.error(res.data.msg || '关联失败')
    }
  } catch (e) {
    toast.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function unbindParent(parent) {
  const ok = await confirm({ title: '解绑确认', message: `确认解绑家长「${parent.name}」？`, type: 'warning', confirmText: '解绑' })
  if (!ok) return
  try {
    await request.delete(`/system/parent/unbind`, { params: { userId: currentUser.value.id, parentId: parent.id } })
    userParents.value = userParents.value.filter(p => p.id !== parent.id)
    toast.success('解绑成功')
  } catch (e) { toast.error('解绑失败') }
}

// 表单数据
const form = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  address: '',
  sex: 0,
  status: 1,
  password: '',
  confirmPassword: '',
  avatar: '',
  parentId: null,
  idCard: '',
  birthday: '',
  joinDate: '',
  gradeId: null
})

const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 获取上级用户名称
function getParentName(parentId) {
  if (!parentId) return '—'
  const u = allUsersMap.value[parentId]
  if (!u) return 'ID:' + parentId
  return u.nickname || u.username
}

// 获取所有用户用于上级选择下拉框（已废弃，使用selectableParents）
const selectableUsers = computed(() => {
  return selectableParents.value
})

// 加载所有用户映射（id -> user）
async function loadAllUsersMap() {
  try {
    const res = await request.get('/system/user/page', {
      params: { pageNum: 1, pageSize: 1000 }
    })
    if (res.data.code === 200) {
      const users = res.data.data.rows || []
      const map = {}
      users.forEach(u => { map[u.id] = u })
      allUsersMap.value = map
    }
  } catch (e) { /* ignore */ }
}

// 当前用户角色等级
const currentUserRoleLevel = ref(0)

// 可选择的上级用户（只能选高一级角色）
const selectableParents = ref([])

// 角色等级映射（用于显示）
const roleLevelMap = { 0: '学生', 1: '教师', 2: '管理员', 3: '超级管理员' }

// 加载可选择的上级用户
async function loadSelectableParents() {
  try {
    const res = await request.get('/system/user/selectable-parents')
    if (res.data.code === 200) {
      selectableParents.value = res.data.data || []
    }
  } catch (e) { /* ignore */ }
}

// 获取用户列表
async function fetchUserList() {
  try {
    const res = await request.get('/system/user/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        username: searchForm.username || undefined,
        nickname: searchForm.nickname || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      userList.value = res.data.data.rows || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('获取用户列表失败', e)
    toast.error('获取用户列表失败')
  }
}

// 搜索
function handleSearch() {
  pageNum.value = 1
  fetchUserList()
}

// 重置搜索
function resetSearch() {
  searchForm.username = ''
  searchForm.nickname = ''
  searchForm.status = ''
  handleSearch()
}

// 分页
function handlePageChange(newPage) {
  pageNum.value = newPage
  fetchUserList()
}

function handleSizeChange() {
  pageNum.value = 1
  fetchUserList()
}

// 打开新增弹窗
function openAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function handleEdit(user) {
  isEdit.value = true
  currentUser.value = user
  Object.assign(form, {
    id: user.id,
    username: user.username,
    nickname: user.nickname || '',
    email: user.email || '',
    phone: user.phone || '',
    address: user.address || '',
    sex: user.sex || 0,
    status: user.status,
    avatar: user.avatar || '',
    parentId: user.parentId || null,
    idCard: user.idCard || '',
    birthday: user.birthday || '',
    joinDate: user.joinDate || '',
    gradeId: user.gradeId || null
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
    id: null, username: '', nickname: '',
    email: '', phone: '', address: '', sex: 0,
    status: 1, password: '', confirmPassword: '',
    avatar: '', parentId: null,
    idCard: '', birthday: '', joinDate: '', gradeId: null
  })
}

// 头像上传
async function handleAvatarUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64 = e.target.result
    try {
      const res = await request.post('/system/user/upload-avatar', { image: base64 })
      if (res.data.code === 200) {
        form.avatar = res.data.data.url
      }
    } catch (err) {
      toast.error('头像上传失败')
    }
  }
  reader.readAsDataURL(file)
}

// 提交表单
async function handleSubmit() {
  if (!isEdit.value && form.password !== form.confirmPassword) {
    toast.warning('两次输入的密码不一致')
    return
  }

  submitting.value = true
  try {
    const data = {
      username: form.username,
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      address: form.address || null,
      sex: form.sex,
      status: form.status,
      avatar: form.avatar,
      parentId: form.parentId || null,
      idCard: form.idCard || null,
      birthday: form.birthday || null,
      joinDate: form.joinDate || null,
      gradeId: form.gradeId || null
    }

    if (isEdit.value) {
      data.id = form.id
      const res = await request.put('/system/user', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeModal()
        fetchUserList()
      } else {
        toast.error(res.data.msg || '修改失败')
      }
    } else {
      data.password = form.password
      const res = await request.post('/system/user', data)
      if (res.data.code === 200) {
        toast.success('新增成功')
        closeModal()
        fetchUserList()
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
async function handleToggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  const confirmed = await confirm({
    title: `${action}确认`,
    message: `确定要${action}用户 <b>"${user.username}"</b> 吗？`,
    type: 'warning',
    confirmText: action
  })
  if (!confirmed) return

  try {
    const res = await request.put('/system/user', {
      id: user.id,
      status: user.status === 1 ? 0 : 1
    })
    if (res.data.code === 200) {
      toast.success(`${action}成功`)
      fetchUserList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  }
}

// 删除用户
async function handleDelete(user) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除用户 <b>"${user.username}"</b> 吗？<br><small style="color:#999">此操作不可恢复！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/system/user/${user.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchUserList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 打开重置密码弹窗
function handleResetPwd(user) {
  currentUser.value = user
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  showPwdModal.value = true
}

// 关闭密码弹窗
function closePwdModal() {
  showPwdModal.value = false
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

// 提交重置密码
async function handleResetPwdSubmit() {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    toast.warning('两次输入的密码不一致')
    return
  }

  submitting.value = true
  try {
    const res = await request.put('/system/user/password', {
      userId: currentUser.value.id,
      newPassword: pwdForm.newPassword
    })
    if (res.data.code === 200) {
      toast.success('密码重置成功')
      closePwdModal()
    } else {
      toast.error(res.data.msg || '重置失败')
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '重置失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 角色分配 ====================

// 打开角色分配弹窗
async function handleAssignRole(user) {
  currentUser.value = user
  selectedRoleIds.value = []
  showRoleModal.value = true
  await fetchAllRoles()
  await fetchUserRoles(user.id)
}

// 获取所有角色
async function fetchAllRoles() {
  try {
    const res = await request.get('/system/role/list')
    if (res.data.code === 200) {
      allRoles.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取角色列表失败', e)
  }
}

// 获取用户当前角色
async function fetchUserRoles(userId) {
  try {
    const res = await request.get(`/system/user/${userId}/roles`)
    if (res.data.code === 200) {
      selectedRoleIds.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取用户角色失败', e)
  }
}

// 切换角色选择
function toggleRole(roleId, checked) {
  if (checked) {
    if (!selectedRoleIds.value.includes(roleId)) {
      selectedRoleIds.value.push(roleId)
    }
  } else {
    selectedRoleIds.value = selectedRoleIds.value.filter(id => id !== roleId)
  }
}

// 关闭角色弹窗
function closeRoleModal() {
  showRoleModal.value = false
  selectedRoleIds.value = []
}

// 保存用户角色
async function saveUserRoles() {
  submitting.value = true
  try {
    const res = await request.post(`/system/user/${currentUser.value.id}/roles`, {
      roleIds: selectedRoleIds.value
    })
    if (res.data.code === 200) {
      toast.success('角色分配成功')
      closeRoleModal()
    } else {
      toast.error(res.data.msg || '分配失败')
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '分配失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 权限分配 ====================

// 打开权限分配弹窗
async function handleAssignPermission(user) {
  currentUser.value = user
  selectedMenuIds.value = []
  showPermModal.value = true
  await fetchMenuTree()
  await fetchUserMenus(user.id)
}

// 获取菜单树
async function fetchMenuTree() {
  try {
    const res = await request.get('/system/menu/list')
    if (res.data.code === 200) {
      const menus = res.data.data || []
      // 构建树形结构
      const parents = menus.filter(m => !m.parentId || m.parentId === 0)
      menuTree.value = parents.map(p => ({
        ...p,
        children: menus.filter(m => m.parentId === p.id)
      }))
    }
  } catch (e) {
    console.error('获取菜单列表失败', e)
  }
}

// 获取用户当前菜单权限
async function fetchUserMenus(userId) {
  try {
    const res = await request.get(`/system/user/${userId}/menus`)
    if (res.data.code === 200) {
      selectedMenuIds.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取用户权限失败', e)
  }
}

// 切换菜单选择
function toggleMenu(menuId, checked) {
  if (checked) {
    if (!selectedMenuIds.value.includes(menuId)) {
      selectedMenuIds.value.push(menuId)
    }
  } else {
    selectedMenuIds.value = selectedMenuIds.value.filter(id => id !== menuId)
  }
}

// 关闭权限弹窗
function closePermModal() {
  showPermModal.value = false
  selectedMenuIds.value = []
}

// 保存用户权限
async function saveUserPermissions() {
  submitting.value = true
  try {
    const res = await request.post(`/system/user/${currentUser.value.id}/menus`, {
      menuIds: selectedMenuIds.value
    })
    if (res.data.code === 200) {
      toast.success('权限分配成功')
      closePermModal()
    } else {
      toast.error(res.data.msg || '分配失败')
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '分配失败')
  } finally {
    submitting.value = false
  }
}

// 分页按钮计算（带省略号）
const pageNumbers = computed(() => {
  const total_pages = totalPages.value
  if (total_pages <= 7) return Array.from({ length: total_pages }, (_, i) => i + 1)
  const pages = [1]
  if (pageNum.value > 3) pages.push('...')
  const start = Math.max(2, pageNum.value - 1)
  const end = Math.min(total_pages - 1, pageNum.value + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  if (pageNum.value < total_pages - 2) pages.push('...')
  pages.push(total_pages)
  return pages
})

function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(() => {
  fetchUserList()
  loadAllUsersMap()
  loadSelectableParents()
  loadGradeList()
})
</script>

<style scoped>
.user-page {
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

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.search-input,
.search-select {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.search-input {
  width: 180px;
}

.search-input:focus,
.search-select:focus {
  border-color: #667eea;
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
  padding: 10px 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table tr:hover {
  background: #f8f9fa;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.user-avatar-letter {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-meta {
  line-height: 1.3;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.user-nick {
  font-size: 12px;
  color: #999;
}

.contact-info {
  line-height: 1.6;
  font-size: 13px;
}

.phone-text {
  color: #999;
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

/* 语义颜色 */
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }

.action-warn { background: #fff7e6; color: #fa8c16; }
.action-warn:hover { background: #fa8c16; color: #fff; }

.action-disable { background: #fff1f0; color: #ff4d4f; }
.action-disable:hover { background: #ff4d4f; color: #fff; }

.action-enable { background: #f6ffed; color: #52c41a; }
.action-enable:hover { background: #52c41a; color: #fff; }

.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

.action-role { background: #f9f0ff; color: #722ed1; }
.action-role:hover { background: #722ed1; color: #fff; }

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.total {
  color: #666;
  font-size: 14px;
}

.page-btn {
  padding: 6px 14px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-num {
  min-width: 32px;
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.page-num.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.page-ellipsis {
  padding: 0 4px;
  color: #999;
}

.page-size {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
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

.modal-small {
  max-width: 400px;
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

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 15px;
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.tip {
  margin-bottom: 20px;
  color: #666;
}

.tip strong {
  color: #333;
}

/* 角色分配样式 */
.action-perm { background: #e6fffb; color: #13c2c2; }
.action-perm:hover { background: #13c2c2; color: #fff; }

.tip-box {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.tip-box.info {
  background: #e6f7ff;
  border-color: #91d5ff;
}

.tip-box p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.role-list {
  max-height: 400px;
  overflow-y: auto;
}

.role-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
}

.role-item:last-child {
  border-bottom: none;
}

.role-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.role-label {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.role-code {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.role-desc {
  font-size: 13px;
  color: #999;
}

/* 权限分配样式 */
.modal-large {
  max-width: 700px;
}

.permission-tree {
  max-height: 450px;
  overflow-y: auto;
  padding: 10px 0;
}

.perm-item {
  margin-bottom: 16px;
}

.perm-checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.2s;
}

.perm-checkbox:hover {
  background: #f5f5f5;
}

.perm-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.perm-icon {
  font-size: 16px;
  width: 24px;
  text-align: center;
}

.perm-name {
  font-size: 14px;
  color: #333;
}

.perm-children {
  margin-left: 40px;
  margin-top: 8px;
  padding-left: 12px;
  border-left: 2px solid #e8e8e8;
}

.perm-checkbox.sub {
  padding: 6px 12px;
}

.perm-checkbox.sub .perm-name {
  font-size: 13px;
  color: #666;
}

/* 家长弹窗样式 */
.parent-list {
  margin-bottom: 16px;
}

.parent-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  margin-bottom: 10px;
  transition: box-shadow 0.2s;
}

.parent-card:hover {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
}

.parent-card-info {
  flex: 1;
}

.parent-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.relation-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 400;
}

.parent-contacts {
  display: flex;
  gap: 14px;
  font-size: 13px;
  color: #666;
  flex-wrap: wrap;
}

.parent-contacts span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.section-title.mt-16,
.mt-16.section-title {
  margin-top: 20px;
}

.no-parent-tip {
  text-align: center;
  color: #bbb;
  font-size: 14px;
  padding: 20px 0;
  background: #fafafa;
  border-radius: 8px;
  border: 1px dashed #e0e0e0;
  margin-bottom: 16px;
}

.action-parent { background: #fff7e6; color: #fa8c16; }
.action-parent:hover { background: #fa8c16; color: #fff; }

/* 上级选择提示 */
.hint-text {
  color: #999;
  font-weight: normal;
  font-size: 12px;
}

.role-level-tag {
  color: #667eea;
  font-size: 11px;
  margin-left: 4px;
}

/* 权限分配弹窗样式 */
.perm-section {
  margin-bottom: 24px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.perm-section-title {
  background: #f8f9fa;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.section-icon {
  font-size: 16px;
}

.section-badge {
  background: #667eea;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: normal;
}

.button-tree {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding: 12px;
}

.perm-type-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

.perm-type-tag.type-1 {
  background: #e6f7ff;
  color: #1890ff;
}

.perm-type-tag.type-2 {
  background: #f6ffed;
  color: #52c41a;
}

.perm-type-tag.type-3 {
  background: #fff7e6;
  color: #fa8c16;
}

.perm-code {
  font-size: 11px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
  font-family: monospace;
}

.perm-checkbox.is-button {
  background: #fffbf0;
  border: 1px dashed #ffd591;
  border-radius: 6px;
  padding: 8px 12px;
}

.perm-checkbox.is-menu {
  background: #f6ffed;
  border-radius: 6px;
}

.perm-checkbox.is-dir {
  background: #e6f7ff;
  border-radius: 6px;
}

/* 上级用户标签 */
.parent-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f4ff;
  color: #667eea;
  border-radius: 4px;
  font-size: 12px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.no-parent {
  color: #ccc;
  font-size: 13px;
}
</style>
