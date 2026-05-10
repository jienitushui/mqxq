package com.jieni.mqxq.common.login;

import com.jieni.mqxq.common.enums.UserTypeEnum;
import com.jieni.mqxq.domain.vo.auth.LoginUserVo;
import com.jieni.mqxq.domain.vo.user.UserVo;

import java.util.List;

/**
 * 登录策略接口
 * 定义不同用户类型的登录验证规则，采用策略模式实现多种登录逻辑
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface LoginStrategy {
    
    /**
     * 获取支持的用户类型
     * 每个策略实现类需要指定其支持的用户类型
     * 
     * @return UserTypeEnum 支持的用户类型枚举
     */
    UserTypeEnum getSupportedUserType();
    
    /**
     * 验证用户权限
     * 根据用户ID和角色列表验证用户是否具有对应的登录权限
     * 
     * @param userId 用户ID
     * @param userRoles 用户角色列表
     * @return boolean 是否具有对应权限
     */
    boolean validatePermission(Integer userId, List<String> userRoles);
    
    /**
     * 处理登录成功后的逻辑
     * 在用户登录成功后执行特定的处理逻辑，如设置特殊权限、记录日志等
     * 
     * @param loginUser 登录用户信息
     * @return LoginUserVo 处理后的登录用户信息
     */
    LoginUserVo processLoginSuccess(LoginUserVo loginUser);
    
    /**
     * 获取用户信息时的权限验证
     * 验证当前用户是否有权限访问指定的用户信息
     * 
     * @param userInfo 用户信息
     * @return boolean 是否有权限访问
     */
    boolean validateUserInfoAccess(UserVo userInfo);
    
    /**
     * 获取权限验证失败时的错误消息
     * 返回适合当前用户类型的权限验证失败提示信息
     * 
     * @return String 权限验证失败的错误消息
     */
    String getPermissionDeniedMessage();
}