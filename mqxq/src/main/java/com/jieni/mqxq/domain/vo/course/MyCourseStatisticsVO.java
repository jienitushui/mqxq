package com.jieni.mqxq.domain.vo.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的课程统计视图对象
 * 
 * 用于展示课程学习统计数据，包括学员数量、完成率等信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的课程统计信息")
public class MyCourseStatisticsVO {

    @Schema(description = "总课程数")
    private Integer totalCourses;

    @Schema(description = "总学员数")
    private Integer totalStudents;

    @Schema(description = "总注册数")
    private Integer totalEnrollments;

    @Schema(description = "总用户数")
    private Integer totalUsers;

    @Schema(description = "已完成课程数")
    private Integer completedCourses;

    @Schema(description = "学习中课程数")
    private Integer studyingCourses;

    @Schema(description = "活跃学员数")
    private Integer activeStudents;

    @Schema(description = "已完成学员数")
    private Integer completedStudents;

    @Schema(description = "完成率（百分比）")
    private Double completionRate;

    @Schema(description = "平均评分")
    private Double averageRating;
}

