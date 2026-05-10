package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送邮箱验证码请求数据传输对象
 * 
 * 用于发送邮箱验证码功能，支持注册和密码重置等多种场景的验证码发送
 * 提供邮箱格式校验和验证码类型区分，确保邮箱验证的准确性和安全性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "发送邮箱验证码请求")
public class SendEmailCodeRequest {

    @NotBlank(message = "用户名不能为空")
    @Email(message = "用户名格式不正确（必须是邮箱格式）")
    @Schema(description = "用户名（邮箱格式）", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(REGISTER|RESET_PASSWORD|BIND_EMAIL)$", message = "无效的验证码类型，只支持：REGISTER、RESET_PASSWORD、BIND_EMAIL")
    @Schema(description = "类型：REGISTER-注册，RESET_PASSWORD-重置密码，BIND_EMAIL-绑定邮箱", example = "REGISTER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
}