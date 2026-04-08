import { inject } from 'vue'
import { useMessageStore } from '@/stores/message'

/**
 * 全局消息提示 composable
 * 使用方式：
 * const { toast, confirm } = useMessage()
 * toast.success('操作成功')
 * await confirm({ message: '确定删除吗？', type: 'danger' })
 */
export function useMessage() {
  const messageStore = useMessageStore()
  const confirm = inject('confirm', () => Promise.resolve(false))

  const toast = {
    success: (msg, title) => messageStore.success(msg, title),
    error: (msg, title) => messageStore.error(msg, title),
    warning: (msg, title) => messageStore.warning(msg, title),
    info: (msg, title) => messageStore.info(msg, title)
  }

  return { toast, confirm }
}
