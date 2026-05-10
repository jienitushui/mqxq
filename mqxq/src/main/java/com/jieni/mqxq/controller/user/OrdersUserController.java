package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.order.OrderPaymentDTO;
import com.jieni.mqxq.domain.dto.order.OrderQueryDTO;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.service.order.OrdersService;
import com.jieni.mqxq.service.order.CoursePurchaseService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单管理控制器
 * 
 * 提供用户管理课程购买订单的完整功能，包括创建订单、订单查询、取消订单、支付处理等。
 * 支持订单状态管理、权限验证、支付流程管理等功能，确保用户只能管理自己的订单。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/user/orders")
@Tag(name = "用户-订单管理", description = "用户端订单相关接口")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
public class OrdersUserController {

    @Resource
    private OrdersService ordersService;
    
    @Resource
    private CoursePurchaseService coursePurchaseService;

    /**
     * 创建课程购买订单
     */
    @Operation(summary = "创建课程订单", description = "用户创建课程购买订单")
    @PostMapping("/create-course-order/{courseId}")
    public Result<OrderVO> createCourseOrder(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户创建课程订单, userId: {}, courseId: {}", userId, courseId);
        
        OrderVO orderVO = coursePurchaseService.purchaseCourse(userId, courseId);
        return Result.success(orderVO);
    }

    /**
     * 获取我的订单列表
     */
    @Operation(summary = "我的订单列表", description = "分页获取用户的订单列表")
    @GetMapping("/my-orders")
    public Result<PageInfo<OrderVO>> getMyOrders(@Valid OrderQueryDTO queryDTO) {
        Integer userId = SaUtil.getLoginId();
        queryDTO.setUserId(userId);
        log.info("获取我的订单列表, queryDTO: {}", queryDTO);
        
        PageInfo<OrderVO> pageInfo = ordersService.getUserOrderPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "订单详情", description = "获取指定订单的详细信息")
    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "订单ID必须大于0") Integer orderId) {
        Integer userId = SaUtil.getLoginId();
        log.info("获取订单详情, userId: {}, orderId: {}", userId, orderId);
        
        OrderVO orderVO = ordersService.getUserOrderById(userId, orderId);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单", description = "用户取消未支付的订单")
    @PutMapping("/cancel/{orderNo}")
    public Result<String> cancelOrder(
            @Parameter(description = "订单号", required = true, in = ParameterIn.PATH, example = "MQXQ_ORDER_1234567890")
            @PathVariable @NotBlank(message = "订单号不能为空") String orderNo) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户取消订单, userId: {}, orderNo: {}", userId, orderNo);
        
        coursePurchaseService.cancelOrder(userId, orderNo);
        return Result.success("订单取消成功");
    }

    /**
     * 支付订单
     */
    @Operation(summary = "支付订单", description = "处理订单支付")
    @PostMapping("/pay")
    public Result<String> payOrder(@Valid @RequestBody OrderPaymentDTO paymentDTO) {
        log.info("处理订单支付, paymentDTO: {}", paymentDTO);
        
        ordersService.processOrderPayment(paymentDTO);
        return Result.success("支付成功");
    }

    /**
     * 根据订单号查询订单
     */
    @Operation(summary = "根据订单号查询", description = "根据订单号查询订单信息")
    @GetMapping("/by-order-no/{orderNo}")
    public Result<OrderVO> getOrderByOrderNo(
            @Parameter(description = "订单号", required = true, in = ParameterIn.PATH, example = "MQXQ_ORDER_1234567890")
            @PathVariable @NotBlank(message = "订单号不能为空") String orderNo) {
        Integer userId = SaUtil.getLoginId();
        log.info("根据订单号查询订单, userId: {}, orderNo: {}", userId, orderNo);
        
        OrderVO orderVO = ordersService.getUserOrderByOrderNo(userId, orderNo);
        return Result.success(orderVO);
    }
}