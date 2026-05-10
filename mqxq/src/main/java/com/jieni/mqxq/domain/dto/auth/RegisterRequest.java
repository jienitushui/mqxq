package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 
 * 用于用户注册功能的数据传输对象
 * 注意：在本系统中，用户名就是邮箱，不需要单独的username字段
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Email(message = "用户名格式不正确（必须是邮箱格式）")
    @Schema(description = "用户名（邮箱格式）", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    @Schema(description = "图形验证码", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captcha;

    @NotBlank(message = "图形验证码key不能为空")
    @Schema(description = "图形验证码key", example = "captcha_key_123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaKey;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    @Schema(description = "用户名验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "^(USER|TEACHER)$", message = "用户类型只能是USER或TEACHER")
    @Schema(description = "用户类型：USER-普通用户, TEACHER-教师", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userType;
}