package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告分页查询DTO
 * 
 * 用于公告的分页查询和条件筛选
 * 支持按标题、分类等条件进行搜索
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告分页查询请求对象")
public class AnnouncementPageQueryDTO {

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
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    /**
     * 公告标题（模糊查询）
     */
    @Schema(description = "公告标题（模糊查询）", example = "系统")
    @Size(max = 100, message = "公告标题长度不能超过100个字符")
    private String title;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    @Positive(message = "分类ID必须大于0")
    private Integer categoryId;
}

