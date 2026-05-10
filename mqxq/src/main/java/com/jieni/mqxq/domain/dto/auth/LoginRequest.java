package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 统一登录请求传输对象
 * 
 * 封装用户登录所需的全部参数，包括用户名、密码、验证码等
 * 支持多种登录方式和角色选择，提供完整的参数校验
 * 适用于管理员、教师、学生等多种角色的统一登录
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "统一登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @NotBlank(message = "验证码key不能为空")
    @Schema(description = "验证码key", example = "captcha_key_123")
    private String captchaKey;

    @Schema(description = "角色", example = "user")
    private String role;
}