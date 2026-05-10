package com.jieni.mqxq.domain.vo.courseview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程浏览统计VO
 * 
 * 用于向前端返回课程浏览的统计信息
 * 包含浏览量、用户数、课程数等多维度统计数据
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程浏览统计VO")
public class CourseViewStatisticsVO {

    @Schema(description = "总浏览次数", example = "1000")
    private Long totalViews;

    @Schema(description = "独立用户数", example = "100")
    private Long uniqueUsers;

    @Schema(description = "浏览课程数", example = "50")
    private Long uniqueCourses;

    @Schema(description = "总课程数", example = "20")
    private Long totalCourses;

    @Schema(description = "已发布课程数", example = "15")
    private Long publishedCourses;

    @Schema(description = "总学员数", example = "500")
    private Long totalStudents;
}

