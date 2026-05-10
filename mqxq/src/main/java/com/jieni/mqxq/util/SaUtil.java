package com.jieni.mqxq.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.domain.vo.user.UserVo;
import com.jieni.mqxq.exception.MyException;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token安全认证工具类
 * 
 * 对Sa-Token框架进行再封装，提供用户登录、登出、密码加密、会话管理等功能。
 * 支持BCrypt密码加密、请求作用域数据存储、用户信息获取等特性。为平台的统一认证系统提供核心支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class SaUtil {

    /**
     * 获取当前账号token
     * 
     * 获取当前登录用户的访问令牌，用于身份验证和权限检查
     * 
     * @return String 当前用户的token字符串，未登录时返回null
     */
    public static String getToken() {
        return StpUtil.getTokenValue();
    }

    /**
     * 根据账号ID登录并返回token
     * 
     * 为指定用户ID创建登录会话，生成并返回访问令牌
     * 
     * @param id 用户ID，可以是整数或字符串类型
     * @return String 登录成功后生成的token字符串
     */
    public static String login(Object id) {
        StpUtil.login(id);
        return getToken();
    }

    /**
     * 注销当前账号
     * 
     * 登出当前用户，清除登录状态和相关缓存信息
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录账号ID
     * 
     * 获取当前登录用户的ID，支持多种类型转换
     * 包括字符串到整数的自动转换和错误处理
     * 
     * @return Integer 当前登录用户的ID，未登录时返回null
     * @throws RuntimeException 当用户ID格式错误或类型不支持时抛出
     */
    public static Integer getLoginId() {
//        return (Integer) StpUtil.getLoginId();
        log.info("loginId: {}", StpUtil.getLoginId());
        Object loginId = StpUtil.getLoginId();
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Integer) {
            return (Integer) loginId;
        }
        if (loginId instanceof String) {
            try {
                return Integer.valueOf((String) loginId);
            } catch (NumberFormatException e) {
                throw new MyException("用户ID格式错误: " + loginId);
            }
        }
        throw new MyException("无法识别的用户ID类型: " + loginId.getClass().getName());
    }

    /**
     * 获取当前的登录用户
     * 
     * 根据当前会话获取完整的用户实体对象
     * 通过数据库查询获取最新的用户信息
     * 
     * @return User 当前登录用户的实体对象，未登录时返回null
     */
    public static User getLoginUser() {
//        Object loginId = StpUtil.getLoginId();
//        if (loginId != null) {
//            Integer userId = Integer.valueOf(loginId.toString());
        Integer userId = getLoginId();
        if (userId != null) {
            UserDao userDao = SpringUtils.getBean(UserDao.class);
            return userDao.queryById(userId);
        }
        return null;
    }

    /**
     * 将原密码进行BCrypt加密
     * 
     * 使用BCrypt算法对密码进行加密，提供高安全性的密码存储
     * 每次加密都会生成不同的盐值，确保密码安全
     * 
     * @param password 原始密码字符串
     * @return String 加密后的密码hash值
     */
    public static String toBcPassword(String password) {
        //加密
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 验证密码是否匹配
     * 
     * 将原始密码与加密后的密码进行比对，验证密码是否正确
     * 使用BCrypt算法进行安全验证，防止密码泄露
     * 
     * @param oriPassword 原始密码字符串
     * @param encode 加密后的密码hash值
     * @return boolean 密码匹配返回true，不匹配返回false
     */
    public static boolean isBc(String oriPassword, String encode) {
        return BCrypt.checkpw(oriPassword, encode);
    }

    /**
     * 获取请求作用域的值
     * 
     * 从当前请求的存储作用域中获取指定键的值
     * 用于在同一请求周期内共享数据
     * 
     * @param key 存储键名
     * @return Object 存储的值，不存在时返回null
     */
    public static Object getStorageValue(String key) {
        return SaHolder.getStorage().get(key);
    }

    /**
     * 写值到请求作用域
     * 
     * 将键值对存储到当前请求的作用域中
     * 用于在同一请求周期内传递和共享数据
     * 
     * @param key 存储键名
     * @param value 要存储的值
     */
    public static void setStorageValue(String key, Object value) {
        SaHolder.getStorage().set(key, value);
    }
}
