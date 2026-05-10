import { request } from '@/utils'

const BASE_URL = '/api/admin/homework'

export default {
  // 获取所有作业列表
  getHomeworkList(params) {
    return request.get(`${BASE_URL}/list`, { params })
  },

  // 获取作业详情
  getHomeworkDetail(id) {
    return request.get(`${BASE_URL}/${id}`)
  },

  // 强制发布作业
  forcePublish(id) {
    return request.put(`${BASE_URL}/${id}/force-publish`)
  },

  // 强制取消发布
  forceUnpublish(id) {
    return request.put(`${BASE_URL}/${id}/force-unpublish`)
  },

  // 删除作业
  deleteHomework(id) {
    return request.delete(`${BASE_URL}/${id}`)
  },

  // 批量删除作业
  batchDeleteHomeworks(homeworkIds) {
    return request.delete(`${BASE_URL}/batch-delete`, {
      data: { homeworkIds }
    })
  },

  // 批量发布作业
  batchPublishHomeworks(homeworkIds) {
    return request.put(`${BASE_URL}/batch-publish`, {
      homeworkIds
    })
  },

  // 批量取消发布
  batchUnpublishHomeworks(homeworkIds) {
    return request.put(`${BASE_URL}/batch-unpublish`, {
      homeworkIds
    })
  },

  // 获取作业统计信息
  getHomeworkStatistics() {
    return request.get(`${BASE_URL}/statistics`)
  },

  // 获取教师作业统计排行
  getTeacherRank(limit = 10) {
    return request.get(`${BASE_URL}/teacher-rank`, { params: { limit } })
  },

  // 获取课程作业统计排行
  getCourseRank(limit = 10) {
    return request.get(`${BASE_URL}/course-rank`, { params: { limit } })
  },

  // 作业趋势分析
  getTrendAnalysis(days = 30) {
    return request.get(`${BASE_URL}/trend-analysis`, { params: { days } })
  },

  // 导出作业数据
  exportHomeworks(params) {
    return request.get(`${BASE_URL}/export`, { 
      params,
      responseType: 'blob'
    })
  },

  // 获取课程选项（支持分页）
  getCourseOptions(params = {}) {
    return request.get('/api/admin/course/page', {
      params: { 
        page: 1,
        size: 10,
        status: 1,
        ...params
      }
    })
  },

  // 获取教师列表（支持分页和搜索）
  getTeachers(params = {}) {
    return request.get('/api/admin/users/list', {
      params: { ...params, userType: 'TEACHER' }
    })
  }
}