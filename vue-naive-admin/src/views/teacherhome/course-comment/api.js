import { request } from '@/utils'

const BASE_URL = '/api/teacher/comment'

export default {
  // 获取我的课程评论列表
  getMyComments(params) {
    return request.get(`${BASE_URL}/my-comments`, { params })
  },

  // 获取指定课程评论列表
  getCourseComments(courseId, params) {
    return request.get(`${BASE_URL}/course/${courseId}`, { params })
  },

  // 获取评论详情
  getCommentDetail(commentId) {
    return request.get(`${BASE_URL}/${commentId}`)
  },

  // 获取我的课程评分统计
  getMyRatingStatistics() {
    return request.get(`${BASE_URL}/my-rating-statistics`)
  },

  // 获取指定课程评分统计
  getCourseRatingStatistics(courseId) {
    return request.get(`${BASE_URL}/course-rating-statistics/${courseId}`)
  },

  // 获取最新评论
  getLatestComments(limit = 10) {
    return request.get(`${BASE_URL}/latest`, { params: { limit } })
  },

  // 获取评论趋势分析
  getTrendAnalysis(params) {
    return request.get(`${BASE_URL}/trend-analysis`, { params })
  },

  // 导出课程评论
  exportComments(courseId) {
    return request.get(`${BASE_URL}/export/${courseId}`, { 
      responseType: 'blob'
    })
  },

  // 回复评论
  replyComment(commentId, data) {
    return request.post(`${BASE_URL}/${commentId}/reply`, data)
  },

  // 获取我的课程列表（用于下拉选择）
  getMyCourses(params = {}) {
    return request.get('/api/teacher/course/page', { params })
  }
}