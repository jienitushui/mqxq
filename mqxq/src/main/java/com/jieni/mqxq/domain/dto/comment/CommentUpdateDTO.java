package com.jieni.mqxq.domain.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评论更新DTO
 * 
 * 用于用户修改自己的评论
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新课程评论请求")
public class CommentUpdateDTO {

    @Schema(description = "评论内容", required = true, example = "这门课程非常不错，老师讲解清晰")
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 5, max = 500, message = "评论内容长度必须在5-500字之间")
    private String content;

    @Schema(description = "评分（1-5星）", required = true, example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在1-5星之间")
    @Max(value = 5, message = "评分必须在1-5星之间")
    private Integer score;
}

