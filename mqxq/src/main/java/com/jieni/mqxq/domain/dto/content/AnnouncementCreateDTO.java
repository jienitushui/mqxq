package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告创建DTO
 * 
 * 用于管理员创建新的公告时接收前端参数
 * 包含完整的参数校验和API文档说明
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告创建请求对象")
public class AnnouncementCreateDTO {

    /**
     * 公告分类ID
     */
    @Schema(description = "公告分类ID", required = true, example = "1")
    @NotNull(message = "公告分类ID不能为空")
    @Positive(message = "公告分类ID必须大于0")
    private Integer categoryId;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题", required = true, example = "系统升级维护通知")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 1, max = 100, message = "公告标题长度必须在1-100个字符之间")
    private String title;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容", required = true, example = "系统将于今晚22:00-24:00进行升级维护...")
    @NotBlank(message = "公告内容不能为空")
    @Size(min = 1, max = 10000, message = "公告内容长度必须在1-10000个字符之间")
    private String content;

    /**
     * 发布时间（可选，为空则表示创建草稿）
     */
    @Schema(description = "发布时间，为空则保存为草稿", example = "2025-01-01T12:00:00")
    private Date publishDate;
}

