package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.order.OrderQueryDTO;
import com.jieni.mqxq.domain.vo.order.OrderStatisticsVO;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.service.order.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器（管理员端）
 * 
 * 提供管理员端订单的全面管理功能，包括订单查询、订单详情、订单删除等操作
 * 支持按用户、课程、状态等条件筛选，提供订单统计和收入统计功能
 * 确保只有管理员角色可以访问，包含完善的日志记录和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/orders")
@Tag(name = "管理员-订单管理", description = "管理员端订单管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class OrdersAdminController {

    @Resource
    private OrdersService ordersService;

    /**
     * 获取订单列表
     */
    @Operation(summary = "订单列表", description = "管理员获取订单列表")
    @GetMapping("/list")
    public Result<PageInfo<OrderVO>> getOrdersList(@Valid OrderQueryDTO queryDTO) {
        log.info("管理员获取订单列表, queryDTO: {}", queryDTO);
        
        PageInfo<OrderVO> pageInfo = ordersService.getAdminOrderPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "订单详情", description = "管理员获取订单详情")
    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "订单ID必须大于0") Integer orderId) {
        log.info("管理员获取订单详情, orderId: {}", orderId);
        
        OrderVO orderVO = ordersService.getAdminOrderById(orderId);
        return Result.success(orderVO);
    }

    /**
     * 删除订单
     */
    @Operation(summary = "删除订单", description = "管理员删除订单")
    @DeleteMapping("/delete/{orderId}")
    public Result<String> deleteOrder(
            @Parameter(description = "订单ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "订单ID必须大于0") Integer orderId) {
        log.info("管理员删除订单, orderId: {}", orderId);
        
        ordersService.deleteOrderById(orderId);
        return Result.success("订单删除成功");
    }

    /**
     * 获取订单统计
     */
    @Operation(summary = "订单统计", description = "管理员获取订单统计信息（包含订单数量统计、收入统计、最近订单）")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        log.info("管理员获取订单统计");
        
        OrderStatisticsVO statistics = ordersService.getOrderStatistics();
        return Result.success(statistics);
    }
}