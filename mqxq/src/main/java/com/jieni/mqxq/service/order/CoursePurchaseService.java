package com.jieni.mqxq.service.order;

import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.domain.vo.order.PurchaseStatusVO;

/**
 * 课程购买服务接口
 * 
 * 提供完整的课程购买业务功能，包括购买流程、支付处理、订单管理等核心功能。
 * 实现购买前的权限校验、重复购买检查、订单状态管理等安全机制。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CoursePurchaseService {

    /**
     * 购买课程 - 创建订单
     * 
     * 处理用户购买课程的完整流程，包括课程验证、重复购买检查、订单创建等
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 创建的订单VO
     */
    OrderVO purchaseCourse(Integer userId, Integer courseId);

    /**
     * 检查课程购买状态
     * 
     * 查询用户对指定课程的购买状态，包括订单状态和支付情况
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 购买状态VO
     */
    PurchaseStatusVO checkPurchaseStatus(Integer userId, Integer courseId);

    /**
     * 取消订单
     * 
     * 用户取消指定的未支付订单
     *
     * @param userId  用户ID
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean cancelOrder(Integer userId, String orderNo);

    /**
     * 处理支付成功回调
     * 
     * 处理第三方支付平台的支付成功通知，完成订单状态更新和课程开通
     *
     * @param orderNo 订单号
     * @return 是否处理成功
     */
    boolean handlePaymentSuccess(String orderNo);

    /**
     * 检查用户是否可以学习课程
     * 
     * 综合判断用户是否有权限学习指定课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否可以学习
     */
    boolean canStudyCourse(Integer userId, Integer courseId);
}