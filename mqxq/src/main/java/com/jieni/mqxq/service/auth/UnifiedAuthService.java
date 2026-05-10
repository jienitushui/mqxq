package com.jieni.mqxq.service.auth;

import com.jieni.mqxq.domain.dto.auth.*;
import com.jieni.mqxq.domain.vo.auth.LoginResponse;

/**
 * 统一认证服务接口
 * 
 * 提供用户认证和授权相关的核心业务逻辑，包括登录、注册、密码管理、邮箱验证等功能
 * 支持多角色用户系统，提供完整的安全验证和会话管理机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface UnifiedAuthService {
    
    /**
     * 用户登录
     * 
     * 验证用户凭证并创建会话，支持用户名（邮箱）登录
     * 包含验证码校验、登录失败限制和IP锁定机制
     * 
     * @param request 登录请求DTO，包含用户名、密码、验证码等
     * @param ipAddress 用户IP地址，用于安全审计和限制
     * @return LoginResponse 登录响应，包含用户信息和Token
     */
    LoginResponse login(LoginRequest request, String ipAddress);
    
    
    /**
     * 用户注册
     * 
     * 创建新用户账户，验证用户名验证码、用户名唯一性等
     * 自动根据userType分配默认角色（USER或TEACHER）和权限
     * 注意：用户名即邮箱格式
     * 
     * @param request 注册请求DTO，包含用户名（邮箱）、密码、验证码等
     * @param ipAddress 用户IP地址，用于安全审计
     */
    void register(RegisterRequest request, String ipAddress);
    
    /**
     * 获取当前用户信息
     * 
     * 根据当前会话获取登录用户的详细信息
     * 
     * @return LoginResponse 用户信息响应，包含用户详情和权限
     */
    LoginResponse getCurrentUserInfo();
    
    
    /**
     * 用户登出
     * 
     * 清除用户会话和Token，退出登录状态
     * 
     * @return boolean 是否成功登出
     */
    boolean logout();
    
    /**
     * 刷新令牌
     * 
     * 刷新访问令牌以延长会话有效期
     * 
     * @return LoginResponse 新的登录响应，包含更新后的Token
     */
    LoginResponse refreshToken();
    
    /**
     * 修改密码
     * 
     * 用户修改自己的登录密码，需要验证旧密码
     * 
     * @param changePasswordRequest 修改密码请求DTO
     */
    void changePassword(ChangePasswordRequest changePasswordRequest);
    
    /**
     * 发送验证码
     * 
     * 向指定用户名（邮箱格式）发送验证码，支持注册、重置密码、更换用户名等场景
     * 同一用户名60秒内只能发送一次
     * 
     * @param sendEmailCodeRequest 发送验证码请求DTO
     */
    void sendEmailCode(SendEmailCodeRequest sendEmailCodeRequest);
    
    /**
     * 忘记密码（通过用户名重置）
     * 
     * 通过用户名验证码重置密码，无需登录
     * 注意：用户名即邮箱格式
     * 
     * @param forgotPasswordRequest 忘记密码请求DTO
     */
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    
    /**
     * 更新用户资料
     * 
     * 更新当前登录用户的基本信息（姓名、手机号、头像等）
     * 
     * @param updateProfileRequest 更新资料请求DTO
     */
    void updateProfile(UpdateProfileRequest updateProfileRequest);
    
    /**
     * 更换用户名
     * 
     * 为当前用户更换新用户名，需要验证码
     * 注意：在本系统中，用户名即邮箱格式
     * 
     * @param bindEmailRequest 更换用户名请求DTO
     */
    void bindEmail(BindEmailRequest bindEmailRequest);
    
    /**
     * 检查用户名（邮箱）是否可用
     * 
     * 检查指定的用户名（邮箱）是否已被注册
     * 注意：在本系统中，用户名就是邮箱
     * 
     * @param username 用户名（邮箱）
     * @return boolean true-可用，false-已被使用
     */
    boolean checkUsernameAvailability(String username);
    
    /**
     * 检查登录状态
     * 
     * 检查当前用户是否处于登录状态
     * 
     * @return LoginResponse 登录状态信息
     */
    LoginResponse checkLoginStatus();
    
    
    /**
     * 生成图形验证码
     * 
     * 生成数学运算验证码图片，用于防止机器人攻击
     * 验证码存储在Redis中，有效期5分钟
     * 
     * @return CaptchaResponse 验证码响应，包含验证码ID和Base64图片
     */
    CaptchaResponse generateCaptcha();
}