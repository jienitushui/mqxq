package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.order.OrderQueryDTO;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.service.order.OrdersService;
import com.jieni.mqxq.util.SaUtil;
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

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器（教师端）
 * 
 * 提供教师端订单查看和管理功能，包括课程订单查询、收入统计和热销课程分析
 * 支持按课程、状态筛选，确保教师只能查看自己课程的订单信息
 * 提供教师个人收入统计和课程销量排行等分析功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/teacher/orders")
@Tag(name = "教师-订单管理", description = "教师端订单管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class OrdersTeacherController {

    @Resource
    private OrdersService ordersService;

    /**
     * 获取我的课程订单列表
     */
    @Operation(summary = "我的课程订单", description = "教师获取自己课程的订单列表")
    @GetMapping("/my-course-orders")
    public Result<PageInfo<OrderVO>> getMyCourseOrders(@Valid OrderQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程订单列表, teacherId: {}, queryDTO: {}", teacherId, queryDTO);
        
        PageInfo<OrderVO> pageInfo = ordersService.getTeacherOrderPage(teacherId, queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取我的收入统计
     */
    @Operation(summary = "我的收入统计", description = "教师获取收入统计信息（包含总收入、订单总数、已完成订单数）")
    @GetMapping("/my-revenue")
    public Result<Map<String, Object>> getMyRevenue() {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取收入统计, teacherId: {}", teacherId);
        
        Map<String, Object> statistics = ordersService.getTeacherRevenueStatistics(teacherId);
        return Result.success(statistics);
    }

    /**
     * 获取热销课程
     */
    @Operation(summary = "热销课程", description = "教师获取热销课程排行")
    @GetMapping("/top-courses")
    public Result<List<Map<String, Object>>> getTopCourses(
            @Parameter(description = "数量限制，范围1-50", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取热销课程, teacherId: {}, limit: {}", teacherId, limit);
        
        List<Map<String, Object>> topCourses = ordersService.getTeacherTopCourses(teacherId, limit);
        return Result.success(topCourses);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "订单详情", description = "教师获取订单详情")
    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "订单ID必须大于0") Integer orderId) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取订单详情, teacherId: {}, orderId: {}", teacherId, orderId);
        
        OrderVO orderVO = ordersService.getTeacherOrderById(teacherId, orderId);
        return Result.success(orderVO);
    }
}