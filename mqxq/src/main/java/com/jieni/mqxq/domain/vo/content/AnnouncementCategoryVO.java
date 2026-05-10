package com.jieni.mqxq.domain.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告分类视图对象
 * 
 * 用于向前端返回公告分类信息
 * 包含分类的完整信息和API文档说明
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告分类视图对象")
public class AnnouncementCategoryVO {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Integer id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "系统公告")
    private String name;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", example = "系统相关的重要公告信息")
    private String description;

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

