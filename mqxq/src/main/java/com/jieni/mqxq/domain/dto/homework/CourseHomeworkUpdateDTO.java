package com.jieni.mqxq.domain.dto.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 课程作业更新DTO
 * 
 * 用于更新课程作业信息的数据传输对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业更新DTO")
public class CourseHomeworkUpdateDTO {

    @Schema(description = "作业ID", required = true, example = "1")
    @NotNull(message = "作业ID不能为空")
    @Min(value = 1, message = "作业ID必须大于0")
    private Integer id;

    @Schema(description = "课程ID", example = "1")
    @Min(value = 1, message = "课程ID必须大于0")
    private Integer courseId;

    @Schema(description = "作业标题", example = "第一章作业")
    @Size(max = 100, message = "作业标题长度不能超过100个字符")
    private String title;

    @Schema(description = "作业内容")
    @Size(max = 5000, message = "作业内容长度不能超过5000个字符")
    private String content;

    @Schema(description = "参考答案")
    @Size(max = 5000, message = "参考答案长度不能超过5000个字符")
    private String answer;

    @Schema(description = "总分值", example = "100")
    @Min(value = 1, message = "总分值必须大于0")
    @Max(value = 1000, message = "总分值不能超过1000")
    private Integer score;

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "截止时间", example = "2025-01-07 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "状态：0-未发布，1-已发布", example = "0")
    private Integer status;
}

