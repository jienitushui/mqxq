package com.jieni.mqxq.domain.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的课程查询DTO
 * 
 * 用于查询我的课程列表时的条件封装，支持分页查询和多条件筛选
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的课程查询条件")
public class MyCourseQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    @Schema(description = "用户ID")
    @Min(value = 1, message = "用户ID必须大于0")
    private Integer userId;

    @Schema(description = "课程ID")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "订单ID")
    @Min(value = 1, message = "订单ID必须大于0")
    private Integer orderId;

    @Schema(description = "学习状态：0-已加入，1-学习中，2-已完成", example = "0")
    private Integer status;

    @Schema(description = "用户昵称（模糊查询）")
    private String userNickname;

    @Schema(description = "课程名称（模糊查询）")
    private String courseName;

    @Schema(description = "学员姓名（模糊查询）")
    private String studentName;

    @Schema(description = "教师ID")
    @Min(value = 1, message = "教师ID必须大于0")
    private Integer teacherId;
}

