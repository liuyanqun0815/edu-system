/**
 * usePermission —— 按钮级权限控制 composable
 *
 * 用法：
 *   const { hasPermission, permissions } = usePermission()
 *   v-if="hasPermission('user:assign:role')"
 */
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

// 全局单例缓存，避免多个组件重复请求
let _permissions = null
let _loading = false
const _listeners = []

async function _load() {
  if (_permissions !== null) return
  if (_loading) {
    return new Promise(resolve => _listeners.push(resolve))
  }
  _loading = true
  try {
    const res = await request.get('/system/menu/permissions')
    if (res.data.code === 200) {
      _permissions = new Set(res.data.data || [])
    } else {
      _permissions = new Set()
    }
  } catch {
    _permissions = new Set()
  } finally {
    _loading = false
    _listeners.forEach(fn => fn())
    _listeners.length = 0
  }
}

export function usePermission() {
  const permissions = ref(_permissions ? new Set(_permissions) : new Set())
  const loaded = ref(_permissions !== null)

  onMounted(async () => {
    if (_permissions === null) {
      await _load()
    }
    permissions.value = new Set(_permissions)
    loaded.value = true
  })

  function hasPermission(perm) {
    if (!loaded.value) return false
    // 超级管理员判断：拥有 '*' 权限时放行一切
    if (permissions.value.has('*')) return true
    return permissions.value.has(perm)
  }

  /** 重置缓存（退出登录时调用） */
  function clearPermissionCache() {
    _permissions = null
  }

  return { permissions, hasPermission, loaded, clearPermissionCache }
}
