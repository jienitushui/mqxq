/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询公告分类
  getList: (params = {}) => {
    console.log('API请求参数:', params)
    return request.get('/api/admin/announcement-categories/page', { params })
  },
  
  // 获取公告分类详情
  getDetail: id => {
    console.log('获取详情ID:', id)
    return request.get(`/api/admin/announcement-categories/${id}`)
  },
  
  // 创建公告分类
  create: data => {
    console.log('创建数据:', data)
    return request.post('/api/admin/announcement-categories', data)
  },
  
  // 更新公告分类
  update: (id, data) => {
    console.log('更新ID:', id, '数据:', data)
    return request.put(`/api/admin/announcement-categories/${id}`, data)
  },
  
  // 删除公告分类
  delete: id => {
    console.log('删除ID:', id)
    return request.delete(`/api/admin/announcement-categories/${id}`)
  },
  
  // 获取所有公告分类（用于下拉选择等）
  getAll: () => request.get('/api/admin/announcement-categories/all'),
  
  // 批量删除公告分类
  batchDelete: ids => {
    console.log('批量删除IDs:', ids)
    return request.delete('/api/admin/announcement-categories/batch', { data: { ids } })
  }
}