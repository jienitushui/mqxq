package com.jieni.mqxq.common.enums;

/**
 * 日志模块枚举
 * 定义系统中各个功能模块的日志分类标识
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public enum LogModuleEnum {
    /** 用户模块 */
    USER("用户"),
    
    /** 订单模块 */
    ORDERS("订单");

    /** 模块中文名称 */
    public String value;

    /**
     * 构造函数
     * 
     * @param value 模块中文名称
     */
    LogModuleEnum(String value) {
        this.value = value;
    }
}
