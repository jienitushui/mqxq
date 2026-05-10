package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 作业批改DTO
 * 
 * 用于教师批改作业时的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业批改DTO")
public class HomeworkSubmissionGradeDTO {

    @Schema(description = "成绩分数", required = true, example = "85.5")
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于0")
    @DecimalMax(value = "100", message = "成绩不能大于100")
    private BigDecimal score;

    @Schema(description = "教师评语", example = "完成良好，注意细节")
    private String teacherComment;
}

