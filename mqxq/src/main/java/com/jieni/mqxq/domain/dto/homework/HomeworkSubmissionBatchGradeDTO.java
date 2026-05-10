package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 作业批量批改DTO
 * 
 * 用于教师批量批改作业的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业批量批改DTO")
public class HomeworkSubmissionBatchGradeDTO {

    @Schema(description = "批改记录列表", required = true)
    @NotEmpty(message = "批改记录不能为空")
    @Valid
    private List<GradeItem> gradings;

    /**
     * 批改项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批改项")
    public static class GradeItem {

        @Schema(description = "学生ID", required = true, example = "1")
        @NotNull(message = "学生ID不能为空")
        @Min(value = 1, message = "学生ID必须大于0")
        private Integer studentId;

        @Schema(description = "成绩分数", required = true, example = "85.5")
        @NotNull(message = "成绩不能为空")
        private BigDecimal score;

        @Schema(description = "教师评语", example = "完成良好")
        private String teacherComment;
    }
}

