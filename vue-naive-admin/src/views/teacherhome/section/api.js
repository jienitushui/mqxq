/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询小节
  getList: (params = {}) => request.get('/api/teacher/section/page', { params }),
  
  // 获取小节详情
  getDetail: id => request.get(`/api/teacher/section/${id}`),
  
  // 新增小节
  create: data => request.post('/api/teacher/section', data),
  
  // 更新小节
  update: (id, data) => request.put(`/api/teacher/section/${id}`, data),
  
  // 删除小节
  delete: id => request.delete(`/api/teacher/section/${id}`),
  
  // 获取章节小节
  getChapterSections: chapterId => request.get(`/api/teacher/section/chapter/${chapterId}`),
  
  // 获取课程小节
  getCourseSections: courseId => request.get(`/api/teacher/section/course/${courseId}`),
  
  // 获取教师课程列表（用于下拉选择）
  getCourses: (params = {}) => request.get('/api/teacher/course/page', { params }),
  
  // 获取章节列表（用于下拉选择）
  getChapters: courseId => request.get(`/api/teacher/chapter/course/${courseId}`),

  // 更新小节状态
  updateStatus: (id, status) => request.put(`/api/teacher/section/${id}/status?status=${status}`),

  // 获取小节统计
  getSectionStatistics: courseId => request.get(`/api/teacher/section/statistics/${courseId}`)
}