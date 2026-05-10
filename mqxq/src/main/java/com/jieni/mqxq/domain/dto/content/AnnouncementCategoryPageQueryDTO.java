package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告分类分页查询DTO
 * 
 * 用于公告分类的分页查询和条件筛选
 * 包含分页参数和查询条件
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告分类分页查询请求对象")
public class AnnouncementCategoryPageQueryDTO {

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
     * 分类名称（模糊查询）
     */
    @Schema(description = "分类名称（模糊查询）", example = "系统")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
}

