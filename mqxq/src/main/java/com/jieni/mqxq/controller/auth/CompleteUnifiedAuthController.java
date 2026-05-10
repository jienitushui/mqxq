package com.jieni.mqxq.controller.auth;

import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.auth.*;
import com.jieni.mqxq.domain.vo.auth.LoginResponse;
import com.jieni.mqxq.service.auth.UnifiedAuthService;
import com.jieni.mqxq.util.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 完整的统一认证控制器
 * 
 * 提供用户认证和授权相关的所有API接口，包括登录、注册、密码管理、邮箱验证等功能
 * 所有业务逻辑已下沉到Service层，Controller仅负责接收请求和返回响应
 * 
 * 主要功能模块：
 * - 用户登录登出管理
 * - 用户注册和资料更新
 * - 密码修改和重置
 * - 邮箱验证码发送和验证
 * - 图形验证码生成
 * - 用户名可用性检查
 * - 登录状态检查
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "统一认证接口", description = "提供登录、注册、密码管理等认证功能")
@Validated
public class CompleteUnifiedAuthController {

    @Resource
    private UnifiedAuthService unifiedAuthService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名（邮箱）登录，需要验证图形验证码")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("用户登录请求，用户名：{}", request.getUsername());
        String ipAddress = IpUtils.getIpAddr();
        LoginResponse response = unifiedAuthService.login(request, ipAddress);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，需要先获取图形验证码和邮箱验证码")
    public Result<Void> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        log.info("用户注册请求，用户名：{}", request.getUsername());
        String ipAddress = IpUtils.getIpAddr();
        unifiedAuthService.register(request, ipAddress);
        return Result.success("注册成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<LoginResponse> getCurrentUserInfo() {
        log.debug("获取当前用户信息");
        LoginResponse response = unifiedAuthService.getCurrentUserInfo();
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录，清除会话")
    public Result<Void> logout() {
        log.info("用户登出");
        boolean success = unifiedAuthService.logout();
        if (success) {
            return Result.success("登出成功");
        } else {
            return Result.error("登出失败");
        }
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "刷新访问令牌以延长会话有效期")
    public Result<LoginResponse> refreshToken() {
        log.debug("刷新令牌");
        LoginResponse response = unifiedAuthService.refreshToken();
        return Result.success(response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的登录密码，需要验证旧密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("用户修改密码");
        unifiedAuthService.changePassword(request);
        return Result.success("密码修改成功");
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-email-code")
    @Operation(summary = "发送验证码", description = "发送验证码用于注册或重置密码，同一用户名60秒内只能发送一次。用户名为邮箱格式")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        log.info("发送验证码，用户名：{}，类型：{}", request.getUsername(), request.getType());
        unifiedAuthService.sendEmailCode(request);
        return Result.success("验证码已发送到您的邮箱，请查收");
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "通过用户名验证码重置密码，无需登录。用户名为邮箱格式")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("忘记密码，用户名：{}", request.getUsername());
        unifiedAuthService.forgotPassword(request);
        return Result.success("密码重置成功");
    }

    /**
     * 更新用户资料
     */
    @PostMapping("/update-profile")
    @Operation(summary = "更新用户资料", description = "更新用户基本信息（姓名、手机号、头像）")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户资料");
        unifiedAuthService.updateProfile(request);
        return Result.success("资料更新成功");
    }

    /**
     * 更换用户名
     */
    @PostMapping("/bind-email")
    @Operation(summary = "更换用户名", description = "更换用户名，需要验证码。用户名为邮箱格式")
    public Result<Void> bindEmail(@Valid @RequestBody BindEmailRequest request) {
        log.info("更换用户名，新用户名：{}", request.getUsername());
        unifiedAuthService.bindEmail(request);
        return Result.success("用户名更换成功");
    }

    /**
     * 检查用户名可用性
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名可用性", description = "检查用户名（邮箱）是否已被使用。注意：在本系统中，用户名就是邮箱")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名（邮箱）", required = true, in = ParameterIn.QUERY, example = "user@example.com")
            @RequestParam @NotBlank(message = "用户名不能为空") @Email(message = "用户名格式不正确（应为邮箱格式）") String username) {
        log.debug("检查用户名可用性，用户名：{}", username);
        boolean available = unifiedAuthService.checkUsernameAvailability(username);
        return Result.success(available);
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check-login-status")
    @Operation(summary = "检查登录状态", description = "检查当前用户是否处于登录状态")
    public Result<LoginResponse> checkLoginStatus() {
        log.debug("检查登录状态");
        LoginResponse response = unifiedAuthService.checkLoginStatus();
        return Result.success(response);
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取图形验证码，用于防止机器人攻击，验证码有效期5分钟")
    public Result<CaptchaResponse> getCaptcha() {
        log.debug("获取图形验证码");
        CaptchaResponse response = unifiedAuthService.generateCaptcha();
        return Result.success(response);
    }
}
