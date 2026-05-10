/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取教师课程列表
  getList: (params = {}) => request.get('/api/teacher/course/page', { params }),
  
  // 获取课程统计
  getStatistics: () => request.get('/api/teacher/course-view/statistics'),
  
  // 获取课程详情
  getDetail: id => request.get(`/api/teacher/course/${id}`),
  
  // 创建课程
  create: data => request.post('/api/teacher/course', data),
  
  // 更新课程信息
  update: (id, data) => request.put(`/api/teacher/course/${id}`, data),
  
  // 删除课程
  delete: id => request.delete(`/api/teacher/course/${id}`),
  
  // 发布课程
  publish: id => request.put(`/api/teacher/course/${id}/publish`),
  
  // 取消发布课程
  unpublish: id => request.put(`/api/teacher/course/${id}/unpublish`),
  
  // 获取课程分类列表
  getSubjects: () => request.get('/api/public/course-subject/tree'),

  // 获取简单课程列表
  getSimpleCourses: () => request.get('/api/teacher/course/simple')
}