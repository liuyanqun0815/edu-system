<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="item in toasts"
          :key="item.id"
          :class="['toast-item', `toast-${item.type}`]"
        >
          <span class="toast-icon">{{ icons[item.type] }}</span>
          <div class="toast-body">
            <div v-if="item.title" class="toast-title">{{ item.title }}</div>
            <div class="toast-msg">{{ item.message }}</div>
          </div>
          <span class="toast-close" @click="removeToast(item.id)">&times;</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useMessageStore } from '@/stores/message'
import { storeToRefs } from 'pinia'

const messageStore = useMessageStore()
const { toasts } = storeToRefs(messageStore)
const { removeToast } = messageStore

const icons = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ'
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  pointer-events: all;
  min-width: 280px;
  max-width: 400px;
  padding: 14px 18px 14px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.14);
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border-left: 4px solid #667eea;
}

.toast-success { border-left-color: #52c41a; }
.toast-error { border-left-color: #ff4d4f; }
.toast-warning { border-left-color: #fa8c16; }
.toast-info { border-left-color: #1890ff; }

.toast-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.toast-success .toast-icon { background: #f6ffed; color: #52c41a; }
.toast-error .toast-icon { background: #fff1f0; color: #ff4d4f; }
.toast-warning .toast-icon { background: #fffbe6; color: #fa8c16; }
.toast-info .toast-icon { background: #e6f7ff; color: #1890ff; }

.toast-body { flex: 1; }
.toast-title { font-size: 14px; font-weight: 600; color: #222; line-height: 1.4; }
.toast-msg { font-size: 13px; color: #666; margin-top: 3px; line-height: 1.5; }

.toast-close {
  font-size: 18px;
  color: #bbb;
  cursor: pointer;
  flex-shrink: 0;
  line-height: 1;
  transition: color 0.2s;
}
.toast-close:hover { color: #555; }

/* 动画 */
.toast-enter-active { animation: toastIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); }
.toast-leave-active { animation: toastOut 0.3s ease forwards; }

@keyframes toastIn {
  from { opacity: 0; transform: translateX(120%); }
  to { opacity: 1; transform: translateX(0); }
}
@keyframes toastOut {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(120%); }
}
</style>
