import { request } from '@/utils/http'

// 获取我的收入统计
export const getMyRevenue = () => {
  return request({
    url: '/api/teacher/orders/my-revenue',
    method: 'GET'
  })
}

// 获取我的课程订单列表
export const getMyCourseOrders = (params) => {
  return request({
    url: '/api/teacher/orders/my-course-orders',
    method: 'GET',
    params
  })
}

// 获取订单详情
export const getOrderDetail = (orderId) => {
  return request({
    url: `/api/teacher/orders/detail/${orderId}`,
    method: 'GET'
  })
}

export default {
  getMyRevenue,
  getMyCourseOrders,
  getOrderDetail
}