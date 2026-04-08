<template>
  <Teleport to="body">
    <Transition name="confirm-fade">
      <div v-if="visible" class="confirm-overlay" @click.self="handleCancel">
        <div class="confirm-modal">
          <div class="confirm-icon" :class="typeClass">
            {{ icon }}
          </div>
          <div class="confirm-content">
            <div class="confirm-title">{{ title }}</div>
            <div class="confirm-message" v-html="message"></div>
          </div>
          <div class="confirm-actions">
            <button class="btn btn-default" @click="handleCancel">
              {{ cancelText }}
            </button>
            <button class="btn" :class="confirmBtnClass" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'

const visible = ref(false)
const title = ref('提示')
const message = ref('')
const type = ref('warning') // warning, danger, info, success
const confirmText = ref('确定')
const cancelText = ref('取消')

let resolvePromise = null

const icon = computed(() => {
  const icons = { warning: '⚠', danger: '🗑', info: 'ℹ', success: '✓' }
  return icons[type.value] || '⚠'
})

const typeClass = computed(() => `type-${type.value}`)
const confirmBtnClass = computed(() => type.value === 'danger' ? 'btn-danger' : 'btn-primary')

function show(options) {
  title.value = options.title || '提示'
  message.value = options.message || ''
  type.value = options.type || 'warning'
  confirmText.value = options.confirmText || '确定'
  cancelText.value = options.cancelText || '取消'
  visible.value = true
  
  return new Promise((resolve) => {
    resolvePromise = resolve
  })
}

function handleConfirm() {
  visible.value = false
  if (resolvePromise) resolvePromise(true)
}

function handleCancel() {
  visible.value = false
  if (resolvePromise) resolvePromise(false)
}

defineExpose({ show })
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(2px);
}

.confirm-modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  padding: 28px 24px 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 16px;
}

.type-warning { background: #fffbe6; color: #fa8c16; }
.type-danger { background: #fff1f0; color: #ff4d4f; }
.type-info { background: #e6f7ff; color: #1890ff; }
.type-success { background: #f6ffed; color: #52c41a; }

.confirm-content { margin-bottom: 24px; }
.confirm-title { font-size: 18px; font-weight: 600; color: #222; margin-bottom: 8px; }
.confirm-message { font-size: 14px; color: #666; line-height: 1.6; }

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn {
  padding: 10px 28px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 90px;
}

.btn-default {
  background: #f5f5f5;
  color: #666;
}
.btn-default:hover { background: #e8e8e8; }

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4); }

.btn-danger {
  background: #ff4d4f;
  color: white;
}
.btn-danger:hover { background: #ff7875; }

/* 动画 */
.confirm-fade-enter-active { animation: confirmIn 0.25s ease; }
.confirm-fade-leave-active { animation: confirmOut 0.2s ease; }

@keyframes confirmIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes confirmOut {
  from { opacity: 1; }
  to { opacity: 0; }
}
</style>
