package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 章节实体类
 * 
 * 存储课程章节的基本信息，包括章节标题、所属课程、创建信息等。
 * 作为课程内容组织的中间层级，连接课程和小节，支持分层级的课程结构管理。为在线教育平台的课程组织实体。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chapter implements Serializable {
    /**
     * 主键章节ID
     */
    @Schema(description = "主键章节ID")
    private Integer id;
    /**
     * 课程ID
     */
    @Schema(description = "课程ID")
    private Integer courseId;
    /**
     * 章节名称
     */
    @Schema(description = "章节名称")
    private String title;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 创建者id
     */
    @Schema(description = "创建者id")
    private Integer createUser;
    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;
    /**
     * 修改者id
     */
    @Schema(description = "修改者id")
    private Integer updateUser;

}

