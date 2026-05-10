package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作业提交更新DTO
 * 
 * 用于学生更新作业提交内容的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交更新DTO")
public class HomeworkSubmissionUpdateDTO {

    @Schema(description = "提交内容", example = "这是更新后的作业答案...")
    private String content;

    @Schema(description = "附件URL", example = "http://example.com/updated-file.pdf")
    private String attachmentUrl;
}

