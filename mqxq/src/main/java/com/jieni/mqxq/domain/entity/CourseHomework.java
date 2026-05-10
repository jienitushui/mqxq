package com.jieni.mqxq.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程作业实体类
 * 
 * 存储课程作业的完整信息，包括作业标题、内容、参考答案、分值、时间设置等
 * 支持作业的发布控制、时间管理、成绩管理等功能，为在线教育平台的作业系统提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseHomework implements Serializable {
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
     * 作业标题
     */
    @Schema(description = "作业标题")
    private String title;
    /**
     * 作业内容
     */
    @Schema(description = "作业内容")
    private String content;
    /**
     * 参考答案
     */
    @Schema(description = "参考答案")
    private String answer;
    /**
     * 总分值
     */
    @Schema(description = "总分值")
    private BigDecimal score;
    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Date startTime;
    /**
     * 截止时间
     */
    @Schema(description = "截止时间")
    private Date endTime;
    /**
     * 状态：0-未发布，1-已发布
     */
    @Schema(description = "状态：0-未发布，1-已发布")
    private Integer status;
    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Integer createUser;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    /**
     * 修改人ID
     */
    @Schema(description = "修改人ID")
    private Integer updateUser;
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
      * 提交次数
      */
    @Schema(description = "提交次数")
    private String submissionCount;
}

