package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 我的课程实体类
 * 
 * 存储用户与课程的关联关系信息，包括学习状态、订单信息等
 * 支持课程学习进度跟踪、学习状态管理和购买记录关联，为在线教育平台的学习管理提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyCourse implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Integer id;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;
    /**
     * 课程ID
     */
    @Schema(description = "课程ID")
    private Integer courseId;
    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private Integer orderId;
    /**
     * 学习状态：0-已加入，1-学习中，2-已完成
     */
    @Schema(description = "学习状态：0-已加入，1-学习中，2-已完成")
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
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userNickname;
    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;
    /**
     * 课程名称
     */
    @Schema(description = "课程名称")
    private String courseName;
    /**
     * 课程封面
     */
    @Schema(description = "课程封面")
    private String courseCover;
    /**
     * 讲师名称
     */
    @Schema(description = "讲师名称")
    private String teacherName;
}

