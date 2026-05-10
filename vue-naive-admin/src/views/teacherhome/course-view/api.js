import { request } from '@/utils'

const BASE_URL = '/api/teacher/course-view'

export default {
  // 分页查询教师课程浏览记录
  getCourseViewPage(params) {
    return request.get(`${BASE_URL}/page`, { params })
  },

  // 获取指定课程浏览记录
  getCourseViews(courseId, params) {
    return request.get(`${BASE_URL}/course/${courseId}`, { params })
  },

  // 获取课程浏览统计
  getCourseViewStatistics() {
    return request.get(`${BASE_URL}/statistics`)
  },

  // 获取我的课程列表（用于下拉选择）
  getMyCourses() {
    return request.get('/api/teacher/course/page', {
      params: { page: 1, size: 100 }
    })
  }
}