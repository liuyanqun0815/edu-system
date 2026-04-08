import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  const menuIds = ref([])

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    refreshToken.value = data.refreshToken
    userId.value = data.userId
    username.value = data.username
    nickname.value = data.nickname || ''
    avatar.value = data.avatar || ''
    localStorage.setItem('token', data.token)
    if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    if (data.nickname) localStorage.setItem('nickname', data.nickname)
    if (data.avatar) localStorage.setItem('avatar', data.avatar)
  }

  function setUserInfo(data) {
    if (data.nickname) {
      nickname.value = data.nickname
      localStorage.setItem('nickname', data.nickname)
    }
    if (data.avatar) {
      avatar.value = data.avatar
      localStorage.setItem('avatar', data.avatar)
    }
  }

  function setMenuIds(ids) {
    menuIds.value = ids
    localStorage.setItem('userMenuIds', JSON.stringify(ids))
  }

  function loadMenuIds() {
    const cached = localStorage.getItem('userMenuIds')
    if (cached) {
      try {
        menuIds.value = JSON.parse(cached)
      } catch (e) {
        menuIds.value = []
      }
    }
  }

  async function fetchUserMenus() {
    if (!userId.value) return
    try {
      const response = await request.get(`/system/user/${userId.value}/menus/final`)
      if (response.data.code === 200 && response.data.data) {
        setMenuIds(response.data.data)
      }
    } catch (e) {
      console.warn('获取菜单权限失败', e)
    }
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const response = await request.get('/system/user/profile')
      if (response.data.code === 200 && response.data.data) {
        setUserInfo(response.data.data)
      }
    } catch (e) {
      console.warn('获取用户信息失败', e)
    }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userId.value = ''
    username.value = ''
    nickname.value = ''
    avatar.value = ''
    menuIds.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
    localStorage.removeItem('avatar')
    localStorage.removeItem('userMenuIds')
  }

  return {
    token,
    refreshToken,
    userId,
    username,
    nickname,
    avatar,
    menuIds,
    isLoggedIn,
    setAuth,
    setUserInfo,
    setMenuIds,
    loadMenuIds,
    fetchUserMenus,
    fetchUserInfo,
    logout
  }
})
