package com.jieni.mqxq.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求DTO
 * 
 * 用于用户个人资料更新功能，支持姓名、手机号、头像等基本信息的修改
 * 所有字段都是可选的，只更新提供的字段
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "更新用户资料请求")
public class UpdateProfileRequest {

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    @Schema(description = "姓名", example = "张三")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}