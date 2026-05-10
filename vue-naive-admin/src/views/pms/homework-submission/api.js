// 作业提交管理相关 API
import { request } from '@/utils/http'

const api = {
  /**
   * 获取作业提交列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页大小
   * @param {number} params.homeworkId - 作业ID
   * @param {number} params.studentId - 学生ID
   * @param {number} params.status - 提交状态
   * @param {string} params.studentName - 学生姓名
   * @param {string} params.homeworkTitle - 作业标题
   * @returns {Promise} 作业提交列表数据
   */
  getSubmissionList: (params) => {
    return request.get('/api/admin/homework-submission/list', { params })
  },

  /**
   * 获取作业提交详情
   * @param {number} id - 提交记录ID
   * @returns {Promise} 作业提交详情数据
   */
  getSubmissionDetail: (id) => {
    return request.get(`/api/admin/homework-submission/${id}`)
  },

  /**
   * 获取作业提交统计信息
   * @param {Object} params - 查询参数
   * @param {number} params.homeworkId - 作业ID
   * @param {string} params.startDate - 开始日期
   * @param {string} params.endDate - 结束日期
   * @returns {Promise} 作业提交统计数据
   */
  getSubmissionStatistics: (params) => {
    return request.get('/api/admin/homework-submission/statistics', { params })
  },

  /**
   * 删除作业提交记录
   * @param {number} id - 提交记录ID
   * @returns {Promise} 删除结果
   */
  deleteSubmission: (id) => {
    return request.delete(`/api/admin/homework-submission/${id}`)
  },

  /**
   * 批量删除作业提交记录
   * @param {Array} ids - 提交记录ID数组
   * @returns {Promise} 批量删除结果
   */
  batchDeleteSubmissions: (ids) => {
    return request.delete('/api/admin/homework-submission/batch', { data: ids })
  },

  /**
   * 导出作业提交数据
   * @param {Object} params - 导出参数
   * @param {number} params.homeworkId - 作业ID
   * @param {number} params.studentId - 学生ID
   * @param {number} params.status - 提交状态
   * @returns {Promise} 导出文件数据
   */
  exportSubmissions: (params) => {
    return request.get('/api/admin/homework-submission/export', { 
      params,
      responseType: 'blob'
    })
  }
}

export default api