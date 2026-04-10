/**
 * 组卷相关API
 */
import request from '@/utils/request'

/**
 * 预览组卷结果
 * @param {Object} config - 组卷配置
 * @returns {Promise}
 */
export function previewAssembly(config) {
  return request.post('/exam/paper/preview', config)
}

/**
 * 按难度随机组卷
 * @param {Object} config - 组卷配置
 * @returns {Promise}
 */
export function assembleByDifficulty(config) {
  return request.post('/exam/paper/assemble', config)
}

/**
 * 获取候选题目池
 * @param {Object} params - 筛选条件
 * @returns {Promise}
 */
export function getQuestionPool(params) {
  return request.get('/exam/paper/question-pool', { params })
}

/**
 * 手动组卷
 * @param {Number} paperId - 试卷ID
 * @param {Array} questionIds - 题目ID列表
 * @param {Array} scores - 分值列表
 * @returns {Promise}
 */
export function manualAssembly(paperId, questionIds, scores) {
  return request.post('/exam/paper/manual-assembly', {
    paperId,
    questionIds,
    scores
  })
}

/**
 * 获取可用题目数量
 * @param {Object} params - 筛选条件
 * @returns {Promise}
 */
export function getAvailableCount(params) {
  return request.get('/exam/paper/available-count', { params })
}

/**
 * 按知识点组卷
 * @param {Object} params - 组卷参数
 * @returns {Promise}
 */
export function assembleByKnowledgePoints(params) {
  return request.post('/exam/paper/assemble-by-kp', params)
}

/**
 * 获取学科知识点列表
 * @param {Number} subjectId - 学科ID
 * @returns {Promise}
 */
export function getKnowledgePoints(subjectId) {
  return request.get('/exam/paper/knowledge-points', {
    params: { subjectId }
  })
}
