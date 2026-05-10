package com.jieni.mqxq.domain.vo.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作业提交VO
 * 
 * 用于作业提交列表展示的视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交VO")
public class HomeworkSubmissionVO {

    @Schema(description = "提交ID", example = "1")
    private Integer id;

    @Schema(description = "作业ID", example = "1")
    private Integer homeworkId;

    @Schema(description = "作业标题", example = "Java基础作业")
    private String homeworkTitle;

    @Schema(description = "学生ID", example = "1")
    private Integer studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "提交内容", example = "这是我的作业答案...")
    private String content;

    @Schema(description = "附件URL", example = "http://example.com/file.pdf")
    private String attachmentUrl;

    @Schema(description = "提交时间", example = "2025-10-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    @Schema(description = "成绩", example = "85.5")
    private BigDecimal score;

    @Schema(description = "最高分", example = "100")
    private BigDecimal maxScore;

    @Schema(description = "教师评语", example = "完成良好")
    private String teacherComment;

    @Schema(description = "批改时间", example = "2025-10-14 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gradeTime;

    @Schema(description = "批改教师ID", example = "2")
    private Integer gradeUser;

    @Schema(description = "状态：0-待批改，1-已批改", example = "0")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-10-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2025-10-13 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

