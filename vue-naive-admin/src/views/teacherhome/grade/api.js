/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取作业提交列表
  getSubmissions: (params = {}) => request.get('/api/teacher/homework/submissions', { params }),
  
  // 获取指定作业的提交列表
  getHomeworkSubmissions: (homeworkId, params = {}) => request.get(`/api/teacher/homework-submission/list/${homeworkId}`, { params }),
  
  // 获取提交详情
  getSubmissionDetail: id => request.get(`/api/teacher/homework/submission/${id}`),
  
  // 批改作业
  gradeSubmission: (submissionId, data) => request.put('/api/teacher/homework/submission/grade', {
    submissionId,
    ...data
  }),
  
  // 获取教师课程列表（用于下拉选择）
  getCourses: (params = {}) => request.get('/api/teacher/course/page', { params }),
  
  // 获取作业列表（用于下拉选择）
  getHomeworks: (courseId, params = {}) => request.get('/api/teacher/homework/list', { 
    params: { 
      courseId,
      ...params 
    } 
  })
}