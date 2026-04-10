<template>
  <div class="parse-template-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>站点解析模板管理</span>
          <el-button type="primary" @click="showAddDialog">新增模板</el-button>
        </div>
      </template>

      <el-table :data="templateList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="templateName" label="模板名称" />
        <el-table-column prop="siteName" label="站点名称" width="120" />
        <el-table-column prop="siteDomain" label="域名" width="150" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'danger'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editTemplate(row)">编辑</el-button>
            <el-button size="small" type="info" @click="viewRules(row)">规则</el-button>
            <el-button size="small" type="danger" @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="模板名称">
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="站点名称">
          <el-input v-model="form.siteName" />
        </el-form-item>
        <el-form-item label="站点域名">
          <el-input v-model="form.siteDomain" />
        </el-form-item>
        <el-form-item label="标题选择器">
          <el-input v-model="form.titleSelector" placeholder="CSS选择器" />
        </el-form-item>
        <el-form-item label="内容选择器">
          <el-input v-model="form.contentSelector" placeholder="CSS选择器" />
        </el-form-item>
        <el-form-item label="答案选择器">
          <el-input v-model="form.answerSelector" placeholder="CSS选择器" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const templateList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const form = ref({
  templateName: '',
  siteName: '',
  siteDomain: '',
  titleSelector: '',
  contentSelector: '',
  answerSelector: '',
  priority: 50,
  enabled: 1
})

// 加载模板列表
const loadTemplates = async () => {
  // TODO: 调用API
  templateList.value = []
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增模板'
  form.value = {
    templateName: '',
    siteName: '',
    siteDomain: '',
    titleSelector: '',
    contentSelector: '',
    answerSelector: '',
    priority: 50,
    enabled: 1
  }
  dialogVisible.value = true
}

// 编辑模板
const editTemplate = (row) => {
  dialogTitle.value = '编辑模板'
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存模板
const saveTemplate = async () => {
  // TODO: 调用API保存
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadTemplates()
}

// 删除模板
const deleteTemplate = (row) => {
  ElMessageBox.confirm('确认删除该模板?', '提示', {
    type: 'warning'
  }).then(async () => {
    // TODO: 调用API删除
    ElMessage.success('删除成功')
    loadTemplates()
  })
}

// 查看规则
const viewRules = (row) => {
  // TODO: 跳转到规则配置页面
  console.log('查看规则:', row)
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
