package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程浏览记录实体类
 * 
 * 存储用户浏览课程的记录信息，包括浏览时间、IP地址、用户代理等
 * 支持课程浏览统计、用户行为分析和数据统计，为在线教育平台的数据分析提供支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseView implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键ID")
    private Integer id;
    /**
     * 课程ID
     */
    @Schema(description = "课程ID")
    private Integer courseId;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;
    /**
     * 浏览时间
     */
    @Schema(description = "浏览时间")
    private Date viewTime;
    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;
    /**
     * 用户代理
     */
    @Schema(description = "用户代理")
    private String userAgent;
    /**
     * 记录创建时间
     */
    @Schema(description = "记录创建时间")
    private Date createTime;

}

