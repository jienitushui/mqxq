package com.jieni.mqxq.domain.vo.course;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 我的课程视图对象
 * 
 * 用于前端展示课程学习信息，包含用户、课程和学习进度等详细信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的课程视图对象")
public class MyCourseVO {

    @Schema(description = "我的课程记录ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "学习状态：0-已加入，1-学习中，2-已完成")
    private Integer status;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程封面")
    private String courseCover;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

