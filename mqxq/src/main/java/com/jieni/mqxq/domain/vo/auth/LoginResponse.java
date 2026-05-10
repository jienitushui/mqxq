package com.jieni.mqxq.domain.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 统一登录响应数据传输对象
 * 
 * 用于返回用户登录成功后的用户信息和认证令牌等相关数据
 * 包含用户基本信息、访问令牌、角色权限等，为前端提供完整的登录响应信息
 * 支持多种用户类型的统一响应格式，确保登录数据的一致性和安全性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Schema(description = "统一登录响应")
public class LoginResponse {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin || admin@example.com")
    private String username;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "手机号码", example = "181****1234")
    private String phone;

    @Schema(description = "头像URL", example = "http://localhost:9000/mqxq/avatar/default.png")
    private String avatar;

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "令牌过期时间(秒)", example = "2592000")
    private Long expiresIn;

    @Schema(description = "角色列表", example = "[\"admin\", \"user\"]")
    private List<String> roles;
}