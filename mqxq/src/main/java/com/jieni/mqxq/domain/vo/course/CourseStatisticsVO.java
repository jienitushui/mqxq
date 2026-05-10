package com.jieni.mqxq.domain.vo.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程统计VO
 * 
 * 用于管理员查看课程统计信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程统计视图对象")
public class CourseStatisticsVO {

    @Schema(description = "课程总数")
    private Long totalCourses;

    @Schema(description = "已发布课程数")
    private Long publishedCourses;

    @Schema(description = "未发布课程数")
    private Long unpublishedCourses;
}

