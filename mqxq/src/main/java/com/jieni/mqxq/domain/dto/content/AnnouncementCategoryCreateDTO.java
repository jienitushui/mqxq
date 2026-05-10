package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告分类创建DTO
 * 
 * 用于管理员创建新的公告分类时接收前端参数
 * 包含完整的参数校验和API文档说明
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告分类创建请求对象")
public class AnnouncementCategoryCreateDTO {

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", required = true, example = "系统公告")
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
    private String name;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", example = "系统相关的重要公告信息")
    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;
}

