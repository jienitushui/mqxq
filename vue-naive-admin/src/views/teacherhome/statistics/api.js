/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2025/01/07 23:00:00
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request } from '@/utils'

export default {
  // 获取教师统计数据
  getStatistics: () => request.get('/api/teacher/my-course/my-statistics'),
  
  // 获取最近课程
  getRecentCourses: (params = { pageNum: 1, pageSize: 10 }) => request.get('/api/teacher/course/page', { params }),
  
  // 获取待批改作业
  getPendingHomeworks: () => request.get('/api/teacher/homework-manage/upcoming-deadline'),
  
  // 获取作业统计数据（用于图表）
  getHomeworkStatistics: () => request.get('/api/teacher/homework-manage/statistics'),

  // 我的课程评分统计
  getMyRatingStatistics() {
    return request.get(`${BASE_URL}/comment/my-rating-statistics`)
  },

  // 我的课程统计
  getMyCourseStatistics() {
    return request.get(`${BASE_URL}/my-course/my-statistics`)
  }
}