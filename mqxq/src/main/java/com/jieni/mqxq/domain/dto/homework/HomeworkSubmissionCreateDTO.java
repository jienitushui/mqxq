package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作业提交创建DTO
 * 
 * 用于学生提交作业时的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交创建DTO")
public class HomeworkSubmissionCreateDTO {

    @Schema(description = "作业ID", required = true, example = "1")
    @NotNull(message = "作业ID不能为空")
    @Min(value = 1, message = "作业ID必须大于0")
    private Integer homeworkId;

    @Schema(description = "提交内容", example = "这是我的作业答案...")
    private String content;

    @Schema(description = "附件URL", example = "http://example.com/file.pdf")
    private String attachmentUrl;
}

