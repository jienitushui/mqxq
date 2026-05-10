package com.jieni.mqxq.common.constants;

/**
 * 登录相关常量类
 * 定义登录模块中使用的各种常量值，包括验证码、失败限制、锁定机制等
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public class LoginConstants {
    
    /** 登录验证码在Redis中的键前缀 */
    public static final String LOGIN_CAPTCHA = "login_captcha:";
    
    /** 登录失败计数在Redis中的键前缀 */
    public static final String LOGIN_FAIL_COUNT = "login_fail_count:";
    
    /** IP锁定在Redis中的键前缀 */
    public static final String IP_LOCK = "ip_lock:";
    
    /** 最大登录失败次数，超过此次数将锁定IP */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;
    
    /** IP锁定时间（分钟） */
    public static final int IP_LOCK_TIME = 30;
    
    /** 验证码有效期（秒） */
    public static final int CAPTCHA_EXPIRE_TIME = 300;
    
    /** 验证码字段名常量 */
    public static final String CODE = "code";
    
    /** 验证码有效时间（秒），与CAPTCHA_EXPIRE_TIME保持一致 */
    public static final int CODE_TIME = 300;
    
    /** 登录失败限制键前缀，用于计数登录失败次数 */
    public static final String LOGIN_FAIL_LIMIT_KEY = "login_fail_limit:";
    
    /** 最大登录失败次数，与MAX_LOGIN_FAIL_COUNT保持一致 */
    public static final int MAX_LOGIN_FAIL_TIMES = 5;
    
    /** 登录锁定时间（分钟），与IP_LOCK_TIME保持一致 */
    public static final int LOGIN_LOCK_TIME = 30;
}