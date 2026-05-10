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
  getList: (params = {}) => request.get('/api/admin/section/page', { params }),
  
  // 获取小节详情
  getDetail: id => request.get(`/api/admin/section/${id}`),
  
  // 新增小节
  create: data => request.post('/api/admin/section', data),
  
  // 更新小节
  update: (id, data) => request.put(`/api/admin/section/${id}`, data),
  
  // 删除小节
  delete: id => request.delete(`/api/admin/section/${id}`),
  
  // 批量删除小节
  batchDelete: ids => request.delete('/api/admin/section/batch', { data: ids }),
  
  // 发布/取消发布小节
  updateStatus: (id, status) => request.put(`/api/admin/section/${id}/?status=${status}`),
  
  // 获取小节统计
  getStatistics: () => request.get('/api/admin/section/statistics'),
  
  // 获取章节小节
  getChapterSections: chapterId => request.get(`/api/admin/section/chapter/${chapterId}`),
  
  // 获取课程小节
  getCourseSections: courseId => request.get(`/api/admin/section/course/${courseId}`),
  
  // 获取课程列表（支持分页）
  getCourses: (params = {}) => request.get('/api/admin/course/page', { 
    params: { 
      page: 1,
      size: 10,
      status: 1,
      ...params
    } 
  }),
  
  // 获取章节列表（支持分页）
  getChapters: (params = {}) => request.get('/api/admin/chapter/page', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}