package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程评论实体类
 * 
 * 表示课程评论的详细信息，包括评分、评论内容、用户信息等
 * 支持评论的状态管理、用户信息关联以及评分统计
 * 实现课程评价系统的完整业务支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseComment implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Integer id;
    /**
     * 课程ID
     */
    @Schema(description = "课程ID")
    private Integer courseId;
    /**
     * 讲师ID
     */
    @Schema(description = "讲师ID")
    private Integer teacherId;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;
    /**
     * 评分1-5星
     */
    @Schema(description = "评分1-5星")
    private Integer score;
    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;
    /**
     * 状态：0-隐藏，1-显示
     */
    @Schema(description = "状态：0-隐藏，1-显示")
    private Integer status;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;


    /**
     * 课程名称
     */
    @Schema(description = "课程名称")
    private String courseName;
    /**
     * 讲师名称
     */
    @Schema(description = "讲师名称")
    private String teacherName;
    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;
    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

}

