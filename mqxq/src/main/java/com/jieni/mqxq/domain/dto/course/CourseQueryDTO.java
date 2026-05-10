package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 课程查询DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程查询请求对象")
public class CourseQueryDTO {

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

