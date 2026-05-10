/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取用户课程详情
  getDetail: id => request.get(`/api/admin/my-course/${id}`),
  
  // 强制退出课程
  forceQuit: id => request.delete(`/api/admin/my-course/${id}`),
  
  // 批量强制退出课程
  batchQuit: data => request.delete('/api/admin/my-course/batch-quit', { data }),
  
  // 获取用户课程记录列表
  getList: (params = {}) => request.get('/api/admin/my-course/list', { params }),
  
  // 获取课程学习统计
  getCourseStatistics: courseId => request.get(`/api/admin/my-course/course-statistics/${courseId}`),
  
  // 获取总体学习统计
  getOverallStatistics: () => request.get('/api/admin/my-course/overall-statistics'),
  
  // 获取用户学习统计
  getUserStatistics: userId => request.get(`/api/admin/my-course/user-statistics/${userId}`)
}