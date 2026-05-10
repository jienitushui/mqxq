package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 发送验证码数据传输对象
 * 
 * 用于注册时发送验证码到用户邮箱的功能，包含收件人邮箱和验证码内容
 * 支持邮箱地址验证和验证码生成，为用户注册和密码重置等场景提供邮箱验证支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendCodeDto {
    @Schema(description = "收件人邮箱")
    private String receiver;
    @Schema(description = "验证码")
    private String code;
    @Schema(description = "邮件主题")
    private String subject;
    @Schema(description = "邮件内容")
    private String content;
}
