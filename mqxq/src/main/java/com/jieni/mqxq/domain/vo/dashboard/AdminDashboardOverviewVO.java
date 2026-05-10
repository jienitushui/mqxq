package com.jieni.mqxq.domain.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理员端数据大屏概览VO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员端数据大屏概览")
public class AdminDashboardOverviewVO {
    
    @Schema(description = "总用户数")
    private Integer totalUsers;
    
    @Schema(description = "总教师数")
    private Integer totalTeachers;
    
    @Schema(description = "总学员数")
    private Integer totalStudents;
    
    @Schema(description = "总课程数")
    private Integer totalCourses;
    
    @Schema(description = "已发布课程数")
    private Integer publishedCourses;
    
    @Schema(description = "总订单数")
    private Integer totalOrders;
    
    @Schema(description = "已完成订单数")
    private Integer completedOrders;
    
    @Schema(description = "平台总收入")
    private BigDecimal totalRevenue;
    
    @Schema(description = "今日新增用户")
    private Integer todayNewUsers;
    
    @Schema(description = "今日新增订单")
    private Integer todayNewOrders;
    
    @Schema(description = "今日收入")
    private BigDecimal todayRevenue;
    
    @Schema(description = "总学习记录数")
    private Integer totalLearningRecords;
    
    @Schema(description = "学习中记录数")
    private Integer studyingRecords;
    
    @Schema(description = "已完成记录数")
    private Integer completedRecords;
    
    @Schema(description = "课程完成率")
    private Double completionRate;
    
    @Schema(description = "总浏览量")
    private Integer totalViews;
    
    @Schema(description = "独立访客数")
    private Integer uniqueVisitors;
    
    @Schema(description = "总作业数")
    private Integer totalHomeworks;
    
    @Schema(description = "总作业提交数")
    private Integer totalSubmissions;
    
    @Schema(description = "总评论数")
    private Integer totalComments;
    
    @Schema(description = "总公告数")
    private Integer totalAnnouncements;
    
    @Schema(description = "总章节数")
    private Integer totalChapters;
    
    @Schema(description = "总小节数")
    private Integer totalSections;
}

