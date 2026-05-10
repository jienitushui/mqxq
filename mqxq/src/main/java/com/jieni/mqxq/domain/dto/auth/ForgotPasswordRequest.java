package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码请求DTO
 * 
 * 用于用户忘记密码时的密码重置功能，包含邮箱验证和新密码设置
 * 通过邮箱验证码确保用户身份，无需登录即可重置密码
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    @Email(message = "用户名格式不正确（必须是邮箱格式）")
    @Schema(description = "用户名（邮箱格式）", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    @Schema(description = "用户名验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}