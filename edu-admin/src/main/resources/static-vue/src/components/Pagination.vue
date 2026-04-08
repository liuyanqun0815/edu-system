<template>
  <div class="pagination-wrapper">
    <div class="pagination-left">
      <div class="pagination-info">
        共 <span class="highlight">{{ total }}</span> 条记录，第 <span class="highlight">{{ currentPage }}</span>/<span class="highlight">{{ totalPages }}</span> 页
      </div>
    </div>
    
    <div class="pagination-center">
      <div class="pagination-controls">
        <button 
          class="page-btn nav-btn first" 
          :disabled="currentPage <= 1" 
          @click="$emit('update:currentPage', 1)"
          title="首页"
        >
          «
        </button>
        <button 
          class="page-btn nav-btn prev" 
          :disabled="currentPage <= 1" 
          @click="$emit('update:currentPage', currentPage - 1)"
        >
          <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
            <path d="M8 2L4 6l4 4" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          上一页
        </button>
        
        <div class="page-numbers">
          <template v-for="(p, idx) in displayPages" :key="idx">
            <button 
              v-if="p !== '...'"
              :class="['page-num', { active: p === currentPage }]"
              @click="$emit('update:currentPage', p)"
            >{{ p }}</button>
            <span v-else class="ellipsis">…</span>
          </template>
        </div>
        
        <button 
          class="page-btn nav-btn next" 
          :disabled="currentPage >= totalPages" 
          @click="$emit('update:currentPage', currentPage + 1)"
        >
          下一页
          <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
            <path d="M4 2l4 4-4 4" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
        </button>
        <button 
          class="page-btn nav-btn last" 
          :disabled="currentPage >= totalPages" 
          @click="$emit('update:currentPage', totalPages)"
          title="末页"
        >
          »
        </button>
      </div>
    </div>
    
    <div class="pagination-right">
      <div class="page-size-selector">
        <select :value="pageSize" @change="$emit('update:pageSize', Number($event.target.value))">
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
          <option :value="50">50条/页</option>
          <option :value="100">100条/页</option>
        </select>
      </div>
      
      <div class="quick-jump">
        <span>跳至</span>
        <input 
          type="number" 
          :value="jumpPage"
          @input="jumpPage = $event.target.value"
          @keyup.enter="handleJump"
          min="1"
          :max="totalPages"
        >
        <span>页</span>
        <button class="jump-btn" @click="handleJump">GO</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize'])

const jumpPage = ref('')

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const displayPages = computed(() => {
  const total = totalPages.value
  const current = props.currentPage
  
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  
  const pages = [1]
  
  if (current > 3) pages.push('...')
  
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  if (current < total - 2) pages.push('...')
  pages.push(total)
  
  return pages
})

function handleJump() {
  const page = parseInt(jumpPage.value)
  if (page >= 1 && page <= totalPages.value) {
    emit('update:currentPage', page)
    jumpPage.value = ''
  }
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  border-radius: 0 0 12px 12px;
  flex-wrap: wrap;
  gap: 16px;
}

.pagination-left, .pagination-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pagination-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.pagination-info {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.pagination-info .highlight {
  color: #667eea;
  font-weight: 600;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #e8e8e8;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.15);
}

.page-btn:active:not(:disabled) {
  transform: translateY(0);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.nav-btn.first, .nav-btn.last {
  padding: 6px 10px;
  font-size: 14px;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 8px;
}

.page-num {
  min-width: 32px;
  height: 32px;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.page-num:hover {
  background: #f5f7ff;
  color: #667eea;
}

.page-num.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.35);
  transform: scale(1.05);
}

.page-num.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2px;
  background: rgba(255,255,255,0.5);
  border-radius: 1px;
}

.ellipsis {
  padding: 0 6px;
  color: #999;
  font-size: 14px;
}

.page-size-selector select {
  padding: 7px 28px 7px 10px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L2 4h8z'/%3E%3C/svg%3E") no-repeat right 8px center;
  cursor: pointer;
  outline: none;
  appearance: none;
  transition: all 0.2s;
}

.page-size-selector select:hover {
  border-color: #667eea;
}

.page-size-selector select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.quick-jump {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.quick-jump input {
  width: 50px;
  padding: 6px 8px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 13px;
  text-align: center;
  outline: none;
  transition: all 0.2s;
}

.quick-jump input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.quick-jump input::-webkit-inner-spin-button,
.quick-jump input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.jump-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
}

.jump-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.35);
}

.jump-btn:active {
  transform: translateY(0);
}

@media (max-width: 900px) {
  .pagination-wrapper {
    flex-direction: column;
    gap: 12px;
  }
  .pagination-left, .pagination-center, .pagination-right {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 600px) {
  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
  .page-numbers {
    order: -1;
    width: 100%;
    justify-content: center;
    margin: 8px 0;
  }
  .quick-jump {
    display: none;
  }
}
</style>
