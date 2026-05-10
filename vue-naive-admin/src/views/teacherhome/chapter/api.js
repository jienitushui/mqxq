/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询章节
  getList: (params = {}) => request.get('/api/teacher/chapter/page', { params }),
  
  // 获取章节详情
  getDetail: id => request.get(`/api/teacher/chapter/${id}`),
  
  // 新增章节
  create: data => request.post('/api/teacher/chapter', data),
  
  // 更新章节
  update: (id, data) => request.put(`/api/teacher/chapter/${id}`, data),
  
  // 删除章节
  delete: id => request.delete(`/api/teacher/chapter/${id}`),
  
  // 获取课程章节
  getCourseChapters: courseId => request.get(`/api/teacher/chapter/course/${courseId}`),
  
  // 章节统计
  getStatistics: courseId => request.get(`/api/teacher/chapter/statistics/${courseId}`),
  
  // 获取教师课程列表（用于下拉选择）
  getCourses: (params = {}) => request.get('/api/teacher/course/page', { params })
}