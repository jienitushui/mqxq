package com.jieni.mqxq.domain.dto.courseview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课程浏览记录创建DTO
 * 
 * 用于创建新的课程浏览记录
 * 记录用户的课程浏览行为和相关环境信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程浏览记录创建DTO")
public class CourseViewCreateDTO {

    @Schema(description = "课程ID", required = true, example = "1")
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;
}

