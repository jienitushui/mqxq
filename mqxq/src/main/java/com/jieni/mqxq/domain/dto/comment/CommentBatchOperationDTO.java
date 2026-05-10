package com.jieni.mqxq.domain.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程评论批量操作DTO
 * 
 * 用于管理员批量操作评论（批量删除、批量显示、批量隐藏等）
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量操作评论请求")
public class CommentBatchOperationDTO {

    @Schema(description = "评论ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "评论ID列表不能为空")
    private List<@NotNull(message = "评论ID不能为null") Integer> commentIds;
}

