package com.jieni.mqxq.exception;

import com.jieni.mqxq.common.enums.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常类
 * 
 * 用于处理业务逻辑中的异常情况，支持错误码和错误信息的统一管理
 * 提供多种构造方式，支持与结果枚举的集成，简化异常处理
 * 实现统一的异常信息返回和日志记录，提高系统的可维护性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyException extends RuntimeException {
    
    /** 错误码 */
    private String code;
    
    /** 错误消息 */
    private String message;
    
    public MyException() {
        this(ResultCodeEnum.BUSINESS_ERROR);
    }

    public MyException(String message) {
        this(ResultCodeEnum.BUSINESS_ERROR.code, message);
    }
    
    public MyException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public MyException(ResultCodeEnum resultCodeEnum) {
        this(resultCodeEnum.code, resultCodeEnum.msg);
    }
    
    public MyException(ResultCodeEnum resultCodeEnum, String message) {
        this(resultCodeEnum.code, message);
    }
}
