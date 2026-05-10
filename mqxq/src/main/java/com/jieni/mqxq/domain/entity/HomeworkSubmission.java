package com.jieni.mqxq.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 作业提交实体类
 * 
 * 存储学生作业提交的完整信息，包括提交内容、附件、得分、教师评语等
 * 支持作业提交的全生命周期管理，从提交到批改完成，为在线教育平台的作业系统提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeworkSubmission implements Serializable {
    
    /**
     * 提交ID
     */
    @Schema(description = "提交ID")
    private Integer id;
    
    /**
     * 作业ID
     */
    @Schema(description = "作业ID")
    private Integer homeworkId;
    
    /**
     * 学生ID
     */
    @Schema(description = "学生ID")
    private Integer studentId;
    
    /**
     * 提交内容
     */
    @Schema(description = "提交内容")
    private String content;
    
    /**
     * 附件URL
     */
    @Schema(description = "附件URL")
    private String attachmentUrl;
    
    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
    
    /**
     * 得分
     */
    @Schema(description = "得分")
    private BigDecimal score;
    
    /**
     * 满分
     */
    @Schema(description = "满分")
    private BigDecimal maxScore;
    
    /**
     * 教师评语
     */
    @Schema(description = "教师评语")
    private String teacherComment;
    
    /**
     * 批改时间
     */
    @Schema(description = "批改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gradeTime;
    
    /**
     * 批改教师ID
     */
    @Schema(description = "批改教师ID")
    private Integer gradeUser;
    
    /**
     * 状态: 0-已提交待批改, 1-已批改
     */
    @Schema(description = "状态: 0-已提交待批改, 1-已批改")
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Integer createUser;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    /**
     * 修改者ID
     */
    @Schema(description = "修改者ID")
    private Integer updateUser;
    
    // 关联字段
    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;
    
    /**
     * 作业标题
     */
    @Schema(description = "作业标题")
    private String homeworkTitle;
}