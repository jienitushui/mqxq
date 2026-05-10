/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取用户列表（分页）
  getList: (params = {}) => request.get('/api/admin/users/list', { params }),
  
  // 获取用户详情
  getUserDetail: id => request.get(`/api/admin/users/${id}`),

    /**
   * 创建新用户
   * @param {Object} params - 创建用户DTO
   * @param {string} params.username - 用户名
   * @param {string} [params.password] - 密码(不填写则默认为123456)
   * @param {string} params.name - 姓名
   * @param {string} [params.phone] - 手机号
   * @param {string} [params.avatar] - 头像URL
   * @param {Array<string>} [params.roles] - 角色代码列表(不填写则默认为USER)
   * @returns {Promise<Object>} 创建结果
   */
  create: params => request.post('/api/admin/users/create', params),

  /**
   * 更新用户信息
   * @param {Object} params - 更新用户DTO
   * @param {number} params.id - 用户ID
   * @param {string} [params.name] - 姓名
   * @param {string} [params.phone] - 手机号
   * @param {string} [params.avatar] - 头像URL
   * @returns {Promise<Object>} 更新结果
   */
  update: params => request.put('/api/admin/users/update', params),
  
  // 删除用户
  delete: id => request.delete(`/api/admin/users/${id}`),

  /**
   * 批量删除用户
   * @param {Object} params - 批量删除DTO
   * @param {Array<number>} params.ids - 用户ID列表
   * @returns {Promise<Object>} 删除结果
   */
  batchDelete: params => request.delete('/api/admin/users/batch', { data: params }),
  
  // 重置用户密码
  resetPassword: id => request.put(`/api/admin/users/reset-password/${id}`),

  /**
   * 更新用户状态
   * @param {Object} params - 更新用户状态DTO
   * @param {number} params.id - 用户ID
   * @param {number} params.status - 状态 1:启用 0:禁用
   * @returns {Promise<Object>} 更新结果
   */
  updateStatus: params => request.put('/api/admin/users/status', params),
  
  // 获取用户角色列表
  getUserRoles: id => request.get(`/api/admin/users/roles/${id}`),
  
  // 获取用户类型统计
  getStatistics: () => request.get('/api/admin/users/statistics'),
  
}