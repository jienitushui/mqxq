package com.jieni.mqxq.domain.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告视图对象
 * 
 * 用于向前端返回公告信息
 * 包含公告的完整信息和API文档说明
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告视图对象")
public class AnnouncementVO {

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
     * 公告内容
     */
    @Schema(description = "公告内容", example = "系统将于今晚22:00-24:00进行升级维护...")
    private String content;

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

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Integer createUser;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-01T12:00:00")
    private Date updateTime;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1")
    private Integer updateUser;
}

