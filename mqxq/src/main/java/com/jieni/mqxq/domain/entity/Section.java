package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程小节实体类
 * 
 * 存储课程小节的完整信息，包括小节内容、视频资源、时长、发布状态等。
 * 作为课程内容的最小单元，支持视频教学、内容管理、状态控制等功能。为在线学习平台的核心内容实体。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Section implements Serializable {
    /**
     * 小节ID
     */
    @Schema(description = "小节ID")
    private Integer id;
    /**
     * 章节ID
     */
    @Schema(description = "章节ID")
    private Integer chapterId;
    /**
     * 课程ID
     */
    @Schema(description = "课程ID")
    private Integer courseId;
    /**
     * 小节名称
     */
    @Schema(description = "小节名称")
    private String title;
    /**
     * 小节内容
     */
    @Schema(description = "小节内容")
    private String content;
    /**
     * 视频url
     */
    @Schema(description = "视频url")
    private String videoUrl;
    /**
     * 小节时长(秒)
     */
    @Schema(description = "小节时长(秒)")
    private Integer duration;
    /**
     * 状态 0未发布 1已发布
     */
    @Schema(description = "状态 0未发布 1已发布")
    private Integer status;
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

