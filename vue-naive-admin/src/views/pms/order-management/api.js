import { request } from '@/utils/http'

// 订单管理API
export const orderApi = {
  /**
   * 获取订单列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页大小
   * @param {number} params.userId - 用户ID
   * @param {number} params.courseId - 课程ID
   * @param {string} params.status - 订单状态
   * @param {string} params.orderNo - 订单号
   * @returns {Promise} 订单列表数据
   */
  getOrderList(params) {
    return request.get('/api/admin/orders/list', { params })
  },
  
  /**
   * 获取订单详情
   * @param {number} orderId - 订单ID
   * @returns {Promise} 订单详情数据
   */
  getOrderDetail(orderId) {
    return request.get(`/api/admin/orders/detail/${orderId}`)
  },
  
  /**
   * 删除订单
   * @param {number} orderId - 订单ID
   * @returns {Promise} 删除结果
   */
  deleteOrder(orderId) {
    return request.delete(`/api/admin/orders/delete/${orderId}`)
  },
  
  /**
   * 获取订单统计信息
   * @returns {Promise} 订单统计数据
   */
  getOrderStatistics() {
    return request.get('/api/admin/orders/statistics')
  }
}

export default orderApi