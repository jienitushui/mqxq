package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程作业查询DTO
 * 
 * 用于作业列表查询的条件封装，支持多维度筛选和分页查询
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业查询DTO")
public class CourseHomeworkQueryDTO {

    @Schema(description = "作业ID")
    @Min(value = 1, message = "作业ID必须大于0")
    private Integer id;

    @Schema(description = "课程ID")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "作业标题（模糊查询）")
    private String title;

    @Schema(description = "作业状态：0-未发布，1-已发布")
    private Integer status;

    @Schema(description = "创建人ID")
    @Min(value = 1, message = "创建人ID必须大于0")
    private Integer createUser;

    @Schema(description = "课程名称（模糊查询）")
    private String courseName;

    @Schema(description = "教师姓名（模糊查询）")
    private String teacherName;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;
}

