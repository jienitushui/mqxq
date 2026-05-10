package com.jieni.mqxq.domain.dto.chapter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 章节创建DTO
 * 用于创建新章节的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节创建DTO")
public class ChapterCreateDTO {
    
    @Schema(description = "课程ID", required = true, example = "1")
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;
    
    @Schema(description = "章节标题", required = true, example = "第一章：Java基础")
    @NotBlank(message = "章节标题不能为空")
    @Size(min = 1, max = 200, message = "章节标题长度必须在1-200之间")
    private String title;
}

