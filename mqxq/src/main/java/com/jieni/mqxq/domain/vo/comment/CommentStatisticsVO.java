package com.jieni.mqxq.domain.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评论统计VO
 * 
 * 用于展示评论的统计信息（总数、状态分布等）
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论统计信息")
public class CommentStatisticsVO {

    @Schema(description = "评论总数", example = "100")
    private Long totalComments;

    @Schema(description = "显示状态评论数", example = "80")
    private Long visibleComments;

    @Schema(description = "隐藏状态评论数", example = "20")
    private Long hiddenComments;

    @Schema(description = "今日新增评论数", example = "5")
    private Long todayComments;

    @Schema(description = "系统平均评分", example = "4.5")
    private Double averageRating;

    @Schema(description = "最近7天评论数", example = "15")
    private Long recentComments;

    @Schema(description = "涉及课程数量", example = "10")
    private Integer totalCourses;
}

