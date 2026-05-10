package com.jieni.mqxq.domain.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告简单视图对象
 * 
 * 用于向前端返回公告列表的简化信息
 * 不包含公告详细内容，用于列表展示
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告简单视图对象")
public class AnnouncementSimpleVO {

    /**
     * 公告ID
     */
    @Schema(description = "公告ID", example = "1")
    private Integer id;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Integer categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "系统公告")
    private String categoryName;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题", example = "系统升级维护通知")
    private String title;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间", example = "2025-01-01T12:00:00")
    private Date publishDate;

    /**
     * 是否已发布
     */
    @Schema(description = "是否已发布", example = "true")
    private Boolean isPublished;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-01T12:00:00")
    private Date createTime;
}

