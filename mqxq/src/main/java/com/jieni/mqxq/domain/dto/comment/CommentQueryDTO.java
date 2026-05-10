package com.jieni.mqxq.domain.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评论查询DTO
 * 
 * 用于多角色（管理员、教师、用户）的评论查询，支持多维度筛选和分页
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程评论查询条件")
public class CommentQueryDTO {

    @Schema(description = "页码，默认1", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小，默认10", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "用户ID", example = "1")
    @Min(value = 1, message = "用户ID必须大于0")
    private Integer userId;

    @Schema(description = "教师ID", example = "1")
    @Min(value = 1, message = "教师ID必须大于0")
    private Integer teacherId;

    @Schema(description = "评分（1-5星）", example = "5")
    @Min(value = 1, message = "评分必须在1-5之间")
    private Integer score;

    @Schema(description = "评论状态：0-隐藏，1-显示", example = "1")
    private Integer status;

    @Schema(description = "关键词搜索", example = "很不错")
    private String keyword;

    @Schema(description = "排序方式：time-按时间，score-按评分", example = "time")
    private String orderBy = "time";

    @Schema(description = "限制数量（用于获取最新、热门评论）", example = "10")
    @Min(value = 1, message = "限制数量必须大于0")
    private Integer limit;

    @Schema(description = "分析天数（用于趋势分析）", example = "30")
    @Min(value = 1, message = "分析天数必须大于0")
    private Integer days = 30;
}

