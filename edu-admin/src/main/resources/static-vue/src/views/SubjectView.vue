<template>
  <div class="subject-page">
    <div class="page-header">
      <h1 class="page-title">学科管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建学科</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input v-model="searchForm.subjectName" type="text" class="search-input" placeholder="学科名称" @keyup.enter="handleSearch">
      <select v-model="searchForm.grade" class="search-select">
        <option value="">全部年级</option>
        <option v-for="g in gradeList" :key="g" :value="g">{{ g }}</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-tip">加载中...</div>

    <!-- 按年级分组展示 -->
    <div v-else>
      <div v-for="(group, grade) in groupedSubjects" :key="grade" class="grade-section">
        <div class="grade-header">
          <span class="grade-label">{{ grade }}</span>
          <span class="grade-count">{{ group.length }} 个学科</span>
        </div>
        <div class="subject-cards">
          <div
            v-for="subject in group"
            :key="subject.id"
            class="subject-card"
            :style="{ borderTopColor: getGradeColor(grade) }"
          >
            <div class="card-icon" :style="{ background: getGradeColor(grade) }">
              {{ subjectIconMap[subject.subjectName] || '📖' }}
            </div>
            <div class="card-body">
              <div class="card-name">{{ subject.subjectName }}</div>
              <div class="card-meta">
                <span class="meta-item">
                  <span class="meta-icon">❓</span>
                  {{ questionCountMap[subject.id] || 0 }} 道题目
                </span>
                <span class="meta-item">
                  <span class="meta-icon">🔢</span>
                  排序 {{ subject.sortOrder }}
                </span>
              </div>
              <span :class="['status-badge', subject.status === 1 ? 'badge-on' : 'badge-off']">
                {{ subject.status === 1 ? '已启用' : '已禁用' }}
              </span>
            </div>
            <div class="card-actions">
              <button class="action-btn action-edit" @click="handleEdit(subject)">编辑</button>
              <button class="action-btn action-delete" @click="handleDelete(subject)">删除</button>
            </div>
          </div>

          <!-- 添加新学科快捷卡片 -->
          <div class="subject-card add-card" @click="openAddModalForGrade(grade)">
            <div class="add-icon">+</div>
            <div class="add-text">添加学科</div>
          </div>
        </div>
      </div>

      <div v-if="Object.keys(groupedSubjects).length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <div class="empty-text">暂无学科数据</div>
        <button class="btn btn-primary" @click="openAddModal">立即添加</button>
      </div>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑学科' : '新建学科' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>学科名称 <span class="required">*</span></label>
              <input v-model="form.subjectName" type="text" placeholder="如：语文、数学、英语" required>
            </div>
            <div class="form-group">
              <label>年级 <span class="required">*</span></label>
              <select v-model="form.grade" required>
                <option v-for="g in gradeList" :key="g" :value="g">{{ g }}</option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>排序</label>
                <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前">
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useMessage } from '@/composables/useMessage'
import { useConfigStore } from '@/stores/config'

const { toast, confirm } = useMessage()
const configStore = useConfigStore()

// 动态年级列表
const gradeList = computed(() => {
  const grades = configStore.gradeList
  return grades.map(g => g.itemName || g.itemLabel)
})

// 学科图标映射
const subjectIconMap = {
  '语文': '📝', '数学': '🔢', '英语': '🔤', '物理': '⚡', '化学': '🧪',
  '生物': '🌱', '历史': '📜', '地理': '🌍', '政治': '⭐', '道德与法治': '⚖️',
  '科学': '🔭', '音乐': '🎵', '美术': '🎨', '体育': '⚽', '信息技术': '💻'
}

// 年级颜色映射（从配置获取）
const gradeColors = computed(() => configStore.gradeColorMap)

function getGradeColor(grade) {
  return gradeColors.value[grade] || '#667eea'
}

const searchForm = reactive({ subjectName: '', grade: '' })
const subjectList = ref([])
const questionCountMap = ref({})
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const loading = ref(false)
const form = reactive({ id: null, subjectName: '', grade: '六年级', sortOrder: 0, status: 1 })

// 按年级分组（保持年级顺序）
const groupedSubjects = computed(() => {
  const filtered = searchForm.subjectName
    ? subjectList.value.filter(s => s.subjectName.includes(searchForm.subjectName))
    : subjectList.value

  const map = {}
  const grades = gradeList.value
  for (const g of grades) {
    const items = filtered.filter(s => s.grade === g)
    if (items.length > 0) map[g] = items
  }
  // 其他年级
  const unknownGrade = filtered.filter(s => !grades.includes(s.grade))
  if (unknownGrade.length > 0) map['其他'] = unknownGrade

  return map
})

