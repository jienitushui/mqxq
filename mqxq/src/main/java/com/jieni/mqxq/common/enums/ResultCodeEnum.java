package com.jieni.mqxq.common.enums;

/**
 * 响应结果代码枚举
 * 定义系统中各种操作的响应状态码和描述信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public enum ResultCodeEnum {
    /** 请求成功 */
    SUCCESS("200", "请求成功"),
    
    /** Token无效错误 - 需要重新登录 */
    TOKEN_INVALID_ERROR("401", "请重新登录"),
    
    /** 参数错误 */
    PARAM_ERROR("400", "参数错误"),
    
    /** 系统异常 */
    SYSTEM_ERROR("500", "系统异常"),
    
    /** 登录失败 */
    LOGIN_ERROR("600", "登录失败"),
    
    /** 注册失败 */
    REGISTER_ERROR("601", "注册失败"),
    
    /** 未登录 */
    NOT_LOGIN("602", "未登录"),
    
    /** 权限不够 */
    NO_PERMISSION("603", "权限不够"),
    
    /** 业务异常 */
    BUSINESS_ERROR("700", "业务处理失败"),
    
    /** 数据不存在 */
    DATA_NOT_FOUND("701", "数据不存在"),
    
    /** 数据已存在 */
    DATA_ALREADY_EXISTS("702", "数据已存在"),
    
    /** 操作失败 */
    OPERATION_FAILED("703", "操作失败"),
    
    /** 数据库操作异常 */
    DB_ERROR("704", "数据库操作异常"),
    
    /** 外部服务调用失败 */
    EXTERNAL_SERVICE_ERROR("705", "外部服务调用失败"),

    // ==================== 课程相关错误码 (710-719) ====================
    /** 课程不存在 */
    COURSE_NOT_FOUND("710", "课程不存在"),

    /** 课程发布失败 */
    COURSE_PUBLISH_FAILED("711", "课程发布失败"),

    /** 课程取消发布失败 */
    COURSE_UNPUBLISH_FAILED("712", "课程取消发布失败"),

    // ==================== 订单相关错误码 (720-729) ====================
    /** 订单不存在 */
    ORDER_NOT_FOUND("720", "订单不存在"),

    /** 订单创建失败 */
    ORDER_CREATE_FAILED("721", "订单创建失败"),

    /** 订单取消失败 */
    ORDER_CANCEL_FAILED("722", "订单取消失败"),

    // ==================== 支付相关错误码 (730-739) ====================
    /** 支付失败 */
    PAYMENT_FAILED("730", "支付失败"),

    /** 支付超时 */
    PAYMENT_TIMEOUT("731", "支付超时"),

    // ==================== 作业相关错误码 (740-749) ====================
    /** 作业不存在 */
    HOMEWORK_NOT_FOUND("740", "作业不存在"),

    /** 作业提交失败 */
    HOMEWORK_SUBMIT_FAILED("741", "作业提交失败"),

    // ==================== 权限相关错误码 (750-759) ====================
    /** 无权限访问 */
    UNAUTHORIZED("750", "无权限访问"),

    /** 禁止访问 */
    FORBIDDEN("751", "禁止访问");

    /** 响应状态码 */
    public String code;
    
    /** 响应消息 */
    public String msg;

    /**
     * 构造函数
     * 
     * @param code 响应状态码
     * @param msg 响应消息
     */
    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
