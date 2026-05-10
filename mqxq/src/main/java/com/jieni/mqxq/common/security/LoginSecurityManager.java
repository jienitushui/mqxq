package com.jieni.mqxq.common.security;

import com.jieni.mqxq.common.constants.LoginConstants;
import com.jieni.mqxq.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 登录安全管理器
 * 
 * 统一处理登录安全相关逻辑，职责单一，只负责安全验证相关功能
 * 
 * 核心功能：
 * 1. IP锁定机制：防止暴力破解，超过最大失败次数后锁定IP
 * 2. 登录失败计数：记录和管理登录失败次数
 * 3. 验证码验证：验证图形验证码的正确性
 * 4. 验证码存储：将验证码存储到Redis并设置过期时间
 * 
 * 设计原则：
 * - 单一职责：只处理安全验证，不涉及业务逻辑
 * - 无状态：所有状态存储在Redis中
 * - 高内聚：所有安全相关的验证集中在此类
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class LoginSecurityManager {
    
    /** Redis工具类，用于缓存操作 */
    @Resource
    private RedisUtil redisUtil;
    
    /**
     * 检查IP是否被锁定
     * 根据登录失败次数判断IP是否被锁定
     * 
     * @param ip IP地址
     * @return boolean 是否被锁定
     */
    public boolean isIpLocked(String ip) {
        String failKey = LoginConstants.LOGIN_FAIL_LIMIT_KEY + ip;
        String failCountStr = redisUtil.getString(failKey);
        
        if (failCountStr != null) {
            int failCount = Integer.parseInt(failCountStr);
            boolean isLocked = failCount >= LoginConstants.MAX_LOGIN_FAIL_TIMES;
            
            if (isLocked) {
                log.warn("IP被锁定，IP: {}, 失败次数: {}", ip, failCount);
            }
            
            return isLocked;
        }
        
        return false;
    }
    
    /**
     * 增加登录失败次数
     * 每次登录失败时调用，累计失败次数，达到阈值时锁定IP
     * 
     * @param ip IP地址
     */
    public void incrementLoginFailCount(String ip) {
        String failKey = LoginConstants.LOGIN_FAIL_LIMIT_KEY + ip;
        String failCountStr = redisUtil.getString(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        
        failCount++;
        // 设置失败计数，过期时间为锁定时长
        redisUtil.add(failKey, String.valueOf(failCount), LoginConstants.LOGIN_LOCK_TIME * 60);
        
        log.warn("登录失败次数增加，IP: {}, 当前失败次数: {}", ip, failCount);
        
        if (failCount >= LoginConstants.MAX_LOGIN_FAIL_TIMES) {
            log.error("IP达到最大失败次数被锁定，IP: {}, 锁定时间: {}分钟", ip, LoginConstants.LOGIN_LOCK_TIME);
        }
    }
    
    /**
     * 清除登录失败计数
     * 登录成功时调用，清除该IP的失败记录
     * 
     * @param ip IP地址
     */
    public void clearLoginFailCount(String ip) {
        String failKey = LoginConstants.LOGIN_FAIL_LIMIT_KEY + ip;
        redisUtil.delete(failKey);
        log.info("清除登录失败计数，IP: {}", ip);
    }
    
    /**
     * 验证验证码
     * 验证用户输入的验证码是否正确
     * 
     * @param captchaId 验证码ID
     * @param captcha 用户输入的验证码
     * @return CaptchaValidationResult 验证结果
     */
    public CaptchaValidationResult validateCaptcha(String captchaId, String captcha) {
        if (captchaId == null || captcha == null) {
            return CaptchaValidationResult.builder()
                    .valid(false)
                    .message("验证码信息不完整")
                    .build();
        }
        
        String captchaKey = LoginConstants.LOGIN_CAPTCHA + captchaId;
        String storedCaptcha = redisUtil.getString(captchaKey);
        
        if (storedCaptcha == null) {
            return CaptchaValidationResult.builder()
                    .valid(false)
                    .message("验证码已失效")
                    .build();
        }
        
        boolean isValid = captcha.equalsIgnoreCase(storedCaptcha);
        
        if (isValid) {
            // 验证成功后删除验证码，防止重复使用
            redisUtil.delete(captchaKey);
            log.debug("验证码验证成功，captchaId: {}", captchaId);
        } else {
            log.warn("验证码验证失败，captchaId: {}, 输入: {}, 期望: {}", captchaId, captcha, storedCaptcha);
        }
        
        return CaptchaValidationResult.builder()
                .valid(isValid)
                .message(isValid ? "验证码验证成功" : "验证码错误")
                .build();
    }
    
    /**
     * 获取剩余锁定时间（分钟）
     * 查询指定IP的剩余锁定时间
     * 
     * @param ip IP地址
     * @return long 剩余锁定时间（分钟），-1表示未锁定
     */
    public long getRemainingLockTime(String ip) {
        String failKey = LoginConstants.LOGIN_FAIL_LIMIT_KEY + ip;
        Long ttl = redisUtil.getExpire(failKey);
        
        if (ttl != null && ttl > 0) {
            return ttl / 60; // 转换为分钟
        }
        
        return -1;
    }
    
    /**
     * 存储验证码到Redis
     * 将生成的验证码存储到Redis中，设置过期时间
     * 
     * @param captchaKey 验证码唯一标识Key
     * @param captchaCode 验证码答案
     * @param expireSeconds 过期时间（秒）
     */
    public void storeCaptcha(String captchaKey, String captchaCode, int expireSeconds) {
        String key = LoginConstants.LOGIN_CAPTCHA + captchaKey;
        redisUtil.add(key, captchaCode, expireSeconds);
        log.debug("验证码已存储到Redis，key：{}，过期时间：{}秒", key, expireSeconds);
    }
    
    /**
     * 删除验证码
     * 验证成功或需要清理时删除验证码
     * 
     * @param captchaKey 验证码唯一标识Key
     */
    public void deleteCaptcha(String captchaKey) {
        String key = LoginConstants.LOGIN_CAPTCHA + captchaKey;
        redisUtil.delete(key);
        log.debug("验证码已删除，key：{}", key);
    }
    
    /**
     * 验证码验证结果类
     * 封装验证码验证的结果信息
     */
    public static class CaptchaValidationResult {
        /** 是否验证成功 */
        private boolean valid;
        
        /** 验证结果消息 */
        private String message;
        
        /**
         * 创建建造者实例
         * 
         * @return CaptchaValidationResultBuilder 建造者实例
         */
        public static CaptchaValidationResultBuilder builder() {
            return new CaptchaValidationResultBuilder();
        }
        
        /**
         * 获取验证结果
         * 
         * @return boolean 是否验证成功
         */
        public boolean isValid() { 
            return valid; 
        }
        
        /**
         * 获取验证消息
         * 
         * @return String 验证消息
         */
        public String getMessage() { 
            return message; 
        }
        
        /**
         * 验证码验证结果建造者类
         * 使用建造者模式构建验证结果对象
         */
        public static class CaptchaValidationResultBuilder {
            /** 验证结果 */
            private boolean valid;
            
            /** 验证消息 */
            private String message;
            
            /**
             * 设置验证结果
             * 
             * @param valid 是否验证成功
             * @return CaptchaValidationResultBuilder 建造者实例
             */
            public CaptchaValidationResultBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }
            
            /**
             * 设置验证消息
             * 
             * @param message 验证消息
             * @return CaptchaValidationResultBuilder 建造者实例
             */
            public CaptchaValidationResultBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            /**
             * 构建验证结果对象
             * 
             * @return CaptchaValidationResult 验证结果对象
             */
            public CaptchaValidationResult build() {
                CaptchaValidationResult result = new CaptchaValidationResult();
                result.valid = this.valid;
                result.message = this.message;
                return result;
            }
        }
    }
}