package com.jieni.mqxq.domain.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色更新DTO
 * 
 * 用于更新已存在角色信息的数据传输对象。
 * 支持部分字段更新，包含字段验证规则，用于角色管理模块的编辑功能。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色更新DTO")
public class RoleUpdateDTO {

    @Schema(description = "角色名称", example = "管理员")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    private String roleName;

    @Schema(description = "角色代码", example = "ADMIN")
    @Size(min = 2, max = 50, message = "角色代码长度必须在2-50个字符之间")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色代码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String roleCode;
}

