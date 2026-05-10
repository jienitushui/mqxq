/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询作业
  getList: (params = {}) => request.get('/api/teacher/homework/list', { params }),

  // 获取作业详情
  getDetail: id => request.get(`/api/teacher/homework/${id}`),

  // 发布作业
  create: data => request.post('/api/teacher/homework', data),

  // 更新作业
  update: (id, data) => request.put(`/api/teacher/homework/${id}`, data),

  // 删除作业
  delete: id => request.delete(`/api/teacher/homework/${id}`),

  // 发布作业
  publish: id => request.put(`/api/teacher/homework/${id}/publish`),

  // 获取课程作业列表
  getCourseHomework: courseId => request.get(`/api/teacher/homework/course/${courseId}`),

  // 获取教师课程列表（用于下拉选择）
  getCourses: (params = {}) => request.get('/api/teacher/course/simple', { params }),

  // 导出作业数据
  exportHomework: (params = {}) => request.get('/api/teacher/homework-manage/export', {
    params,
    responseType: 'blob'
  }),
}