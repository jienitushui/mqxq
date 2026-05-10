/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取课程分页列表
  getPage: (params = {}) => request.get('/api/admin/course/page', { params }),
  
  // 获取课程详情
  getDetail: id => request.get(`/api/admin/course/${id}`),
  
  // 创建课程
  create: data => request.post('/api/admin/course', data),
  
  // 更新课程
  update: (id, data) => request.put(`/api/admin/course/${id}`, data),
  
  // 删除课程
  delete: id => request.delete(`/api/admin/course/${id}`),
  
  // 发布课程
  publish: id => request.put(`/api/admin/course/${id}/publish`),
  
  // 下架课程
  unpublish: id => request.put(`/api/admin/course/${id}/unpublish`),
  
  // 获取课程统计信息
  getStatistics: () => request.get('/api/admin/course/statistics'),
  
  // 获取课程分类
  getCategories: () => request.get('/api/admin/course-subject/tree'),
  
  // 获取课程章节
  getChapters: courseId => request.get(`/api/admin/chapter/course/${courseId}`),
  
  // 添加课程章节
  addChapter: data => request.post('/api/admin/chapter', data),
  
  // 更新课程章节
  updateChapter: (id, data) => request.put(`/api/admin/chapter/${id}`, data),
  
  // 删除课程章节
  deleteChapter: id => request.delete(`/api/admin/chapter/${id}`),
  
  // 批量操作章节
  batchChapter: data => request.post('/api/admin/chapter/batch', data),
  
  // 获取教师列表（用于课程分配，支持分页和搜索）
  getTeachers: (params = {}) => request.get('/api/admin/users/list', { params: { ...params, userType: 'TEACHER' } }),
  
}