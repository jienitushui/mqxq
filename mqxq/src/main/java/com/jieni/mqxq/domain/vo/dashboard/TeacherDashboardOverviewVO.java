package com.jieni.mqxq.domain.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 教师端数据大屏概览VO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "教师端数据大屏概览")
public class TeacherDashboardOverviewVO {
    
    @Schema(description = "课程总数")
    private Integer totalCourses;
    
    @Schema(description = "已发布课程数")
    private Integer publishedCourses;
    
    @Schema(description = "未发布课程数")
    private Integer unpublishedCourses;
    
    @Schema(description = "课程总浏览量")
    private Integer totalViews;
    
    @Schema(description = "课程总销售额")
    private BigDecimal totalRevenue;
    
    @Schema(description = "课程平均评分")
    private Double avgRating;
    
    @Schema(description = "课程评论总数")
    private Integer totalComments;
    
    @Schema(description = "总学员数")
    private Integer totalStudents;
    
    @Schema(description = "学习中学员数")
    private Integer studyingStudents;
    
    @Schema(description = "已完成学员数")
    private Integer completedStudents;
    
    @Schema(description = "已加入学员数")
    private Integer joinedStudents;
    
    @Schema(description = "学员完成率")
    private Double completionRate;
    
    @Schema(description = "作业总数")
    private Integer totalHomeworks;
    
    @Schema(description = "已发布作业数")
    private Integer publishedHomeworks;
    
    @Schema(description = "待批改作业数")
    private Integer pendingGradingCount;
    
    @Schema(description = "已批改作业数")
    private Integer gradedCount;
    
    @Schema(description = "作业平均分")
    private Double avgHomeworkScore;
    
    @Schema(description = "今日收入")
    private BigDecimal todayRevenue;
    
    @Schema(description = "本月收入")
    private BigDecimal monthRevenue;
    
    @Schema(description = "累计总收入")
    private BigDecimal cumulativeRevenue;
    
    @Schema(description = "今日订单数")
    private Integer todayOrderCount;
}

