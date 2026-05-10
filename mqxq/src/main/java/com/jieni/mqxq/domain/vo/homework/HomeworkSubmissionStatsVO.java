package com.jieni.mqxq.domain.vo.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 作业提交统计VO
 * 
 * 用于作业提交统计信息展示的视图对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交统计VO")
public class HomeworkSubmissionStatsVO {

    @Schema(description = "总提交数", example = "50")
    private Long totalSubmissions;

    @Schema(description = "已批改数量", example = "30")
    private Long gradedCount;

    @Schema(description = "待批改数量", example = "20")
    private Long ungradedCount;

    @Schema(description = "平均分", example = "85.5")
    private BigDecimal avgScore;

    @Schema(description = "最高分", example = "98.0")
    private BigDecimal maxScore;

    @Schema(description = "最低分", example = "60.0")
    private BigDecimal minScore;

    @Schema(description = "及格率（%）", example = "90.0")
    private BigDecimal passRate;

    @Schema(description = "优秀率（%）", example = "40.0")
    private BigDecimal excellentRate;
}

