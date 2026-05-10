package com.jieni.mqxq.exception;

import cn.dev33.satoken.exception.SaTokenException;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.common.enums.ResultCodeEnum;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中抛出的各种异常，并返回规范的错误响应
 * 
 * 处理的异常类型包括：
 * - Sa-Token权限异常
 * - 自定义业务异常
 * - 参数校验异常
 * - 系统运行时异常
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@ControllerAdvice("com.jieni.mqxq.controller")
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理Sa-Token权限验证异常
     * 当用户未登录、权限不足或Token失效时抛出此异常
     * 
     * @param request HTTP请求对象
     * @param e Sa-Token异常
     * @return Result 权限验证失败的响应结果
     */
    @ExceptionHandler(SaTokenException.class)
    @ResponseBody
    public Result saTokenError(HttpServletRequest request, SaTokenException e){
        log.error("权限异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCodeEnum.TOKEN_INVALID_ERROR.code, ResultCodeEnum.TOKEN_INVALID_ERROR.msg);
    }

    /**
     * 处理业务异常
     * 统一处理 MyException，返回业务层级的错误信息
     * 
     * @param request HTTP请求对象
     * @param e 自定义业务异常
     * @return Result 业务异常的响应结果
     */
    @ExceptionHandler(MyException.class)
    @ResponseBody
    public Result businessExceptionHandler(HttpServletRequest request, MyException e){
        log.warn("业务异常：请求路径={}, 错误码={}, 异常信息={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     * 当方法参数不符合要求时抛出此异常
     *
     * @param request HTTP请求对象
     * @param e 非法参数异常
     * @return Result 参数校验失败的响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Result handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        log.warn("参数验证异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCodeEnum.PARAM_ERROR.code, "参数不正确: " + e.getMessage());
    }

    /**
     * 处理JSR-303参数验证失败异常
     * 当@PathVariable或@RequestParam上的验证注解（如@NotNull、@Min等）验证失败时抛出此异常
     * 需要Controller类上有@Validated注解才会生效
     *
     * @param request HTTP请求对象
     * @param e 约束违反异常
     * @return Result 参数验证失败的响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Result handleConstraintViolation(HttpServletRequest request, ConstraintViolationException e) {
        log.warn("JSR-303参数验证失败：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage());

        // 提取所有验证失败的消息
        String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));

        return Result.error(ResultCodeEnum.PARAM_ERROR.code, message);
    }

    /**
     * 处理空指针异常
     * 当代码中出现空指针访问时抛出此异常
     * 
     * @param request HTTP请求对象
     * @param e 空指针异常
     * @return Result 系统内部错误的响应结果，如果是流式响应则返回null
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public Result handleNullPointerException(HttpServletRequest request, NullPointerException e) {
        // 检查是否是SSE流式响应请求
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // SSE请求的空指针异常，只记录日志，不返回Result（因为响应类型不匹配）
            log.error("SSE流式响应空指针异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
            return null;
        }
        log.error("空指针异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCodeEnum.SYSTEM_ERROR.code, "系统内部错误");
    }

    /**
     * 处理数据库相关运行时异常
     * 包括数据完整性约束异常和唯一键冲突异常
     * 
     * @param request HTTP请求对象
     * @param e 数据库相关运行时异常
     * @return Result 数据操作失败的响应结果
     */
    @ExceptionHandler({org.springframework.dao.DataIntegrityViolationException.class,
                      org.springframework.dao.DuplicateKeyException.class})
    @ResponseBody
    public Result handleDataException(HttpServletRequest request, RuntimeException e) {
        log.warn("数据库操作异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCodeEnum.PARAM_ERROR.code, "数据操作失败，请检查输入信息");
    }

    /**
     * 处理异步请求超时异常
     * 当SSE连接超时时，不应该返回Result对象，因为响应类型是text/event-stream
     * 
     * @param request HTTP请求对象
     * @param e 异步请求超时异常
     * @return null 不返回结果，让Spring处理
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseBody
    public Result handleAsyncRequestTimeoutException(HttpServletRequest request, AsyncRequestTimeoutException e) {
        // 检查是否是SSE请求
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // SSE请求超时，只记录日志，不返回Result（因为响应类型不匹配）
            log.warn("SSE请求超时：请求路径={}", request.getRequestURI());
            return null;
        }
        log.warn("异步请求超时：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage());
        return Result.error(ResultCodeEnum.SYSTEM_ERROR.code, "请求超时，请稍后重试");
    }

    /**
     * 处理其他运行时异常
     * 处理未被上述具体异常处理方法处理的运行时异常
     * 
     * @param request HTTP请求对象
     * @param e 运行时异常
     * @return Result 系统异常的响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result handleRuntimeException(HttpServletRequest request, RuntimeException e) {
        // 检查是否是SSE请求
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // SSE请求的运行时异常，只记录日志，不返回Result
            log.error("SSE运行时异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
            return null;
        }
        log.error("运行时异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCodeEnum.SYSTEM_ERROR.code, "系统处理异常，请稍后重试");
    }

    /**
     * 统一异常处理
     * 作为最后一道防线，处理所有未被上述方法捕获的异常
     * 
     * @param request HTTP请求对象
     * @param e 异常对象
     * @return Result 系统异常的响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(HttpServletRequest request, Exception e){
        log.error("系统异常：请求路径={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCodeEnum.SYSTEM_ERROR.code, ResultCodeEnum.SYSTEM_ERROR.msg);
    }

    /**
     * 处理方法参数类型不匹配异常
     * 当HTTP请求参数无法转换为方法期望的类型时抛出此异常
     * 
     * @param request HTTP请求对象
     * @param ex 方法参数类型不匹配异常
     * @return Result 参数类型错误的响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Result handleMethodArgumentTypeMismatch(HttpServletRequest request, MethodArgumentTypeMismatchException ex) {
        String error = String.format("参数 '%s' 的值 '%s' 无效，期望的类型是 '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        log.warn("参数类型不匹配：请求路径={}, 错误信息={}", request.getRequestURI(), error);
        return Result.error(ResultCodeEnum.PARAM_ERROR.code, error);
    }
}
