package com.jieni.mqxq.service.order;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.order.OrderPaymentDTO;
import com.jieni.mqxq.domain.dto.order.OrderQueryDTO;
import com.jieni.mqxq.domain.entity.Orders;
import com.jieni.mqxq.domain.vo.order.OrderStatisticsVO;
import com.jieni.mqxq.domain.vo.order.OrderVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 * 
 * 提供订单管理的完整服务功能，包括订单的CRUD操作、支付管理、状态控制、统计分析等。
 * 支持用户端、管理员端、教师端不同角色的订单管理需求，为在线教育平台的订单系统提供核心业务服务。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface OrdersService {

    // ========== 基础CRUD方法 ==========

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单信息
     */
    Orders queryById(Integer id);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Orders queryByOrderNo(String orderNo);

    /**
     * 创建订单
     *
     * @param orders 订单信息
     * @return 创建的订单
     */
    Orders createOrder(Orders orders);

    /**
     * 更新订单
     *
     * @param orders 订单信息
     * @return 更新后的订单
     */
    Orders updateOrder(Orders orders);

    /**
     * 删除订单
     *
     * @param id 订单ID
     * @return 是否删除成功
     */
    boolean deleteOrder(Integer id);

    /**
     * 插入订单
     *
     * @param orders 订单信息
     * @return 影响行数
     */
    int insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Orders selectByOrderNo(String orderNo);

    /**
     * 根据ID更新订单
     *
     * @param orders 订单信息
     * @return 影响行数
     */
    int updateById(Orders orders);

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Orders> queryByUserId(Integer userId);

    /**
     * 根据用户ID和状态查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Orders> queryByUserId(Integer userId, String status);

    // ========== 用户端方法 ==========

    /**
     * 用户获取订单分页列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageInfo<OrderVO> getUserOrderPage(OrderQueryDTO queryDTO);

    /**
     * 用户根据订单号获取订单详情
     *
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVO getUserOrderByOrderNo(Integer userId, String orderNo);

    /**
     * 用户根据订单ID获取订单详情
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单VO
     */
    OrderVO getUserOrderById(Integer userId, Integer orderId);

    /**
     * 处理订单支付
     *
     * @param paymentDTO 支付信息DTO
     * @return 是否成功
     */
    boolean processOrderPayment(OrderPaymentDTO paymentDTO);

    // ========== 管理员端方法 ==========

    /**
     * 管理员获取订单分页列表
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageInfo<OrderVO> getAdminOrderPage(OrderQueryDTO queryDTO);

    /**
     * 管理员获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单VO
     */
    OrderVO getAdminOrderById(Integer orderId);

    /**
     * 管理员删除订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean deleteOrderById(Integer orderId);

    /**
     * 获取订单统计信息
     *
     * @return 统计VO
     */
    OrderStatisticsVO getOrderStatistics();

    // ========== 教师端方法 ==========

    /**
     * 教师获取课程订单分页列表
     *
     * @param teacherId 教师ID
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageInfo<OrderVO> getTeacherOrderPage(Integer teacherId, OrderQueryDTO queryDTO);

    /**
     * 教师获取订单详情
     *
     * @param teacherId 教师ID
     * @param orderId 订单ID
     * @return 订单VO
     */
    OrderVO getTeacherOrderById(Integer teacherId, Integer orderId);

    /**
     * 获取教师收入统计
     *
     * @param teacherId 教师ID
     * @return 收入统计
     */
    Map<String, Object> getTeacherRevenueStatistics(Integer teacherId);

    /**
     * 获取教师热销课程列表
     *
     * @param teacherId 教师ID
     * @param limit     数量限制
     * @return 热销课程列表
     */
    List<Map<String, Object>> getTeacherTopCourses(Integer teacherId, Integer limit);
}