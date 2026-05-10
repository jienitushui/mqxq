package com.jieni.mqxq.common.login.impl;

import com.jieni.mqxq.common.enums.UserTypeEnum;
import com.jieni.mqxq.common.login.LoginStrategy;
import com.jieni.mqxq.domain.vo.auth.LoginUserVo;
import com.jieni.mqxq.domain.vo.user.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理员登录策略实现类
 * 实现管理员用户的登录验证、权限校验和登录后处理逻辑
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class AdminLoginStrategy implements LoginStrategy {
    
    /**
     * 获取支持的用户类型
     * 
     * @return UserTypeEnum 管理员用户类型
     */
    @Override
    public UserTypeEnum getSupportedUserType() {
        return UserTypeEnum.ADMIN;
    }
    
    /**
     * 验证管理员权限
     * 检查用户是否具有管理员角色权限
     * 
     * @param userId 用户ID
     * @param userRoles 用户角色列表
     * @return boolean 是否具有管理员权限
     */
    @Override
    public boolean validatePermission(Integer userId, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("管理员登录失败：用户角色为空，用户ID: {}", userId);
            return false;
        }
        
        // 支持多种管理员角色标识
        boolean isAdmin = userRoles.contains(UserTypeEnum.ADMIN.getCode()) ||
                         userRoles.contains("管理员") ||
                         userRoles.contains("admin") ||
                         userRoles.contains("ADMIN");
        
        if (isAdmin) {
            log.info("管理员权限验证通过，用户ID: {}", userId);
        } else {
            log.warn("管理员登录失败：用户不具有管理员权限，用户ID: {}, 角色: {}", userId, userRoles);
        }
        
        return isAdmin;
    }
    
    /**
     * 处理管理员登录成功后的逻辑
     * 设置管理员特有的标识和权限
     * 
     * @param loginUser 登录用户信息
     * @return LoginUserVo 处理后的登录用户信息
     */
    @Override
    public LoginUserVo processLoginSuccess(LoginUserVo loginUser) {
        // 设置管理员特有的标识
        loginUser.setIsAdmin(true);
        log.info("管理员登录成功，用户: {}", loginUser.getUsername());
        return loginUser;
    }
    
    /**
     * 验证用户信息访问权限
     * 检查用户是否具有管理员权限来访问用户信息
     * 
     * @param userInfo 用户信息
     * @return boolean 是否有权限访问
     */
    @Override
    public boolean validateUserInfoAccess(UserVo userInfo) {
        if (userInfo == null || userInfo.getRoleList() == null) {
            return false;
        }
        
        List<String> roles = userInfo.getRoleList();
        return roles.contains(UserTypeEnum.ADMIN.getCode()) ||
               roles.contains("管理员") ||
               roles.contains("admin") ||
               roles.contains("ADMIN");
    }
    
    /**
     * 获取权限验证失败时的错误消息
     * 
     * @return String 管理员权限验证失败的错误提示
     */
    @Override
    public String getPermissionDeniedMessage() {
        return "该账号不具有管理员权限";
    }
}