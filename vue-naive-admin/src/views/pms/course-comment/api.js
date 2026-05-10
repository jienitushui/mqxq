/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取评论列表
  getList: (params = {}) => request.get('/api/admin/comment/list', { params }),
  
  // 获取评论详情
  getDetail: id => request.get(`/api/admin/comment/${id}`),
  
  // 删除评论
  delete: id => request.delete(`/api/admin/comment/${id}`),
  
  // 批量删除评论
  batchDelete: ids => request.delete('/api/admin/comment/batch-delete', { data: { ids } }),
  
  // 批量隐藏评论
  batchHide: ids => request.put('/api/admin/comment/batch-hide', { ids }),
  
  // 批量显示评论
  batchShow: ids => request.put('/api/admin/comment/batch-show', { ids }),
  
  // 切换评论状态
  toggleStatus: id => request.put(`/api/admin/comment/${id}/toggle-status`),
  
  // 获取评论统计
  getStatistics: () => request.get('/api/admin/comment/statistics'),
  
  // 获取课程评分排行
  getCourseRatingRank: (params = {}) => request.get('/api/admin/comment/course-rating-rank', { params }),
  
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