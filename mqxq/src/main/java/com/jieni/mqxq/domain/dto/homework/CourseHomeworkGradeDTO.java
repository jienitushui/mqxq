package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课程作业批改DTO
 * 
 * 用于教师批改学生作业的数据传输对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业批改DTO")
public class CourseHomeworkGradeDTO {

    @Schema(description = "提交记录ID", required = true, example = "1")
    @NotNull(message = "提交记录ID不能为空")
    @Min(value = 1, message = "提交记录ID必须大于0")
    private Integer submissionId;

    @Schema(description = "得分", required = true, example = "85")
    @NotNull(message = "得分不能为空")
    @DecimalMin(value = "0", message = "得分不能小于0")
    @DecimalMax(value = "1000", message = "得分不能超过1000")
    private BigDecimal score;

    @Schema(description = "教师评语")
    @Size(max = 2000, message = "教师评语长度不能超过2000个字符")
    private String teacherComment;
}

