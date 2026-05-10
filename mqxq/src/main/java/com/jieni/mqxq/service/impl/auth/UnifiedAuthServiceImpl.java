package com.jieni.mqxq.service.impl.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import com.google.code.kaptcha.Producer;
import com.jieni.mqxq.common.constants.LoginConstants;
import com.jieni.mqxq.domain.dto.auth.*;
import com.jieni.mqxq.domain.vo.auth.LoginResponse;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.auth.UnifiedAuthService;
import com.jieni.mqxq.service.auth.UserService;
import com.jieni.mqxq.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 统一认证服务实现类
 * 实现统一的用户认证服务，支持多种用户类型的登录验证
 * 
 * 主要功能：
 * - 统一登录入口，支持管理员、教师、学生登录
 * - 基于策略模式处理不同类型用户的权限验证
 * - 登录安全管理：验证码验证、IP锁定、失败计数
 * - 用户信息管理和会话管理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class UnifiedAuthServiceImpl implements UnifiedAuthService {
    
    /** 用户服务，提供用户信息和角色管理 */
    @Resource
    private UserService userService;
    
    /** 增强认证服务，提供扩展功能 */
    @Resource
    private UnifiedAuthServiceEnhanced enhancedService;
    
    /** Kaptcha验证码生成器 */
    @Resource
    private Producer producer;
    
    /** Redis工具类 */
    @Resource
    private RedisUtil redisUtil;
    
    @Override
    public boolean logout() {
//        log.info("用户登出", StpUtil.getLoginId());
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                log.info("用户登出，用户ID: {}", loginId);
                StpUtil.logout();
                log.info("用户登出成功，用户ID: {}", loginId);
                return true;
            }
            return true;
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return false;
        }
    }

    // 实现新增的接口方法，委托给增强服务

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress)  {
        return enhancedService.login(request, ipAddress);
    }

    @Override
    public void register(RegisterRequest request, String ipAddress)  {
        enhancedService.register(request, ipAddress);
    }

    @Override
    public LoginResponse getCurrentUserInfo()  {
        return enhancedService.getCurrentUserInfo();
    }

    @Override
    public LoginResponse refreshToken()  {
        return enhancedService.refreshToken();
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest)  {
        enhancedService.changePassword(
            changePasswordRequest.getOldPassword(),
            changePasswordRequest.getNewPassword(),
            changePasswordRequest.getConfirmPassword()
        );
    }

    @Override
    public void sendEmailCode(SendEmailCodeRequest sendEmailCodeRequest) {
        enhancedService.sendEmailCode(
            sendEmailCodeRequest.getUsername(),
            sendEmailCodeRequest.getType()
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        enhancedService.forgotPassword(
            forgotPasswordRequest.getUsername(),
            forgotPasswordRequest.getCode(),
            forgotPasswordRequest.getNewPassword(),
            forgotPasswordRequest.getConfirmPassword()
        );
    }

    @Override
    public void updateProfile(UpdateProfileRequest updateProfileRequest) {
        enhancedService.updateProfile(
            updateProfileRequest.getName(),
            updateProfileRequest.getPhone(),
            updateProfileRequest.getAvatar()
        );
    }

    @Override
    public void bindEmail(BindEmailRequest bindEmailRequest) {
        enhancedService.bindEmail(
            bindEmailRequest.getUsername(),
            bindEmailRequest.getCode()
        );
    }

    @Override
    public LoginResponse checkLoginStatus() {
        return enhancedService.checkLoginStatus();
    }
    
    @Override
    public boolean checkUsernameAvailability(String username) {
        // 在本系统中，用户名就是邮箱
        return enhancedService.checkUsernameAvailability(username);
    }
    
    @Override
    public CaptchaResponse generateCaptcha() {
        log.debug("生成图形验证码");
        
        // 生成验证码ID
        String captchaId = IdUtil.fastSimpleUUID();
        String captchaKey = LoginConstants.LOGIN_CAPTCHA + captchaId;
        
        // 生成验证码文本（格式：算式@答案）
        String captchaText = producer.createText();
        String captchaStr = captchaText.substring(0, captchaText.lastIndexOf("@"));
        String captchaCode = captchaText.substring(captchaText.lastIndexOf("@") + 1);
        
        // 存储验证码答案到Redis，5分钟有效期
        redisUtil.add(captchaKey, captchaCode, LoginConstants.CODE_TIME);
        log.debug("验证码已存储到Redis，key：{}，过期时间：{}秒", captchaKey, LoginConstants.CODE_TIME);
        
        // 生成验证码图片
        try (FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream()) {
            BufferedImage image = producer.createImage(captchaStr);
            ImageIO.write(image, "jpg", outputStream);
            
            // 构建响应对象
            CaptchaResponse response = new CaptchaResponse();
            response.setCaptchaKey(captchaId);
            response.setCaptchaImage("data:image/jpeg;base64," + Base64.encode(outputStream.toByteArray()));
            response.setExpiresIn((long) LoginConstants.CODE_TIME);
            
            log.info("验证码生成成功，ID：{}", captchaId);
            return response;
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            throw new MyException("生成验证码失败：" + e.getMessage());
        }
    }
}