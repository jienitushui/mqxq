package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.dashboard.*;
import com.jieni.mqxq.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端数据大屏控制器
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/dashboard")
@Tag(name = "管理员-数据大屏", description = "管理员端数据大屏接口")
@CrossOrigin
@SaCheckRole("管理员")
public class DashboardAdminController {
    
    @Resource
    private DashboardService dashboardService;
    
    /**
     * 获取数据大屏概览
     */
    @Operation(summary = "数据大屏概览", description = "获取管理员端数据大屏核心指标概览")
    @GetMapping("/overview")
    public Result<AdminDashboardOverviewVO> getOverview() {
        log.info("管理员获取数据大屏概览");
        
        AdminDashboardOverviewVO overview = dashboardService.getAdminOverview();
        return Result.success(overview);
    }
    
    /**
     * 获取用户角色分布
     */
    @Operation(summary = "用户角色分布", description = "获取用户角色分布数据（饼图）")
    @GetMapping("/user-role-distribution")
    public Result<List<DistributionDataVO>> getUserRoleDistribution() {
        log.info("管理员获取用户角色分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminUserRoleDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取用户状态分布
     */
    @Operation(summary = "用户状态分布", description = "获取用户状态分布数据（饼图）")
    @GetMapping("/user-status-distribution")
    public Result<List<DistributionDataVO>> getUserStatusDistribution() {
        log.info("管理员获取用户状态分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminUserStatusDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取课程分类分布
     */
    @Operation(summary = "课程分类分布", description = "获取课程分类分布数据（饼图）")
    @GetMapping("/course-category-distribution")
    public Result<List<DistributionDataVO>> getCourseCategoryDistribution() {
        log.info("管理员获取课程分类分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminCourseCategoryDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取课程状态分布
     */
    @Operation(summary = "课程状态分布", description = "获取课程状态分布数据（饼图）")
    @GetMapping("/course-status-distribution")
    public Result<List<DistributionDataVO>> getCourseStatusDistribution() {
        log.info("管理员获取课程状态分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminCourseStatusDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取订单状态分布
     */
    @Operation(summary = "订单状态分布", description = "获取订单状态分布数据（饼图）")
    @GetMapping("/order-status-distribution")
    public Result<List<DistributionDataVO>> getOrderStatusDistribution() {
        log.info("管理员获取订单状态分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminOrderStatusDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取学习状态分布
     */
    @Operation(summary = "学习状态分布", description = "获取学习状态分布数据（饼图）")
    @GetMapping("/learning-status-distribution")
    public Result<List<DistributionDataVO>> getLearningStatusDistribution() {
        log.info("管理员获取学习状态分布");
        
        List<DistributionDataVO> data = dashboardService.getAdminLearningStatusDistribution();
        return Result.success(data);
    }
    
    /**
     * 获取用户注册趋势
     */
    @Operation(summary = "用户注册趋势", description = "获取用户注册趋势数据（折线图）")
    @GetMapping("/user-registration-trend")
    public Result<List<TrendDataVO>> getUserRegistrationTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        log.info("管理员获取用户注册趋势, days: {}", days);
        
        List<TrendDataVO> data = dashboardService.getAdminUserRegistrationTrend(days);
        return Result.success(data);
    }
    
    /**
     * 获取订单金额趋势
     */
    @Operation(summary = "订单金额趋势", description = "获取订单金额趋势数据（折线图）")
    @GetMapping("/order-revenue-trend")
    public Result<List<TrendDataVO>> getOrderRevenueTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        log.info("管理员获取订单金额趋势, days: {}", days);
        
        List<TrendDataVO> data = dashboardService.getAdminOrderRevenueTrend(days);
        return Result.success(data);
    }
    
    /**
     * 获取订单数量趋势
     */
    @Operation(summary = "订单数量趋势", description = "获取订单数量趋势数据（折线图）")
    @GetMapping("/order-count-trend")
    public Result<List<TrendDataVO>> getOrderCountTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        log.info("管理员获取订单数量趋势, days: {}", days);
        
        List<TrendDataVO> data = dashboardService.getAdminOrderCountTrend(days);
        return Result.success(data);
    }
    
    /**
     * 获取课程创建趋势
     */
    @Operation(summary = "课程创建趋势", description = "获取课程创建趋势数据（折线图）")
    @GetMapping("/course-creation-trend")
    public Result<List<TrendDataVO>> getCourseCreationTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        log.info("管理员获取课程创建趋势, days: {}", days);
        
        List<TrendDataVO> data = dashboardService.getAdminCourseCreationTrend(days);
        return Result.success(data);
    }
    
    /**
     * 获取课程浏览量排行
     */
    @Operation(summary = "课程浏览量排行", description = "获取课程浏览量排行数据（柱状图）")
    @GetMapping("/top-courses-by-views")
    public Result<List<RankingDataVO>> getTopCoursesByViews(
            @Parameter(description = "数量限制，默认10", example = "10")
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        log.info("管理员获取课程浏览量排行, limit: {}", limit);
        
        List<RankingDataVO> data = dashboardService.getAdminTopCoursesByViews(limit);
        return Result.success(data);
    }
    
    /**
     * 获取课程销售额排行
     */
    @Operation(summary = "课程销售额排行", description = "获取课程销售额排行数据（柱状图）")
    @GetMapping("/top-courses-by-revenue")
    public Result<List<RankingDataVO>> getTopCoursesByRevenue(
            @Parameter(description = "数量限制，默认10", example = "10")
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        log.info("管理员获取课程销售额排行, limit: {}", limit);
        
        List<RankingDataVO> data = dashboardService.getAdminTopCoursesByRevenue(limit);
        return Result.success(data);
    }
    
    /**
     * 获取教师收入排行
     */
    @Operation(summary = "教师收入排行", description = "获取教师收入排行数据（柱状图）")
    @GetMapping("/top-teachers")
    public Result<List<RankingDataVO>> getTopTeachers(
            @Parameter(description = "数量限制，默认10", example = "10")
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        log.info("管理员获取教师收入排行, limit: {}", limit);
        
        List<RankingDataVO> data = dashboardService.getAdminTopTeachers(limit);
        return Result.success(data);
    }
}

