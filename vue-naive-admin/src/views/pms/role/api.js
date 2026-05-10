/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取所有角色列表
  getList: () => request.get('/api/admin/role/list'),
  
  // 分页获取角色列表
  getPage: (params = {}) => request.get('/api/admin/role/page', { params }),
  
  // 根据ID获取角色详情
  getDetail: id => request.get(`/api/admin/role/${id}`),
  
  // 创建新角色
  create: data => request.post('/api/admin/role/add', data),
  
  // 更新角色信息
  update: data => request.put(`/api/admin/role/${data.id}`, data),
  
  // 删除角色
  delete: id => request.delete(`/api/admin/role/${id}`),
  
  // 批量删除角色
  batchDelete: ids => request.delete('/api/admin/role/batch', { data: { ids } }),
  
  // 获取角色权限
  getPermissions: id => request.get(`/api/admin/role/${id}/permissions`),
  
  // 分配角色权限
  assignPermissions: (id, permissionIds) => request.put(`/api/admin/role/${id}/permissions`, { permissionIds }),
  
  // 根据角色代码获取角色
  getByCode: roleCode => request.get(`/api/admin/role/code/${roleCode}`)
}