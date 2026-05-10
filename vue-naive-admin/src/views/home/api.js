/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 22:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取首页统计数据
  getDashboardStats: () => request.get('/dashboard/stats'),
  
  // 获取用户概览数据
  getUserOverview: () => request.get('/dashboard/user-overview'),
  
  // 获取课程统计数据
  getCourseStats: () => request.get('/dashboard/course-stats'),
  
  // 获取作业统计数据
  getHomeworkStats: () => request.get('/dashboard/homework-stats'),
  
  // 获取最新公告
  getLatestAnnouncements: (limit = 5) => request.get('/dashboard/announcements', { params: { limit } }),
  
  // 获取最新课程
  getLatestCourses: (limit = 6) => request.get('/dashboard/courses', { params: { limit } }),
  
  // 获取待办事项
  getTodoList: () => request.get('/dashboard/todos'),
  
  // 获取学习进度统计
  getLearningProgress: () => request.get('/dashboard/learning-progress'),
  
  // 获取系统通知
  getSystemNotifications: (params = {}) => request.get('/dashboard/notifications', { params }),
  
  // 标记通知为已读
  markNotificationAsRead: id => request.patch(`/dashboard/notifications/${id}/read`),
  
  // 获取快捷操作菜单
  getQuickActions: () => request.get('/dashboard/quick-actions'),
  
  // 获取图表数据
  getChartData: (type, params = {}) => request.get(`/dashboard/charts/${type}`, { params })
}