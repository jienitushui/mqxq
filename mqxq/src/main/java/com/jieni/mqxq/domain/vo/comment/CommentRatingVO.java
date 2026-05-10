package com.jieni.mqxq.domain.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评分统计VO
 * 
 * 用于展示课程的评分统计信息（平均分、评分分布等）
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程评分统计信息")
public class CommentRatingVO {

    @Schema(description = "评论总数", example = "50")
    private Integer totalComments;

    @Schema(description = "平均评分", example = "4.5")
    private Double averageRating;

    @Schema(description = "评分分布：[1星数量, 2星数量, 3星数量, 4星数量, 5星数量]", 
            example = "[2, 3, 5, 15, 25]")
    private int[] ratingDistribution;
}

