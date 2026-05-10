package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告更新DTO
 * 
 * 用于管理员更新公告信息时接收前端参数
 * 支持部分字段更新，包含完整的参数校验
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告更新请求对象")
public class AnnouncementUpdateDTO {

    /**
     * 公告分类ID
     */
    @Schema(description = "公告分类ID", example = "1")
    @Positive(message = "公告分类ID必须大于0")
    private Integer categoryId;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题", example = "系统升级维护通知")
    @Size(min = 1, max = 100, message = "公告标题长度必须在1-100个字符之间")
    private String title;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容", example = "系统将于今晚22:00-24:00进行升级维护...")
    @Size(min = 1, max = 10000, message = "公告内容长度必须在1-10000个字符之间")
    private String content;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2025-01-01T12:00:00")
    private Date publishDate;
}

