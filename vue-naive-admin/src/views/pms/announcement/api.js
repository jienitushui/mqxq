/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询公告
  getList: (params = {}) => request.get('/api/admin/announcements/page', { params }),
  
  // 获取公告详情
  getDetail: id => request.get(`/api/admin/announcements/${id}`),
  
  // 创建公告
  create: data => {
    console.log('API创建请求数据:', data)
    return request.post('/api/admin/announcements', data)
  },
  
  // 更新公告
  update: (id, data) => {
    console.log('API更新请求数据:', data)
    return request.put(`/api/admin/announcements/${id}`, data)
  },
  
  // 删除公告
  delete: id => request.delete(`/api/admin/announcements/${id}`),
  
  // 批量删除公告
  batchDelete: ids => request.delete('/api/admin/announcements/batch', { data: ids }),
  
  // 发布公告
  publish: id => request.put(`/api/admin/announcements/${id}/publish`),
  
  // 撤回公告
  withdraw: id => request.put(`/api/admin/announcements/${id}/withdraw`),
  
  // 获取公告分类列表
  getCategories: () => request.get('/api/admin/announcement-categories/all')
}