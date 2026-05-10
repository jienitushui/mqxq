package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码响应数据传输对象
 * 
 * 用于返回图形验证码相关信息，包含验证码图片、唯一标识和过期时间
 * 支持Base64格式图片传输和验证码生命周期管理，提升系统安全性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "验证码响应")
public class CaptchaResponse {

    @Schema(description = "验证码key", example = "captcha_key_123456")
    private String captchaKey;

    @Schema(description = "验证码图片Base64", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String captchaImage;

    @Schema(description = "验证码过期时间(秒)", example = "300")
    private Long expiresIn = 300L;
}