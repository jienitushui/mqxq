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
  getList: (params = {}) => request.get('/api/admin/chapter/page', { params }),
  
  // 获取章节详情
  getDetail: id => request.get(`/api/admin/chapter/${id}`),
  
  // 新增章节
  create: data => request.post('/api/admin/chapter', data),
  
  // 更新章节
  update: (id, data) => request.put(`/api/admin/chapter/${id}`, data),
  
  // 删除章节
  delete: id => request.delete(`/api/admin/chapter/${id}`),
  
  // 批量删除章节
  batchDelete: ids => request.delete('/api/admin/chapter/batch', { data: ids }),
  
  // 获取章节统计
  getStatistics: () => request.get('/api/admin/chapter/statistics'),
  
  // 获取课程章节
  getCourseChapters: courseId => request.get(`/api/admin/chapter/course/${courseId}`),
  
  // 获取课程列表（支持分页）
  getCourses: (params = {}) => request.get('/api/admin/course/page', { 
    params: { 
      page: 1,
      size: 10,
      status: 1,
      ...params
    } 
  })
}