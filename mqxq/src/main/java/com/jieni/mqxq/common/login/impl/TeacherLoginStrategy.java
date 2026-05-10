package com.jieni.mqxq.common.login.impl;

import com.jieni.mqxq.common.enums.UserTypeEnum;
import com.jieni.mqxq.common.login.LoginStrategy;
import com.jieni.mqxq.domain.vo.auth.LoginUserVo;
import com.jieni.mqxq.domain.vo.user.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 教师登录策略实现类
 * 实现教师用户的登录验证、权限校验和登录后处理逻辑
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class TeacherLoginStrategy implements LoginStrategy {
    
    /**
     * 获取支持的用户类型
     * 
     * @return UserTypeEnum 教师用户类型
     */
    @Override
    public UserTypeEnum getSupportedUserType() {
        return UserTypeEnum.TEACHER;
    }
    
    /**
     * 验证教师权限
     * 检查用户是否具有教师角色权限
     * 
     * @param userId 用户ID
     * @param userRoles 用户角色列表
     * @return boolean 是否具有教师权限
     */
    @Override
    public boolean validatePermission(Integer userId, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("教师登录失败：用户角色为空，用户ID: {}", userId);
            return false;
        }
        
        // 支持多种教师角色标识
        boolean isTeacher = userRoles.contains(UserTypeEnum.TEACHER.getCode()) ||
                           userRoles.contains("教师") ||
                           userRoles.contains("teacher") ||
                           userRoles.contains("TEACHER");
        
        if (isTeacher) {
            log.info("教师权限验证通过，用户ID: {}", userId);
        } else {
            log.warn("教师登录失败：用户不具有教师权限，用户ID: {}, 角色: {}", userId, userRoles);
        }
        
        return isTeacher;
    }
    
    /**
     * 处理教师登录成功后的逻辑
     * 设置教师特有的标识和权限
     * 
     * @param loginUser 登录用户信息
     * @return LoginUserVo 处理后的登录用户信息
     */
    @Override
    public LoginUserVo processLoginSuccess(LoginUserVo loginUser) {
        // 设置教师特有的标识
        loginUser.setIsAdmin(false); // 教师不是管理员
        log.info("教师登录成功，用户: {}", loginUser.getUsername());
        return loginUser;
    }
    
    /**
     * 验证用户信息访问权限
     * 检查用户是否具有教师权限来访问用户信息
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
        return roles.contains(UserTypeEnum.TEACHER.getCode()) ||
               roles.contains("教师") ||
               roles.contains("teacher") ||
               roles.contains("TEACHER");
    }
    
    /**
     * 获取权限验证失败时的错误消息
     * 
     * @return String 教师权限验证失败的错误提示
     */
    @Override
    public String getPermissionDeniedMessage() {
        return "该账号不具有教师权限";
    }
}