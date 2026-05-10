package com.jieni.mqxq.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除DTO
 * 
 * 用于批量删除用户时的数据传输对象
 * 提供ID列表的统一验证，确保批量操作的安全性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量删除DTO")
public class BatchDeleteDTO {

    @Schema(description = "用户ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID列表不能为空")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Integer> ids;
}

