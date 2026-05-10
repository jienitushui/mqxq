package com.jieni.mqxq.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户状态DTO
 * 
 * 用于管理员启用或禁用用户账户时的数据传输对象
 * 提供用户账户状态的精确控制，支持批量状态更新的扩展
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户状态DTO")
public class UpdateUserStatusDTO {

    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Integer id;

    @Schema(description = "状态 1:启用 0:禁用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    private Integer status;
}

