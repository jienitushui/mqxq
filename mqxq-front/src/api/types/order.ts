// 订单信息
export interface Order {
  id: number
  orderNo: string
  userId: number
  courseId: number
  courseName: string
  coursePrice: number
  actualPrice: number
  goodsId?: number
  goodsName?: string
  goodsPrice?: number
  goodsImg?: string
  paymentMethod?: string
  paymentTime?: string
  payTime?: string
  payNo?: string
  status: number | string
  createTime: string
  updateTime: string
}

// 订单统计
export interface OrderStatistics {
  totalOrders: number
  paidOrders: number
  unpaidOrders: number
  totalAmount: number
  paidAmount: number
}

// 订单搜索参数
export interface OrderSearchParams {
  page?: number
  size?: number
  status?: string
}

// 支付请求参数
export interface PaymentRequest {
  orderNo: string
  paymentMethod: string
}