package com.jieni.mqxq.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jieni.mqxq.common.enums.ResultCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果类
 * 用于封装API接口的返回数据，提供统一的响应格式
 * 
 * @param <T> 数据类型
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 成功状态码 */
    private static final String CODE_SUCCESS = "200";
    
    /** 成功提示信息 */
    private static final String MSG_SUCCESS = "请求成功";
    
    /** 错误状态码 */
    private static final String CODE_ERROR = "500";
    
    /** 错误提示信息 */
    private static final String MSG_ERROR = "请求失败";

    /** 响应状态码，200表示成功，其他表示失败 */
    private String code;
    
    /** 响应消息，通常用于描述失败原因 */
    private String msg;
    
    /** 响应数据，包含接口返回的具体数据 */
    private T data;

    /**
     * 创建响应结果对象
     * 
     * @param code 状态码
     * @param msg 消息
     * @param <T> 数据类型
     * @return 响应结果对象
     */
    public static <T> Result<T> result(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 创建响应结果对象（带数据）
     * 
     * @param code 状态码
     * @param msg 消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应结果对象
     */
    public static <T> Result<T> result(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 基于枚举创建响应结果对象
     * 
     * @param resultCodeEnum 结果代码枚举
     * @param <T> 数据类型
     * @return 响应结果对象
     */
    public static <T> Result<T> result(ResultCodeEnum resultCodeEnum) {
        return result(resultCodeEnum.code, resultCodeEnum.msg);
    }

    /**
     * 基于枚举创建响应结果对象（带数据）
     * 
     * @param resultCodeEnum 结果代码枚举
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应结果对象
     */
    public static <T> Result<T> result(ResultCodeEnum resultCodeEnum, T data) {
        return result(resultCodeEnum.code, resultCodeEnum.msg, data);
    }

    /**
     * 创建成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return result(ResultCodeEnum.SUCCESS);
    }

    /**
     * 创建成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return result(ResultCodeEnum.SUCCESS, data);
    }

    /**
     * 创建成功响应（自定义消息）
     * 
     * @param msg 成功消息
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String msg) {
        return result(CODE_SUCCESS, msg);
    }

    /**
     * 创建成功响应（自定义消息和数据）
     * 
     * @param msg 成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String msg, T data) {
        return result(CODE_SUCCESS, msg, data);
    }

    /**
     * 创建错误响应（默认错误信息）
     * 
     * @param <T> 数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error() {
        return result(ResultCodeEnum.SYSTEM_ERROR);
    }

    /**
     * 创建错误响应（自定义错误信息）
     * 
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String msg) {
        return result(CODE_ERROR, msg);
    }

    /**
     * 创建错误响应（自定义状态码和错误信息）
     * 
     * @param code 错误状态码
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String code, String msg) {
        return result(code, msg);
    }

    /**
     * 基于枚举创建错误响应
     * 
     * @param resultCodeEnum 结果代码枚举
     * @param <T> 数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        return result(resultCodeEnum);
    }

    /**
     * 基于枚举创建错误响应（带数据）
     * 
     * @param resultCodeEnum 结果代码枚举
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum, T data) {
        return result(resultCodeEnum, data);
    }

    // =========================== 实用判断方法 ===========================

    /**
     * 判断响应是否成功
     * 注意：此方法不会被序列化到JSON中
     * 
     * @return 是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        return CODE_SUCCESS.equals(this.code);
    }

    /**
     * 判断响应是否失败
     * 注意：此方法不会被序列化到JSON中
     * 
     * @return 是否失败
     */
    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 获取数据，如果响应失败则返回null
     * 注意：此方法不会被序列化到JSON中
     * 
     * @return 数据或null
     */
    @JsonIgnore
    public T getDataIfSuccess() {
        return isSuccess() ? this.data : null;
    }

}
