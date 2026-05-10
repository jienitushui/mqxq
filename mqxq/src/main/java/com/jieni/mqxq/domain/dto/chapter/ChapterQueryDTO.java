package com.jieni.mqxq.domain.dto.chapter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 章节查询DTO
 * 用于章节查询的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节查询DTO")
public class ChapterQueryDTO {
    
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;
    
    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;
    
    @Schema(description = "章节标题", example = "第一章")
    private String title;
}

