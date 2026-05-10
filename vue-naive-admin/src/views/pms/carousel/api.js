/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 分页查询轮播图
  getList: (params = {}) => request.get('/api/admin/carousel/page', { params }),
  
  // 获取轮播图列表
  getCarouselList: (params = {}) => request.get('/api/admin/carousel/list', { params }),
  
  // 获取轮播图详情
  getDetail: id => request.get(`/api/admin/carousel/${id}`),
  
  // 创建轮播图
  create: data => {
    console.log('API创建请求数据:', data)
    return request.post('/api/admin/carousel', data)
  },
  
  // 更新轮播图
  update: (id, data) => {
    console.log('API更新请求数据:', data)
    return request.put(`/api/admin/carousel/${id}`, data)
  },
  
  // 删除轮播图
  delete: id => request.delete(`/api/admin/carousel/${id}`),
  
  // 批量删除轮播图
  batchDelete: ids => request.delete('/api/admin/carousel/batch', { data: ids }),
  
  // 上传图片
  uploadImage: data => {
    console.log('API上传图片请求')
    return request.post('/api/public/files/image', data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}