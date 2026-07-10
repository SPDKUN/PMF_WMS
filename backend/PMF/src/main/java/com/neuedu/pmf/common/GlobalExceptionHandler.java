package com.neuedu.pmf.common;

import com.neuedu.pmf.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一捕获并处理所有控制器层抛出的异常
 */
//@Slf4j
@RestControllerAdvice  // 等同于 @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常（优先级最高）
     */
    @ExceptionHandler(BusinessException.class)
    public ResultData handleBusinessException(BusinessException e) {
        // 业务异常只打印info级别日志，不打印堆栈（避免日志冗余）
        //log.info("业务异常：{}", e.getMessage());
        return ResultData.fail(ResultCode.BUSINESS_ERROR);
    }

    /**
     * 处理参数校验异常（@Valid/@Validated 校验失败）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResultData handleValidationException(Exception e) {
        String errorMsg;

        if (e instanceof MethodArgumentNotValidException) {
            // 请求体参数校验失败
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;

            errorMsg = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("；"));
        } else {
            // 请求参数绑定校验失败（如路径参数、请求头参数）
            BindException ex = (BindException) e;
            errorMsg = ex.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("；"));
        }
        log.warn("参数校验异常：{}", errorMsg);
        return ResultData.fail(ResultCode.PARAM_ERROR);
    }

    /**
     * 处理404异常（需要在配置中开启：spring.mvc.throw-exception-if-no-handler-found=true）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultData handle404Exception(NoHandlerFoundException e) {
        log.warn("404异常：请求路径不存在 - {}", e.getRequestURL());
        return ResultData.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 处理数据库唯一键冲突（如重复用户名）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResultData handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据库唯一键冲突：{}", e.getMessage());
        String msg = extractConstraintMessage(e, "数据已存在，请检查是否重复");
        return ResultData.fail(ResultCode.FAILED.getCode(), msg);
    }

    /**
     * 处理数据库约束违反异常（如非空字段为空）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResultData handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据库约束违反：{}", e.getMessage());
        String msg = extractConstraintMessage(e, "数据完整性校验失败，请检查必填字段");
        return ResultData.fail(ResultCode.FAILED.getCode(), msg);
    }

    /**
     * 从异常中提取约束信息
     */
    private String extractConstraintMessage(Exception e, String defaultMsg) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                String msg = cause.getMessage();
                if (msg != null) {
                    if (msg.contains("user_name")) {
                        return "用户名已存在或不能为空";
                    }
                    if (msg.contains("password")) {
                        return "密码不能为空";
                    }
                    if (msg.contains("real_name")) {
                        return "真实姓名不能为空";
                    }
                }
            }
            cause = cause.getCause();
        }
        return defaultMsg;
    }

    /**
     * 处理所有未捕获的系统异常（兜底处理，优先级最低）
     */
    @ExceptionHandler(Exception.class)
    public ResultData handleSystemException(Exception e) {
        // 系统异常打印error级别日志，包含堆栈信息，方便排查问题
        log.error("系统异常", e);
        // 给前端返回通用错误信息，避免暴露系统内部细节
        return ResultData.fail(ResultCode.FAILED);
    }
}