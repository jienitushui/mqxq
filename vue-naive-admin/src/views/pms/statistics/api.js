import { request } from '@/utils/http'

// 统计数据API
export const statisticsApi = {
  // 用户统计
  getUserStats() {
    return request.get('/api/admin/users/statistics')
  },
  
  // 课程统计
  getCourseStats() {
    return request.get('/api/admin/course/statistics')
  },
  
  // 浏览统计
  getBrowseStats() {
    return request.get('/api/admin/course-view/statistics')
  },
  
  // 总体学习统计
  getLearningStats() {
    return request.get('/api/admin/my-course/overall-statistics')
  },
  
  // 章节统计
  getChapterStats() {
    return request.get('/api/admin/chapter/statistics')
  },
  
  // 小节统计
  getSectionStats() {
    return request.get('/api/admin/section/statistics')
  },
  
  // 作业统计
  getHomeworkStats() {
    return request.get('/api/admin/homework/statistics')
  },
  
  // 提交统计
  getSubmissionStats() {
    return request.get('/api/admin/homework-submission/statistics')
  },
  
  // 订单统计
  getOrderStats() {
    return request.get('/api/admin/orders/statistics')
  },
  
  // 收入统计
  getRevenueStats() {
    return request.get('/api/admin/orders/statistics')
  },
  
  // 评论统计
  getCommentStats() {
    return request.get('/api/admin/comment/statistics')
  },
  
  // 指定课程学习统计
  getCourseStudyStats(courseId) {
    return request.get(`/api/admin/my-course/course-statistics/${courseId}`)
  },
  
  // 指定用户学习统计
  getUserStudyStats(userId) {
    return request.get(`/api/admin/my-course/user-statistics/${userId}`)
  }
}

export default statisticsApi