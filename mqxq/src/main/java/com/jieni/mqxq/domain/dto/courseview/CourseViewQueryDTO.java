package com.jieni.mqxq.domain.dto.courseview;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 课程浏览记录查询DTO
 * 
 * 用于课程浏览记录的分页查询和条件筛选
 * 支持按课程、用户、IP地址等多维度查询
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "课程浏览记录查询DTO")
public class CourseViewQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "用户ID", example = "1")
    @Min(value = 1, message = "用户ID必须大于0")
    private Integer userId;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "用户代理", example = "Mozilla/5.0")
    private String userAgent;
}

