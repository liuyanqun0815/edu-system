import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useConfigStore = defineStore('config', () => {
  // 配置数据
  const configs = ref({})
  const loaded = ref(false)

  // 计算属性：各类型配置
  const gradeList = computed(() => configs.value.gradeLevel || [])
  const questionTypes = computed(() => configs.value.questionType || [])
  const difficultyLevels = computed(() => configs.value.difficultyLevel || [])
  const courseCategories = computed(() => configs.value.courseCategory || [])
  const examSubjects = computed(() => configs.value.examSubject || [])
  const paperTypes = computed(() => configs.value.paperType || [])
  const courseStatuses = computed(() => configs.value.courseStatus || [])
  const examStatuses = computed(() => configs.value.examStatus || [])
  const userStatuses = computed(() => configs.value.userStatus || [])

  // 映射表（根据itemKey或itemValue快速查找）
  const questionTypeMap = computed(() => {
    const map = {}
    ;(configs.value.questionType || []).forEach(item => {
      map[item.itemValue] = item.itemName || item.itemLabel
    })
    return map
  })

  const difficultyMap = computed(() => {
    const map = {}
    ;(configs.value.difficultyLevel || []).forEach(item => {
      map[item.itemValue] = item.itemName || item.itemLabel
    })
    return map
  })

  const gradeColorMap = computed(() => {
    const map = {}
    ;(configs.value.gradeLevel || []).forEach(item => {
      if (item.itemColor) {
        map[item.itemName || item.itemLabel] = item.itemColor
      }
    })
    return map
  })

  // 从后端加载所有枚举配置
  async function loadConfigs() {
    if (loaded.value) return configs.value
    
    try {
      const res = await request.get('/system/config/enums')
      if (res.data.code === 200 && res.data.data) {
        configs.value = res.data.data
        loaded.value = true
      }
    } catch (e) {
      console.error('加载配置失败', e)
      // 使用默认配置
      configs.value = getDefaultConfigs()
      loaded.value = true
    }
    return configs.value
  }

  // 刷新配置
  async function refreshConfigs() {
    loaded.value = false
    return loadConfigs()
  }

  // 根据分类编码获取配置项
  function getConfigByCode(categoryCode) {
    return configs.value[categoryCode] || []
  }

  // 默认配置（网络失败时的兜底）
  function getDefaultConfigs() {
    return {
      gradeLevel: [
        { itemValue: '1', itemName: '一年级', itemLabel: '一年级' },
        { itemValue: '2', itemName: '二年级', itemLabel: '二年级' },
        { itemValue: '3', itemName: '三年级', itemLabel: '三年级' },
        { itemValue: '4', itemName: '四年级', itemLabel: '四年级' },
        { itemValue: '5', itemName: '五年级', itemLabel: '五年级' },
        { itemValue: '6', itemName: '六年级', itemLabel: '六年级' },
        { itemValue: '7', itemName: '初一', itemLabel: '初一' },
        { itemValue: '8', itemName: '初二', itemLabel: '初二' },
        { itemValue: '9', itemName: '初三', itemLabel: '初三' },
        { itemValue: '10', itemName: '高一', itemLabel: '高一' },
        { itemValue: '11', itemName: '高二', itemLabel: '高二' },
        { itemValue: '12', itemName: '高三', itemLabel: '高三' }
      ],
      questionType: [
        { itemValue: '1', itemName: '单选', itemLabel: '单选题' },
        { itemValue: '2', itemName: '多选', itemLabel: '多选题' },
        { itemValue: '3', itemName: '判断', itemLabel: '判断题' },
        { itemValue: '4', itemName: '填空', itemLabel: '填空题' },
        { itemValue: '5', itemName: '简答', itemLabel: '简答题' }
      ],
      difficultyLevel: [
        { itemValue: '1', itemName: '简单', itemLabel: '简单' },
        { itemValue: '2', itemName: '中等', itemLabel: '中等' },
        { itemValue: '3', itemName: '困难', itemLabel: '困难' }
      ]
    }
  }

  return {
    configs,
    loaded,
    gradeList,
    questionTypes,
    difficultyLevels,
    courseCategories,
    examSubjects,
    paperTypes,
    courseStatuses,
    examStatuses,
    userStatuses,
    questionTypeMap,
    difficultyMap,
    gradeColorMap,
    loadConfigs,
    refreshConfigs,
    getConfigByCode
  }
})
