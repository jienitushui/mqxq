import { request } from '@/utils'

const BASE_URL = '/api/teacher/homework'

export default {
  // 获取作业提交列表
  getSubmissionList(homeworkId, params) {
    return request.get(`${BASE_URL}/submissions`, { 
      params: { homeworkId, ...params }
    })
  },

  // 获取学生提交详情
  getSubmissionDetail(homeworkId, studentId) {
    return request.get(`/api/teacher/homework-submission/detail/${homeworkId}/${studentId}`)
  },

  // 批改作业
  gradeSubmission: (submissionId, data) => request.put('/api/teacher/homework/submission/grade', {
    submissionId,
    ...data
  }),

  // 批量批改作业
  batchGradeSubmissions(homeworkId, data) {
    return request.put(`/api/teacher/homework-submission/batch-grade/${homeworkId}`, data)
  },

  // 获取作业提交统计
  getSubmissionStatistics(homeworkId) {
    return request.get(`/api/teacher/homework-submission/statistics/${homeworkId}`)
  },

  // 获取未提交学生列表
  getUnsubmittedStudents(homeworkId) {
    return request.get(`/api/teacher/homework-submission/unsubmitted/${homeworkId}`)
  },

  // 导出提交记录
  exportSubmissions(homeworkId) {
    return request.get(`/api/teacher/homework-submission/export/${homeworkId}`, {
      responseType: 'blob'
    })
  },

  // 获取作业列表（支持分页和搜索）
  getHomeworkList(params) {
    return request.get('/api/teacher/homework/list', { params })
  }
}