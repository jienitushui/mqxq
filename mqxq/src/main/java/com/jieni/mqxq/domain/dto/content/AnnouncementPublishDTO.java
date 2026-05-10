package com.jieni.mqxq.domain.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告发布DTO
 * 
 * 用于发布公告时接收前端参数
 * 包含可选的发布时间设置
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告发布请求对象")
public class AnnouncementPublishDTO {

    /**
     * 发布时间（可选，为空则使用当前时间）
     */
    @Schema(description = "发布时间，为空则使用当前时间", example = "2025-01-01T12:00:00")
    private Date publishDate;
}

