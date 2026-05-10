package com.jieni.mqxq.domain.vo.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 课程作业VO
 * 
 * 用于返回课程作业信息的视图对象，包含作业的完整信息和关联数据
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业VO")
public class CourseHomeworkVO {

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "作业内容")
    private String content;

    @Schema(description = "参考答案")
    private String answer;

    @Schema(description = "总分值")
    private Integer score;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "状态：0-未发布，1-已发布")
    private Integer status;

    @Schema(description = "创建人ID")
    private Integer createUser;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    private Integer updateUser;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "提交次数")
    private Integer submissionCount;
}

