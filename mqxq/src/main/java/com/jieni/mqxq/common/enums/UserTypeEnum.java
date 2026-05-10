package com.jieni.mqxq.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 用户类型枚举
 * 定义系统中不同类型用户的分类及其相关操作
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public enum UserTypeEnum {
    /** 学员 */
    STUDENT("USER", "用户"),
    
    /** 教师 */
    TEACHER("TEACHER", "教师"), 
    
    /** 管理员 */
    ADMIN("ADMIN", "管理员");
    
    /** 用户类型代码 */
    private final String code;
    
    /** 用户类型名称 */
    private final String name;
    
    /**
     * 构造函数
     * 
     * @param code 用户类型代码
     * @param name 用户类型名称
     */
    UserTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 获取用户类型代码
     * 
     * @return String 用户类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取用户类型名称
     * 
     * @return String 用户类型名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取统一登录路径
     * 所有用户类型都使用统一的登录接口
     * 
     * @return String 登录路径
     */
    public String getLoginPath() {
        return "/api/auth/login";
    }
    
    /**
     * 根据代码获取用户类型
     * 
     * @param code 用户类型代码
     * @return UserTypeEnum 对应的用户类型枚举，找不到则返回null
     */
    public static UserTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (UserTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查是否为有效的用户类型代码
     * 
     * @param code 用户类型代码
     * @return boolean 是否有效
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
    
    /**
     * 获取所有用户类型代码
     * 
     * @return String[] 所有用户类型代码数组
     */
    public static String[] getAllCodes() {
        return Arrays.stream(values())
                .map(UserTypeEnum::getCode)
                .toArray(String[]::new);
    }
    
    /**
     * 根据角色列表获取用户类型
     * 按优先级确定用户类型：管理员 > 教师 > 学生
     * 
     * @param roles 角色列表
     * @return UserType 用户类型包装对象
     */
    public static UserType getByRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return new UserType(STUDENT);
        }
        
        // 优先级：管理员 > 教师 > 学生
        for (String role : roles) {
            if (role != null) {
                String upperRole = role.toUpperCase();
                if ("ADMIN".equals(upperRole) || "管理员".equals(role)) {
                    return new UserType(ADMIN);
                } else if ("TEACHER".equals(upperRole) || "教师".equals(role)) {
                    return new UserType(TEACHER);
                }
            }
        }
        return new UserType(STUDENT);
    }
    
    /**
     * 根据角色列表获取用户类型代码
     * 
     * @param roles 角色列表
     * @return String 用户类型代码
     */
    public static String getUserTypeCode(List<String> roles) {
        UserType userType = getByRoles(roles);
        return userType.getCode();
    }
    
    /**
     * 用户类型包装类
     * 提供用户类型的封装和便捷操作方法
     */
    public static class UserType {
        /** 用户类型枚举 */
        private final UserTypeEnum type;
        
        /**
         * 构造函数
         * 
         * @param type 用户类型枚举，如果为null则默认为STUDENT
         */
        public UserType(UserTypeEnum type) {
            this.type = type != null ? type : STUDENT;
        }
        
        /**
         * 获取用户类型名称
         * 
         * @return String 用户类型名称
         */
        public String getName() {
            return type.getName();
        }
        
        /**
         * 获取用户类型代码
         * 
         * @return String 用户类型代码
         */
        public String getCode() {
            return type.getCode();
        }
        
        /**
         * 获取登录路径
         * 
         * @return String 登录路径
         */
        public String getLoginPath() {
            return type.getLoginPath();
        }
        
        /**
         * 获取用户类型枚举
         * 
         * @return UserTypeEnum 用户类型枚举
         */
        public UserTypeEnum getType() {
            return type;
        }
        
        /**
         * 重写toString方法
         * 
         * @return String 字符串表示
         */
        @Override
        public String toString() {
            return String.format("UserType{code='%s', name='%s'}", 
                    type.getCode(), type.getName());
        }
    }
}