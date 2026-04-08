import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMessageStore = defineStore('message', () => {
  const toasts = ref([])
  let toastId = 0

  function addToast(message, type = 'info', title = '', duration = 3000) {
    const id = ++toastId
    toasts.value.push({ id, message, type, title })
    
    if (duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }
    return id
  }

  function removeToast(id) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index > -1) toasts.value.splice(index, 1)
  }

  function success(message, title = '') {
    return addToast(message, 'success', title)
  }

  function error(message, title = '') {
    return addToast(message, 'error', title, 4000)
  }

  function warning(message, title = '') {
    return addToast(message, 'warning', title)
  }

  function info(message, title = '') {
    return addToast(message, 'info', title)
  }

  return {
    toasts,
    addToast,
    removeToast,
    success,
    error,
    warning,
    info
  }
})
