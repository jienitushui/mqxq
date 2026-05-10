package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.dashboard.*;
import com.jieni.mqxq.service.dashboard.DashboardService;
import com.jieni.mqxq.util.SaUtil;
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
 * 教师端数据大屏控制器
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/dashboard")
@Tag(name = "教师-数据大屏", description = "教师端数据大屏接口")
@CrossOrigin
@SaCheckRole("教师")
public class DashboardTeacherController {
    
    @Resource
    private DashboardService dashboardService;
    
    /**
     * 获取数据大屏概览
     */
    @Operation(summary = "数据大屏概览", description = "获取教师端数据大屏核心指标概览")
    @GetMapping("/overview")
    public Result<TeacherDashboardOverviewVO> getOverview() {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取数据大屏概览, teacherId: {}", teacherId);
        
        TeacherDashboardOverviewVO overview = dashboardService.getTeacherOverview(teacherId);
        return Result.success(overview);
    }
    
    /**
     * 获取课程状态分布
     */
    @Operation(summary = "课程状态分布", description = "获取课程状态分布数据（饼图）")
    @GetMapping("/course-status-distribution")
    public Result<List<DistributionDataVO>> getCourseStatusDistribution() {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程状态分布, teacherId: {}", teacherId);
        
        List<DistributionDataVO> data = dashboardService.getTeacherCourseStatusDistribution(teacherId);
        return Result.success(data);
    }
    
    /**
     * 获取学员学习状态分布
     */
    @Operation(summary = "学员学习状态分布", description = "获取学员学习状态分布数据（饼图）")
    @GetMapping("/student-status-distribution")
    public Result<List<DistributionDataVO>> getStudentStatusDistribution() {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取学员学习状态分布, teacherId: {}", teacherId);
        
        List<DistributionDataVO> data = dashboardService.getTeacherStudentStatusDistribution(teacherId);
        return Result.success(data);
    }
    
    /**
     * 获取课程浏览量趋势
     */
    @Operation(summary = "课程浏览量趋势", description = "获取课程浏览量趋势数据（折线图）")
    @GetMapping("/course-view-trend")
    public Result<List<TrendDataVO>> getCourseViewTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程浏览量趋势, teacherId: {}, days: {}", teacherId, days);
        
        List<TrendDataVO> data = dashboardService.getTeacherCourseViewTrend(teacherId, days);
        return Result.success(data);
    }
    
    /**
     * 获取收入趋势
     */
    @Operation(summary = "收入趋势", description = "获取收入趋势数据（折线图）")
    @GetMapping("/revenue-trend")
    public Result<List<TrendDataVO>> getRevenueTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取收入趋势, teacherId: {}, days: {}", teacherId, days);
        
        List<TrendDataVO> data = dashboardService.getTeacherRevenueTrend(teacherId, days);
        return Result.success(data);
    }
    
    /**
     * 获取学员增长趋势
     */
    @Operation(summary = "学员增长趋势", description = "获取学员增长趋势数据（折线图）")
    @GetMapping("/student-growth-trend")
    public Result<List<TrendDataVO>> getStudentGrowthTrend(
            @Parameter(description = "天数，默认30天", example = "30")
            @RequestParam(required = false, defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取学员增长趋势, teacherId: {}, days: {}", teacherId, days);
        
        List<TrendDataVO> data = dashboardService.getTeacherStudentGrowthTrend(teacherId, days);
        return Result.success(data);
    }
    
    /**
     * 获取课程销售排行
     */
    @Operation(summary = "课程销售排行", description = "获取课程销售排行数据（柱状图）")
    @GetMapping("/top-courses")
    public Result<List<RankingDataVO>> getTopCourses(
            @Parameter(description = "数量限制，默认10", example = "10")
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程销售排行, teacherId: {}, limit: {}", teacherId, limit);
        
        List<RankingDataVO> data = dashboardService.getTeacherTopCourses(teacherId, limit);
        return Result.success(data);
    }
    
    /**
     * 获取课程完成率排行
     */
    @Operation(summary = "课程完成率排行", description = "获取课程完成率排行数据（柱状图）")
    @GetMapping("/course-completion-ranking")
    public Result<List<RankingDataVO>> getCourseCompletionRanking(
            @Parameter(description = "数量限制，默认10", example = "10")
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "数量限制必须大于0") Integer limit) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程完成率排行, teacherId: {}, limit: {}", teacherId, limit);
        
        List<RankingDataVO> data = dashboardService.getTeacherCourseCompletionRanking(teacherId, limit);
        return Result.success(data);
    }
}

