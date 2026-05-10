package com.jieni.mqxq.common.login.impl;

import com.jieni.mqxq.common.enums.UserTypeEnum;
import com.jieni.mqxq.common.login.LoginStrategy;
import com.jieni.mqxq.domain.vo.auth.LoginUserVo;
import com.jieni.mqxq.domain.vo.user.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学员登录策略实现类
 * 实现学员用户的登录验证、权限校验和登录后处理逻辑
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class StudentLoginStrategy implements LoginStrategy {
    
    /**
     * 获取支持的用户类型
     * 
     * @return UserTypeEnum 学员用户类型
     */
    @Override
    public UserTypeEnum getSupportedUserType() {
        return UserTypeEnum.STUDENT;
    }
    
    /**
     * 验证学员权限
     * 学员登录不需要特殊权限验证，所有注册用户都可以作为学员登录
     * 
     * @param userId 用户ID
     * @param userRoles 用户角色列表
     * @return boolean 始终返回true，允许所有用户作为学员登录
     */
    @Override
    public boolean validatePermission(Integer userId, List<String> userRoles) {
        // 学员登录不需要特殊权限验证，所有注册用户都可以作为学员登录
        log.debug("学员登录权限验证通过，用户ID: {}", userId);
        return true;
    }
    
    /**
     * 处理学员登录成功后的逻辑
     * 设置学员特有的标识和权限
     * 
     * @param loginUser 登录用户信息
     * @return LoginUserVo 处理后的登录用户信息
     */
    @Override
    public LoginUserVo processLoginSuccess(LoginUserVo loginUser) {
        // 设置学员特有的标识
        loginUser.setIsAdmin(false);
        log.info("学员登录成功，用户: {}", loginUser.getUsername());
        return loginUser;
    }
    
    /**
     * 验证用户信息访问权限
     * 学员可以访问自己的基本信息
     * 
     * @param userInfo 用户信息
     * @return boolean 始终返回true，允许学员访问基本信息
     */
    @Override
    public boolean validateUserInfoAccess(UserVo userInfo) {
        // 学员可以访问自己的基本信息
        return true;
    }
    
    /**
     * 获取权限验证失败时的错误消息
     * 
     * @return String 学员权限验证失败的错误提示
     */
    @Override
    public String getPermissionDeniedMessage() {
        return "该账号无法作为学员登录";
    }
}