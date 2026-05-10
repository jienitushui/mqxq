package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更换用户名请求DTO
 * 
 * 用于用户更换用户名功能，通过验证码确保操作安全
 * 注意：在本系统中，用户名必须是邮箱格式
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "更换用户名请求")
public class BindEmailRequest {

    @NotBlank(message = "新用户名不能为空")
    @Email(message = "新用户名格式不正确（必须是邮箱格式）")
    @Schema(description = "新用户名（邮箱格式）", example = "new-user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    @Schema(description = "用户名验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}