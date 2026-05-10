package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 已发布课程查询DTO
 * 
 * 用于用户端和公共接口查询已发布的课程
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "已发布课程查询请求对象")
public class PublishedCourseQueryDTO {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "课程标题关键词（支持模糊查询）", example = "Java")
    private String title;

    @Min(value = 1, message = "分类ID必须大于0")
    @Schema(description = "课程分类ID", example = "1")
    private Integer subjectId;
}

