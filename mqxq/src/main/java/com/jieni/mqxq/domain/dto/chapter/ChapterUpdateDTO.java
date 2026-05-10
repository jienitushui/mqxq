package com.jieni.mqxq.domain.dto.chapter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 章节更新DTO
 * 用于更新章节信息的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "章节更新DTO")
public class ChapterUpdateDTO {
    
    @Schema(description = "章节标题", example = "第一章：Java进阶")
    @Size(min = 1, max = 200, message = "章节标题长度必须在1-200之间")
    private String title;
    
    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;
}

