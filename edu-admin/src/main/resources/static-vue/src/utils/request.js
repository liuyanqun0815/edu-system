import axios from 'axios'
import router from '@/router'

const request = axios.create({
  timeout: 30000
})

// 是否正在刷新Token
let isRefreshing = false
// 重试队列
let requests = []

// 请求拦截器:自动附加 Authorization 和 X-User-Id
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      // 检查token是否已过期(JWT payload中的exp字段)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        const exp = payload.exp * 1000 // 转换为毫秒
        if (Date.now() >= exp) {
          // Token已过期,清除本地存储
          localStorage.clear()
          router.push('/login')
          return Promise.reject(new Error('Token已过期'))
        }
      } catch (e) {
        // 解析失败,清除token
        localStorage.removeItem('token')
      }
      config.headers['Authorization'] = 'Bearer ' + token
    }
    const userId = localStorage.getItem('userId')
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 401 和 Token 刷新
request.interceptors.response.use(
  async (response) => response,
  async (error) => {
    const originalRequest = error.config
    
    // 如果是401且未刷新过
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 如果正在刷新，将请求加入队列
        return new Promise((resolve) => {
          requests.push((token) => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token
            resolve(request(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          throw new Error('No refresh token')
        }

        // 调用刷新接口
        const res = await axios.post('/auth/refresh-token', null, {
          params: { refreshToken }
        })

        if (res.data.code === 200) {
          const { token, refreshToken: newRefreshToken } = res.data.data
          
          // 保存新Token
          localStorage.setItem('token', token)
          localStorage.setItem('refreshToken', newRefreshToken)

          // 重试队列中的请求
          requests.forEach(cb => cb(token))
          requests = []

          // 重试原请求
          originalRequest.headers['Authorization'] = 'Bearer ' + token
          return request(originalRequest)
        } else {
          throw new Error('Refresh failed')
        }
      } catch (e) {
        // 刷新失败，清除登录状态并跳转登录页
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('avatar')
        localStorage.removeItem('userMenuIds')
        router.push('/login')
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default request
