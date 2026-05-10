package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告搜索DTO
 * 
 * 用于公告搜索时接收前端参数
 * 包含搜索关键词和分页参数
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告搜索请求对象")
public class AnnouncementSearchDTO {

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词", required = true, example = "系统")
    @NotBlank(message = "搜索关键词不能为空")
    @Size(min = 1, max = 50, message = "搜索关键词长度必须在1-50个字符之间")
    private String keyword;

    /**
     * 页码
     */
    @Schema(description = "页码，从1开始", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 50, message = "每页大小不能超过50")
    private Integer size = 10;
}

