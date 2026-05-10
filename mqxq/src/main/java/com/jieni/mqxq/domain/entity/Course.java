package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程实体类
 * 
 * 存储课程的完整信息，包括课程基本信息、价格、状态、统计数据等。
 * 支持课程的分类管理、教师关联、发布状态控制等功能。为在线教育平台的核心业务实体。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键ID")
    private Integer id;
    /**
     * 课程教师ID
     */
    @Schema(description = "课程教师ID")
    private Integer teacherId;
    /**
     * 课程分类ID
     */
    @Schema(description = "课程分类ID")
    private Integer subjectId;
    /**
     * 课程标题
     */
    @Schema(description = "课程标题")
    private String title;
    /**
     * 课程简介
     */
    @Schema(description = "课程简介")
    private String description;
    /**
     * 课程销售价格，设置为0则可免费观看
     */
    @Schema(description = "课程销售价格，设置为0则可免费观看")
    private BigDecimal price;
    /**
     * 总课时
     */
    @Schema(description = "总课时")
    private Integer lessonNum;
    /**
     * 视频总时长（秒）
     */
    @Schema(description = "视频总时长（秒）")
    private Integer durationSum;
    /**
     * 课程封面图片路径
     */
    @Schema(description = "课程封面图片路径")
    private String cover;
    /**
     * 销售数量
     */
    @Schema(description = "销售数量")
    private Integer buyCount;
    /**
     * 浏览数量
     */
    @Schema(description = "浏览数量")
    private Integer viewCount;
    /**
     * 课程状态 0未发布 1已发布
     */
    @Schema(description = "课程状态 0未发布 1已发布")
    private Integer status;
    /**
     * 课程发布时间
     */
    @Schema(description = "课程发布时间")
    private Date publishTime;
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

