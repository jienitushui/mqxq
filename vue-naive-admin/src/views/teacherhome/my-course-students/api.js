import { request } from '@/utils'

const BASE_URL = '/api/teacher/my-course'

export default {
  // 获取我的课程学员列表
  getStudentsList(params) {
    return request.get(`${BASE_URL}/students`, { params })
  },

  // 获取指定课程学员列表
  getCourseStudents(courseId, params) {
    return request.get(`${BASE_URL}/course/${courseId}/students`, { params })
  },

  // 获取学员学习详情
  getStudentDetail(myCourseId) {
    return request.get(`${BASE_URL}/student-detail/${myCourseId}`)
  },

  // 移除课程学员
  removeStudent(myCourseId) {
    return request.delete(`${BASE_URL}/remove-student/${myCourseId}`)
  },

  // 批量移除学员
  batchRemoveStudents(myCourseIds) {
    return request.delete(`${BASE_URL}/batch-remove-students`, {
      data: { myCourseIds }
    })
  },

  // 获取我的课程统计
  getMyStatistics() {
    return request.get(`${BASE_URL}/my-statistics`).then(response => {
      // 直接返回统计数据，不包装在data中
      return response || {
        averageRating: 0,
        totalStudents: 0,
        totalCourses: 0
      }
    })
  },

  // 获取指定课程统计
  getCourseStatistics(courseId) {
    return request.get(`${BASE_URL}/course-statistics/${courseId}`)
  },

  // 导出学员列表
  exportStudents(courseId, params) {
    return request.get(`${BASE_URL}/export-students/${courseId}`, { 
      params,
      responseType: 'blob'
    })
  },

  // 获取我的课程列表（用于下拉选择）
  getMyCourses(params = {}) {
    return request.get('/api/teacher/course/page', { params }).then(response => {
      // 处理分页格式的响应，提取list数组
      return response.data.list || []
    })
  }
}