async function fetchSubjectList() {
  loading.value = true
  try {
    const res = await request.get('/exam/subject/list', {
      params: { grade: searchForm.grade || undefined }
    })
    if (res.data.code === 200) {
      subjectList.value = res.data.data || []
    }
    // 获取题目数量统计
    try {
      const countRes = await request.get('/exam/subject/question-count')
      if (countRes.data.code === 200) {
        questionCountMap.value = countRes.data.data || {}
      }
    } catch (e) { /* ignore */ }
  } catch (e) {
    console.error('获取学科列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { fetchSubjectList() }
function resetSearch() { searchForm.subjectName = ''; searchForm.grade = ''; fetchSubjectList() }

function openAddModal() {
  isEdit.value = false
  Object.assign(form, { id: null, subjectName: '', grade: '六年级', sortOrder: 0, status: 1 })
  showModal.value = true
}

function openAddModalForGrade(grade) {
  isEdit.value = false
  Object.assign(form, { id: null, subjectName: '', grade, sortOrder: 0, status: 1 })
  showModal.value = true
}

function handleEdit(subject) {
  isEdit.value = true
  Object.assign(form, { ...subject })
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/exam/subject', form)
      toast.success('修改成功')
    } else {
      await request.post('/exam/subject', form)
      toast.success('新增成功')
    }
    closeModal()
    fetchSubjectList()
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(subject) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除学科 <b>"${subject.subjectName}"</b> 吗？`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return
  try {
    await request.delete(`/exam/subject/${subject.id}`)
    toast.success('删除成功')
    fetchSubjectList()
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

onMounted(async () => {
  await configStore.loadConfigs()
  fetchSubjectList()
})
</script>

<style scoped>
.subject-page { padding: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.page-title { font-size: 28px; color: #333; margin: 0; }

.btn { padding: 10px 25px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; transition: all 0.2s; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(102,126,234,0.4); }
.btn-default { background: #f0f0f0; color: #666; }

.search-bar { display: flex; gap: 12px; margin-bottom: 28px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); flex-wrap: wrap; }
.search-input, .search-select { padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; }
.search-input { width: 200px; }

.loading-tip { text-align: center; color: #999; padding: 50px; font-size: 16px; }

/* 年级分组 */
.grade-section { margin-bottom: 32px; }

.grade-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.grade-label {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  position: relative;
  padding-left: 14px;
}

.grade-label::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.grade-count {
  font-size: 13px;
  color: #999;
  background: #f5f5f5;
  padding: 3px 10px;
  border-radius: 20px;
}

/* 学科卡片网格 */
.subject-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.subject-card {
  background: white;
  border-radius: 14px;
  border-top: 4px solid #667eea;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  padding: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.subject-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
  opacity: 0.9;
}

.card-name {
  font-size: 17px;
  font-weight: 600;
  color: #222;
  margin-bottom: 10px;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 10px;
}

.meta-item {
  font-size: 13px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
}
.badge-on { background: #f6ffed; color: #52c41a; }
.badge-off { background: #fff1f0; color: #ff4d4f; }

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 添加卡片 */
.add-card {
  border-top-color: #e0e0e0 !important;
  border: 2px dashed #ddd;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  min-height: 160px;
}

.add-card:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.add-icon {
  font-size: 32px;
  color: #ccc;
  line-height: 1;
  margin-bottom: 8px;
  transition: color 0.2s;
}

.add-card:hover .add-icon { color: #667eea; }

.add-text {
  font-size: 14px;
  color: #bbb;
  transition: color 0.2s;
}

.add-card:hover .add-text { color: #667eea; }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.empty-icon { font-size: 60px; margin-bottom: 16px; }
.empty-text { color: #999; font-size: 16px; margin-bottom: 20px; }

/* 操作按钮 */
.action-btn { padding: 4px 12px; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 16px; width: 90%; max-width: 480px; max-height: 90vh; overflow: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 25px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; color: #333; }
.required { color: #ff4d4f; }
.form-group input, .form-group select { width: 100%; padding: 10px 15px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-group input:focus, .form-group select:focus { border-color: #667eea; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 10px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
</style>
