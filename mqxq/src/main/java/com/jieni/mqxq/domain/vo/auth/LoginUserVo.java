package com.jieni.mqxq.domain.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户视图对象
 * 
 * 封装用户登录后的基本信息和认证状态，用于前端显示和权限控制
 * 包含用户身份信息、认证令牌和管理员权限标识，支持用户会话管理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginUserVo {
    @Schema(description = "用户id",hidden = true)
    private Integer id;
    @Schema(description = "账号（邮箱格式）",hidden = true)
    private String username;
    @Schema(description = "token",hidden = true)
    private String token;
    @Schema(description = "用户昵称",hidden = true)
    private String name;
    @Schema(description = "密码，（返回时不展示）",hidden = true)
    private String password;
    @Schema(description = "是否为管理员")
    private Boolean isAdmin;
}
