<template>
  <div class="course-page">
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <button class="btn btn-primary" @click="openAddModal">+ 新建课程</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchForm.courseName"
        type="text"
        class="search-input"
        placeholder="课程名称"
        @keyup.enter="handleSearch"
      >
      <select v-model="searchForm.categoryId" class="search-select">
        <option value="">全部分类</option>
        <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
          {{ cat.categoryName }}
        </option>
      </select>
      <select v-model="searchForm.status" class="search-select">
        <option value="">全部状态</option>
        <option :value="0">草稿</option>
        <option :value="1">已发布</option>
        <option :value="2">已下架</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">查询</button>
      <button class="btn btn-default" @click="resetSearch">重置</button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>封面</th>
            <th>课程名称</th>
            <th>分类</th>
            <th>难度</th>
            <th>课时</th>
            <th>价格</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in courseList" :key="course.id">
            <td>{{ course.id }}</td>
            <td>
              <img
                :src="course.coverImage || defaultCover"
                class="course-cover"
                alt="封面"
              >
            </td>
            <td>{{ course.courseName }}</td>
            <td>{{ getCategoryName(course.categoryId) }}</td>
            <td>
              <span :class="['difficulty-tag', `difficulty-${course.difficulty}`]">
                {{ difficultyMap[course.difficulty] || '未知' }}
              </span>
            </td>
            <td>{{ course.courseHours }}课时</td>
            <td>¥{{ course.price || 0 }}</td>
            <td>
              <span :class="['status-tag', `status-${course.status}`]">
                {{ statusMap[course.status] }}
              </span>
            </td>
            <td>{{ formatDate(course.createTime) }}</td>
            <td class="actions">
              <button class="action-btn action-edit" @click="handleEdit(course)">编辑</button>
              <button class="action-btn action-warn" @click="openChapterModal(course)">章节</button>
              <button class="action-btn action-delete" @click="handleDelete(course)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-left">
          <span class="total">共 <em>{{ total }}</em> 条</span>
        </div>
        <div class="pagination-center">
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(1)" title="首页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/></svg>
          </button>
          <button :disabled="pageNum <= 1" class="page-btn" @click="handlePageChange(pageNum - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
            <span>上一页</span>
          </button>
          <span class="page-info"><strong>{{ pageNum }}</strong> / {{ totalPages }}</span>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(pageNum + 1)">
            <span>下一页</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
          </button>
          <button :disabled="pageNum >= totalPages" class="page-btn" @click="handlePageChange(totalPages)" title="末页">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 17l5-5-5-5M6 17l5-5-5-5"/></svg>
          </button>
        </div>
        <div class="pagination-right">
          <select v-model="pageSize" class="page-size" @change="handleSizeChange">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑课程' : '新建课程' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>课程名称 <span class="required">*</span></label>
                <input v-model="form.courseName" type="text" placeholder="请输入课程名称" required>
              </div>
              <div class="form-group">
                <label>课程编码</label>
                <input v-model="form.courseCode" type="text" placeholder="请输入课程编码">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>课程分类 <span class="required">*</span></label>
                <select v-model="form.categoryId" required>
                  <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
                    {{ cat.categoryName }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>难度等级</label>
                <select v-model="form.difficulty">
                  <option :value="1">初级</option>
                  <option :value="2">中级</option>
                  <option :value="3">高级</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>课时数</label>
                <input v-model.number="form.courseHours" type="number" placeholder="请输入课时数">
              </div>
              <div class="form-group">
                <label>价格（元）</label>
                <input v-model.number="form.price" type="number" step="0.01" placeholder="请输入价格">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>状态</label>
                <select v-model="form.status">
                  <option :value="0">草稿</option>
                  <option :value="1">已发布</option>
                  <option :value="2">已下架</option>
                </select>
              </div>
              <div class="form-group">
                <label>课程封面</label>
                <div class="cover-upload">
                  <img v-if="form.coverImage" :src="form.coverImage" class="preview-cover">
                  <div v-else class="cover-placeholder">暂无封面</div>
                  <input type="file" accept="image/*" @change="handleCoverUpload">
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>课程简介</label>
              <textarea v-model="form.description" rows="2" placeholder="请输入课程简介"></textarea>
            </div>
            <div class="form-group">
              <label>课程详情</label>
              <textarea v-model="form.detail" rows="4" placeholder="请输入课程详情"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 章节管理弹窗 -->
    <div v-if="showChapterModal" class="modal-overlay" @click.self="closeChapterModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>章节管理 - {{ currentCourse?.courseName }}</h3>
          <button class="close-btn" @click="closeChapterModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="chapter-header">
            <button class="btn btn-primary" @click="openAddChapterModal">+ 添加章节</button>
          </div>
          <div class="chapter-list">
            <div
              v-for="(chapter, index) in chapterList"
              :key="chapter.id"
              class="chapter-item"
            >
              <div class="chapter-info">
                <span class="chapter-index">第{{ index + 1 }}章</span>
                <span class="chapter-title">{{ chapter.chapterName }}</span>
                <span v-if="chapter.videoDuration" class="chapter-duration">
                  {{ Math.floor(chapter.videoDuration / 60) }}分钟
                </span>
              </div>
              <div class="chapter-actions">
                <button class="action-btn action-edit" @click="handleEditChapter(chapter)">编辑</button>
                <button class="action-btn action-delete" @click="handleDeleteChapter(chapter)">删除</button>
              </div>
            </div>
            <div v-if="chapterList.length === 0" class="empty-tip">
              暂无章节，点击上方按钮添加
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 章节编辑弹窗 -->
    <div v-if="showChapterEditModal" class="modal-overlay" @click.self="closeChapterEditModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEditChapter ? '编辑章节' : '添加章节' }}</h3>
          <button class="close-btn" @click="closeChapterEditModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleChapterSubmit">
            <div class="form-group">
              <label>章节名称 <span class="required">*</span></label>
              <input v-model="chapterForm.chapterName" type="text" placeholder="请输入章节名称" required>
            </div>
            <div class="form-group">
              <label>视频时长（分钟）</label>
              <input v-model.number="chapterForm.videoDuration" type="number" placeholder="请输入视频时长">
            </div>
            <div class="form-group">
              <label>排序</label>
              <input v-model.number="chapterForm.sortOrder" type="number" placeholder="数字越小越靠前">
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="chapterForm.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-default" @click="closeChapterEditModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
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

const { toast, confirm } = useMessage()

const defaultCover = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjZjBmMmZmIi8+PHRleHQgeD0iNDAlIiB5PSI1NSUiIGZvbnQtc2l6ZT0iMjAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM5OWFiZWQiPvCfk5o8L3RleHQ+PC9zdmc+'

const difficultyMap = { 1: '初级', 2: '中级', 3: '高级' }
const statusMap = { 0: '草稿', 1: '已发布', 2: '已下架' }

// 搜索表单
const searchForm = reactive({
  courseName: '',
  categoryId: '',
  status: ''
})

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

// 数据列表
const courseList = ref([])
const categoryList = ref([])
const chapterList = ref([])

// 弹窗控制
const showModal = ref(false)
const showChapterModal = ref(false)
const showChapterEditModal = ref(false)
const isEdit = ref(false)
const isEditChapter = ref(false)
const submitting = ref(false)
const currentCourse = ref(null)

// 表单数据
const form = reactive({
  id: null,
  courseName: '',
  courseCode: '',
  categoryId: null,
  teacherId: null,
  coverImage: '',
  description: '',
  detail: '',
  difficulty: 1,
  courseHours: 0,
  price: 0,
  status: 0
})

const chapterForm = reactive({
  id: null,
  courseId: null,
  chapterName: '',
  videoDuration: 0,
  sortOrder: 0,
  status: 1
})

// 获取分类名称
function getCategoryName(categoryId) {
  const cat = categoryList.value.find(c => c.id === categoryId)
  return cat ? cat.categoryName : '-'
}

// 获取课程列表
async function fetchCourseList() {
  try {
    const res = await request.get('/course/course/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        courseName: searchForm.courseName || undefined,
        categoryId: searchForm.categoryId || undefined,
        status: searchForm.status !== '' ? searchForm.status : undefined
      }
    })
    if (res.data.code === 200) {
      courseList.value = res.data.data.rows || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('获取课程列表失败', e)
    toast.error('获取课程列表失败')
  }
}

