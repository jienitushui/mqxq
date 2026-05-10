/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 22:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取教师课程统计
  getMyCourseStats: () => request.get('/api/teacher/my-course/my-statistics'),
  
  // 获取学生统计
  getStudentStats: () => request.get('/api/teacher/my-course/students'),
  
  // 获取作业统计
  getHomeworkStats: () => request.get('/api/teacher/homework-manage/statistics'),
  
  // 获取最新评论
  getLatestComments: (limit = 10) => request.get('/api/teacher/comment/latest', { params: { limit } }),
  
  // 获取待处理作业
  getPendingHomeworks: () => request.get('/api/teacher/homework-manage/upcoming-deadline'),
  
  // 获取我教授的课程列表
  getMyTeachingCourses: (params = {}) => request.get('/api/teacher/course/page', { params }),
  
  // 获取我的课程学生
  getMyCourseStudents: (params = {}) => request.get('/api/teacher/my-course/students', { params }),
  
  // 获取课程评论趋势分析
  getCommentTrend: () => request.get('/api/teacher/comment/trend-analysis'),
  
  // 获取我的评分统计
  getMyRatingStats: () => request.get('/api/teacher/comment/my-rating-statistics'),
  
  // 获取订单统计
  getOrderStats: () => request.get('/api/teacher/orders/statistics'),
  
  // 获取热门课程
  getTopCourses: () => request.get('/api/teacher/orders/top-courses'),
  
  // 获取我的课程订单
  getMyCourseOrders: () => request.get('/api/teacher/orders/my-courses'),

  // 获取订单详情
  getOrderDetail: (orderId) => request.get(`/api/teacher/orders/detail/${orderId}`),

  // 获取我的收入
  getMyRevenue: () => request.get('/api/teacher/orders/my-revenue')
}