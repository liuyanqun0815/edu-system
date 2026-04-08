<template>
  <aside class="sidebar">
    <nav class="menu">
      <router-link
        v-for="menu in visibleMenus"
        :key="menu.id"
        :to="menu.path"
        class="menu-item"
        active-class="active"
      >
        <span class="icon">{{ menu.icon }}</span>
        <span class="label">{{ menu.label }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 菜单ID与数据库sys_menu表保持一致
// 顶级菜单(parent_id=0)
const allMenus = [
  { id: 1, path: '/dashboard', icon: '📊', label: '工作台' },
  { id: 2, path: '/course', icon: '📚', label: '课程管理' },
  { id: 3, path: '/exam', icon: '📝', label: '考试管理' },
  { id: 4, path: '/question', icon: '❓', label: '题库管理' },
  { id: 5, path: '/subject', icon: '📖', label: '学科管理' },
  { id: 6, path: '/word', icon: '🔤', label: '单词训练' },
  { id: 7, path: '/user', icon: '👥', label: '用户管理' },
  { id: 8, path: '/role', icon: '🔑', label: '角色管理' },
  { id: 9, path: '/menu', icon: '📋', label: '菜单管理' },
  { id: 10, path: '/evaluation', icon: '💬', label: '评价管理' },
  { id: 11, path: '/stats', icon: '📈', label: '数据统计' },
  { id: 12, path: '/setting', icon: '⚙️', label: '系统设置' },
  { id: 13, path: '/paper-upload', icon: '📄', label: '试卷上传' },
  { id: 14, path: '/online-exam', icon: '✏️', label: '在线考试' },
  { id: 15, path: '/config', icon: '🔧', label: '配置管理' },
  // 活动管理在数据库中是子菜单(id=105, parent_id=100)，这里暂时作为顶级菜单展示
  { id: 105, path: '/activity', icon: '📢', label: '活动管理' },
  // 登录日志需要新增到数据库
  { id: 16, path: '/login-log', icon: '📋', label: '登录日志' },
]

const visibleMenus = computed(() => {
  // 如果 menuIds 为空或未加载，显示所有菜单
  // 只有当 menuIds 有值且不为空数组时才过滤
  if (!userStore.menuIds || userStore.menuIds.length === 0) {
    return allMenus
  }
  const filtered = allMenus.filter(menu => userStore.menuIds.includes(menu.id))
  // 如果过滤后为空，返回所有菜单（避免用户无权限时看不到任何菜单）
  return filtered.length > 0 ? filtered : allMenus
})


</script>

<style scoped>
.sidebar {
  width: 250px;
  background: white;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  padding: 20px 0;
  flex-shrink: 0;
  overflow-y: auto;
  position: fixed;
  left: 0;
  top: 64px;
  height: calc(100vh - 64px);
  z-index: 100;
}

.menu {
  display: flex;
  flex-direction: column;
}

.menu-item {
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  text-decoration: none;
  border-left: 3px solid transparent;
}

.menu-item:hover,
.menu-item.active {
  background: linear-gradient(90deg, #667eea20, transparent);
  color: #667eea;
  border-left-color: #667eea;
}

.icon {
  font-size: 18px;
}

.label {
  font-size: 14px;
}
</style>
