import { request } from '@/utils'

const BASE_URL = '/api/teacher/dashboard'

export default {
  // 获取数据大屏概览
  getOverview() {
    return request.get(`${BASE_URL}/overview`)
  },

  // 获取课程状态分布
  getCourseStatusDistribution() {
    return request.get(`${BASE_URL}/course-status-distribution`)
  },

  // 获取学员学习状态分布
  getStudentStatusDistribution() {
    return request.get(`${BASE_URL}/student-status-distribution`)
  },

  // 获取课程浏览量趋势
  getCourseViewTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/course-view-trend`, { params })
  },

  // 获取收入趋势
  getRevenueTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/revenue-trend`, { params })
  },

  // 获取学员增长趋势
  getStudentGrowthTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/student-growth-trend`, { params })
  },

  // 获取课程销售排行
  getTopCourses(params = { limit: 10 }) {
    return request.get(`${BASE_URL}/top-courses`, { params })
  },

  // 获取课程完成率排行
  getCourseCompletionRanking(params = { limit: 10 }) {
    return request.get(`${BASE_URL}/course-completion-ranking`, { params })
  }
}

