package com.jieni.mqxq.domain.vo.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程作业提交记录VO
 * 
 * 用于返回作业提交记录的视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业提交记录VO")
public class CourseHomeworkSubmissionVO {

    @Schema(description = "提交记录ID")
    private Integer id;

    @Schema(description = "作业ID")
    private Integer homeworkId;

    @Schema(description = "作业标题")
    private String homeworkTitle;

    @Schema(description = "学生ID")
    private Integer studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "提交内容")
    private String content;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "得分")
    private BigDecimal score;

    @Schema(description = "总分")
    private Integer maxScore;

    @Schema(description = "教师评语")
    private String teacherComment;

    @Schema(description = "状态：0-待批改，1-已批改")
    private Integer status;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    @Schema(description = "批改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gradeTime;

    @Schema(description = "批改教师ID")
    private Integer gradeUser;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

