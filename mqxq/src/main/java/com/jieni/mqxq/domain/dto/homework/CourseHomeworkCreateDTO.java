package com.jieni.mqxq.domain.dto.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 课程作业创建DTO
 * 
 * 用于创建课程作业的数据传输对象，包含作业的基本信息和时间设置
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业创建DTO")
public class CourseHomeworkCreateDTO {

    @Schema(description = "课程ID", required = true, example = "1")
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "作业标题", required = true, example = "第一章作业")
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 100, message = "作业标题长度不能超过100个字符")
    private String title;

    @Schema(description = "作业内容", required = true)
    @NotBlank(message = "作业内容不能为空")
    @Size(max = 5000, message = "作业内容长度不能超过5000个字符")
    private String content;

    @Schema(description = "参考答案")
    @Size(max = 5000, message = "参考答案长度不能超过5000个字符")
    private String answer;

    @Schema(description = "总分值", required = true, example = "100")
    @NotNull(message = "总分值不能为空")
    @Min(value = 1, message = "总分值必须大于0")
    @Max(value = 100, message = "总分值不能超过100")
    private Integer score;

    @Schema(description = "开始时间", required = true, example = "2025-01-01 00:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "截止时间", required = true, example = "2025-01-07 23:59:59")
    @NotNull(message = "截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "状态：0-未发布，1-已发布", example = "0")
    private Integer status = 0;
}

