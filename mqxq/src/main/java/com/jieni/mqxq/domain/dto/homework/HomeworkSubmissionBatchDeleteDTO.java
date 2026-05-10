package com.jieni.mqxq.domain.dto.homework;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 作业提交批量删除DTO
 * 
 * 用于批量删除作业提交记录的参数封装
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交批量删除DTO")
public class HomeworkSubmissionBatchDeleteDTO {

    @Schema(description = "提交ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "ID列表不能为空")
    private List<@Min(value = 1, message = "ID必须大于0") Integer> ids;
}

