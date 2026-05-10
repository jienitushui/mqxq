package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 课程分页查询DTO
 * 
 * 用于课程列表的分页查询，包含查询条件和分页参数
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程分页查询请求对象")
public class CoursePageQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "课程标题关键词（支持模糊查询）", example = "Java")
    private String title;

    @Min(value = 0, message = "课程状态不能小于0")
    @Schema(description = "课程状态：0-未发布，1-已发布", example = "1")
    private Integer status;

    @Min(value = 1, message = "分类ID必须大于0")
    @Schema(description = "课程分类ID", example = "1")
    private Integer subjectId;

    @Min(value = 1, message = "教师ID必须大于0")
    @Schema(description = "教师ID", example = "1")
    private Integer teacherId;
}

