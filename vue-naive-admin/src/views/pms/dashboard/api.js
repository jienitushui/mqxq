import { request } from '@/utils'

const BASE_URL = '/api/admin/dashboard'

export default {
  // 获取数据大屏概览
  getOverview() {
    return request.get(`${BASE_URL}/overview`)
  },

  // 获取用户角色分布
  getUserRoleDistribution() {
    return request.get(`${BASE_URL}/user-role-distribution`)
  },

  // 获取用户状态分布
  getUserStatusDistribution() {
    return request.get(`${BASE_URL}/user-status-distribution`)
  },

  // 获取课程分类分布
  getCourseCategoryDistribution() {
    return request.get(`${BASE_URL}/course-category-distribution`)
  },

  // 获取课程状态分布
  getCourseStatusDistribution() {
    return request.get(`${BASE_URL}/course-status-distribution`)
  },

  // 获取订单状态分布
  getOrderStatusDistribution() {
    return request.get(`${BASE_URL}/order-status-distribution`)
  },

  // 获取学习状态分布
  getLearningStatusDistribution() {
    return request.get(`${BASE_URL}/learning-status-distribution`)
  },

  // 获取用户注册趋势
  getUserRegistrationTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/user-registration-trend`, { params })
  },

  // 获取订单金额趋势
  getOrderRevenueTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/order-revenue-trend`, { params })
  },

  // 获取订单数量趋势
  getOrderCountTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/order-count-trend`, { params })
  },

  // 获取课程创建趋势
  getCourseCreationTrend(params = { days: 30 }) {
    return request.get(`${BASE_URL}/course-creation-trend`, { params })
  },

  // 获取课程浏览量排行
  getTopCoursesByViews(params = { limit: 10 }) {
    return request.get(`${BASE_URL}/top-courses-by-views`, { params })
  },

  // 获取课程销售额排行
  getTopCoursesByRevenue(params = { limit: 10 }) {
    return request.get(`${BASE_URL}/top-courses-by-revenue`, { params })
  },

  // 获取教师收入排行
  getTopTeachers(params = { limit: 10 }) {
    return request.get(`${BASE_URL}/top-teachers`, { params })
  }
}

