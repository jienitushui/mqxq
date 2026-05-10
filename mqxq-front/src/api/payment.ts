import request from '@/utils/request'
import type { Result } from './types/common'

// 支付相关API
export const paymentApi = {
  // 发起支付宝支付
  alipayPay(orderNo: string): Promise<void> {
    // 直接跳转到支付页面
    // window.location.href = `http://mqxq.free.idcfengye.com/api/alipay/pay?orderNo=${orderNo}`
    window.location.href = `http://159.75.11.181:9999/api/alipay/pay?orderNo=${orderNo}`
    return Promise.resolve()
  },

  // 支付宝退款
  alipayRefund(orderNo: string): Promise<Result> {
    return request.get('/api/alipay/refund', { params: { orderNo } })
  },

  // 支付宝异步通知回调（通常由后端处理，前端不直接调用）
  alipayNotify(data: any): Promise<string> {
    return request.post('/api/alipay/notify', data)
  }
}