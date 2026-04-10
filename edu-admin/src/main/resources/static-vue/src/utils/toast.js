/**
 * Toast 消息提示工具
 * 基于 Element Plus 的 ElMessage
 */
import { ElMessage } from 'element-plus'

const toast = {
  /**
   * 成功提示
   */
  success(message, duration = 3000) {
    ElMessage({
      message,
      type: 'success',
      duration,
      showClose: true
    })
  },

  /**
   * 警告提示
   */
  warning(message, duration = 3000) {
    ElMessage({
      message,
      type: 'warning',
      duration,
      showClose: true
    })
  },

  /**
   * 错误提示
   */
  error(message, duration = 5000) {
    ElMessage({
      message,
      type: 'error',
      duration,
      showClose: true
    })
  },

  /**
   * 信息提示
   */
  info(message, duration = 3000) {
    ElMessage({
      message,
      type: 'info',
      duration,
      showClose: true
    })
  },

  /**
   * 加载提示
   */
  loading(message = '加载中...') {
    return ElMessage({
      message,
      type: 'info',
      duration: 0, // 不自动关闭
      icon: 'Loading'
    })
  }
}

export default toast