// 获取分类列表
async function fetchCategoryList() {
  try {
    const res = await request.get('/course/category/list')
    if (res.data.code === 200) {
      categoryList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取分类列表失败', e)
  }
}

// 获取章节列表
async function fetchChapterList(courseId) {
  try {
    const res = await request.get('/course/chapter/list', { params: { courseId } })
    if (res.data.code === 200) {
      chapterList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取章节列表失败', e)
  }
}

// 搜索
function handleSearch() {
  pageNum.value = 1
  fetchCourseList()
}

// 重置搜索
function resetSearch() {
  searchForm.courseName = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

// 分页
function handlePageChange(newPage) {
  pageNum.value = newPage
  fetchCourseList()
}

function handleSizeChange() {
  pageNum.value = 1
  fetchCourseList()
}

// 打开新增弹窗
function openAddModal() {
  isEdit.value = false
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
function handleEdit(course) {
  isEdit.value = true
  Object.assign(form, {
    id: course.id,
    courseName: course.courseName,
    courseCode: course.courseCode || '',
    categoryId: course.categoryId,
    coverImage: course.coverImage || '',
    description: course.description || '',
    detail: course.detail || '',
    difficulty: course.difficulty || 1,
    courseHours: course.courseHours || 0,
    price: course.price || 0,
    status: course.status
  })
  showModal.value = true
}

// 关闭弹窗
function closeModal() {
  showModal.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  Object.assign(form, {
    id: null,
    courseName: '',
    courseCode: '',
    categoryId: null,
    coverImage: '',
    description: '',
    detail: '',
    difficulty: 1,
    courseHours: 0,
    price: 0,
    status: 0
  })
}

// 封面上传
async function handleCoverUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64 = e.target.result
    try {
      const res = await request.post('/system/user/upload-avatar', { image: base64 })
      if (res.data.code === 200) {
        form.coverImage = res.data.data.url
      }
    } catch (err) {
      toast.error('封面上传失败')
    }
  }
  reader.readAsDataURL(file)
}

// 提交表单
async function handleSubmit() {
  submitting.value = true
  try {
    const data = { ...form }

    if (isEdit.value) {
      const res = await request.put('/course/course', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeModal()
        fetchCourseList()
      } else {
        toast.error(res.data.msg || '修改失败')
      }
    } else {
      const res = await request.post('/course/course', data)
      if (res.data.code === 200) {
        toast.success('新增成功')
        closeModal()
        fetchCourseList()
      } else {
        toast.error(res.data.msg || '新增失败')
      }
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除课程
async function handleDelete(course) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除课程 <b>"${course.courseName}"</b> 吗？<br><small style="color:#999">此操作不可恢复！</small>`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/course/course/${course.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchCourseList()
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 打开章节管理弹窗
async function openChapterModal(course) {
  currentCourse.value = course
  await fetchChapterList(course.id)
  showChapterModal.value = true
}

// 关闭章节弹窗
function closeChapterModal() {
  showChapterModal.value = false
  currentCourse.value = null
  chapterList.value = []
}

// 打开添加章节弹窗
function openAddChapterModal() {
  isEditChapter.value = false
  chapterForm.id = null
  chapterForm.courseId = currentCourse.value.id
  chapterForm.chapterName = ''
  chapterForm.videoDuration = 0
  chapterForm.sortOrder = chapterList.value.length + 1
  chapterForm.status = 1
  showChapterEditModal.value = true
}

// 编辑章节
function handleEditChapter(chapter) {
  isEditChapter.value = true
  Object.assign(chapterForm, {
    id: chapter.id,
    courseId: chapter.courseId,
    chapterName: chapter.chapterName,
    videoDuration: chapter.videoDuration || 0,
    sortOrder: chapter.sortOrder || 0,
    status: chapter.status
  })
  showChapterEditModal.value = true
}

// 关闭章节编辑弹窗
function closeChapterEditModal() {
  showChapterEditModal.value = false
}

// 提交章节
async function handleChapterSubmit() {
  submitting.value = true
  try {
    const data = { ...chapterForm }

    if (isEditChapter.value) {
      const res = await request.put('/course/chapter', data)
      if (res.data.code === 200) {
        toast.success('修改成功')
        closeChapterEditModal()
        fetchChapterList(currentCourse.value.id)
      }
    } else {
      const res = await request.post('/course/chapter', data)
      if (res.data.code === 200) {
        toast.success('添加成功')
        closeChapterEditModal()
        fetchChapterList(currentCourse.value.id)
      }
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除章节
async function handleDeleteChapter(chapter) {
  const confirmed = await confirm({
    title: '删除确认',
    message: `确定要删除章节 <b>"${chapter.chapterName}"</b> 吗？`,
    type: 'danger',
    confirmText: '删除'
  })
  if (!confirmed) return

  try {
    const res = await request.delete(`/course/chapter/${chapter.id}`)
    if (res.data.code === 200) {
      toast.success('删除成功')
      fetchChapterList(currentCourse.value.id)
    }
  } catch (e) {
    toast.error(e.response?.data?.msg || '删除失败')
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(() => {
  fetchCourseList()
  fetchCategoryList()
})
</script>

<style scoped>
.course-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.btn {
  padding: 10px 25px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-default {
  background: #f0f0f0;
  color: #666;
}

.btn-default:hover {
  background: #e0e0e0;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.search-input,
.search-select {
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.search-input {
  width: 200px;
}

.search-input:focus,
.search-select:focus {
  border-color: #667eea;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table tr:hover {
  background: #f8f9fa;
}

.course-cover {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.difficulty-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.difficulty-1 {
  background: #f6ffed;
  color: #52c41a;
}

.difficulty-2 {
  background: #fff7e6;
  color: #fa8c16;
}

.difficulty-3 {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-0 {
  background: #f0f0f0;
  color: #666;
}

.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.status-2 {
  background: #fff1f0;
  color: #ff4d4f;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn:hover { opacity: 0.85; transform: translateY(-1px); }
.action-edit { background: #e6f7ff; color: #1890ff; }
.action-edit:hover { background: #1890ff; color: #fff; }
.action-warn { background: #fff7e6; color: #fa8c16; }
.action-warn:hover { background: #fa8c16; color: #fff; }
.action-delete { background: #fff1f0; color: #ff4d4f; }
.action-delete:hover { background: #ff4d4f; color: #fff; }

.pagination { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-top: 1px solid #f0f0f0; background: #fafbfc; border-radius: 0 0 12px 12px; }
.pagination-left, .pagination-center, .pagination-right { display: flex; align-items: center; gap: 8px; }
.total { font-size: 13px; color: #666; }
.total em { font-style: normal; color: #667eea; font-weight: 600; }
.page-btn { display: inline-flex; align-items: center; gap: 4px; padding: 8px 14px; border: 1px solid #e8e8e8; background: #fff; border-radius: 8px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.25s; }
.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(102,126,234,0.15); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.page-btn:first-child, .page-btn:last-child { padding: 8px 10px; }
.page-info { padding: 0 12px; font-size: 13px; color: #666; }
.page-info strong { color: #667eea; font-size: 15px; font-weight: 600; }
.page-size { padding: 8px 32px 8px 12px; border: 1px solid #e8e8e8; border-radius: 8px; font-size: 13px; color: #666; background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 8L2 4h8z'/%3E%3C/svg%3E") no-repeat right 10px center; cursor: pointer; outline: none; appearance: none; transition: all 0.2s; }
.page-size:hover { border-color: #667eea; }
.page-size:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
}

.modal-large {
  max-width: 700px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 25px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.required {
  color: #ff4d4f;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 15px;
}

.preview-cover {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.cover-placeholder {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 章节管理样式 */
.chapter-header {
  margin-bottom: 20px;
}

.chapter-list {
  max-height: 400px;
  overflow-y: auto;
}

.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.chapter-index {
  background: #667eea;
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.chapter-title {
  font-size: 14px;
  color: #333;
}

.chapter-duration {
  color: #999;
  font-size: 12px;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
