package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作业提交查询DTO
 * 
 * 用于作业提交列表查询的参数封装，支持多维度筛选
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交查询DTO")
public class HomeworkSubmissionQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    @Schema(description = "作业ID", example = "1")
    @Min(value = 1, message = "作业ID必须大于0")
    private Integer homeworkId;

    @Schema(description = "学生ID", example = "1")
    @Min(value = 1, message = "学生ID必须大于0")
    private Integer studentId;

    @Schema(description = "提交状态：0-待批改，1-已批改", example = "0")
    private Integer status;

    @Schema(description = "学生姓名（模糊查询）", example = "张三")
    private String studentName;

    @Schema(description = "作业标题（模糊查询）", example = "Java基础")
    private String homeworkTitle;

    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "开始日期", example = "2025-01-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-31")
    private String endDate;
}

