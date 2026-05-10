package com.jieni.mqxq.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.jieni.mqxq.service.auth.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token权限认证接口实现类
 * 实现自定义权限验证逻辑，为Sa-Token提供权限和角色信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    /** 用户服务，用于获取用户角色信息 */
    @Resource
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     * 当前系统暂不使用细粒度权限控制，返回空列表
     * 
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return List<String> 权限码列表，格式如：/user/add、/user/update
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     * 用于Sa-Token的角色权限校验，支持的角色：ADMIN、TEACHER、USER
     * 
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return List<String> 角色标识列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            // 避免在Spring容器初始化阶段调用可能不存在的登录状态
            if (loginId == null) {
                return List.of();
            }
            
            // 获取登录ID
            String loginIdStr = String.valueOf(loginId);
            if (loginIdStr == null || "null".equals(loginIdStr)) {
                return List.of();
            }
            
            // 验证登录状态并获取用户ID
            if (!StpUtil.isLogin()) {
                return List.of();
            }
            
            // 获取用户角色列表
            Integer userId = Integer.valueOf(loginIdStr);
            List<String> roles = userService.getUserRoleNames(userId);
            
            return roles != null ? roles : List.of();
        } catch (Exception e) {
            // 在初始化阶段或异常情况下返回空列表，避免影响Spring容器启动
            return List.of();
        }
    }
}
