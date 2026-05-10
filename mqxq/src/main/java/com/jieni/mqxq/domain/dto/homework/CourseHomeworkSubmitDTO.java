package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程作业提交DTO
 * 
 * 用于学生提交作业答案的数据传输对象
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程作业提交DTO")
public class CourseHomeworkSubmitDTO {

    @Schema(description = "作业ID", required = true, example = "1")
    @NotNull(message = "作业ID不能为空")
    @Min(value = 1, message = "作业ID必须大于0")
    private Integer homeworkId;

    @Schema(description = "作业答案", required = true)
    @NotBlank(message = "作业答案不能为空")
    @Size(max = 10000, message = "作业答案长度不能超过10000个字符")
    private String content;

    @Schema(description = "附件URL")
    @Size(max = 500, message = "附件URL长度不能超过500个字符")
    private String attachmentUrl;
}

