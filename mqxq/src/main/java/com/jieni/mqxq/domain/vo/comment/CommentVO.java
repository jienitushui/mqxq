package com.jieni.mqxq.domain.vo.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 课程评论VO
 * 
 * 用于前端展示的评论信息视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程评论信息")
public class CommentVO {

    @Schema(description = "评论ID", example = "1")
    private Integer id;

    @Schema(description = "课程ID", example = "1")
    private Integer courseId;

    @Schema(description = "课程名称", example = "Java基础教程")
    private String courseName;

    @Schema(description = "教师ID", example = "1")
    private Integer teacherId;

    @Schema(description = "教师名称", example = "张老师")
    private String teacherName;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "用户昵称", example = "小明")
    private String nickname;

    @Schema(description = "用户名", example = "xiaoming")
    private String userName;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "用户头像URL（扩展字段）", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "评分（1-5星）", example = "5")
    private Integer score;

    @Schema(description = "评论内容", example = "这门课程非常不错")
    private String content;

    @Schema(description = "评论状态：0-隐藏，1-显示", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

