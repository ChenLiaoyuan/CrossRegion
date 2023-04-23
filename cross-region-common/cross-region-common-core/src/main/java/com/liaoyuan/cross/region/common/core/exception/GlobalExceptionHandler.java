package com.liaoyuan.cross.region.common.core.exception;

import com.liaoyuan.cross.region.common.core.response.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;

/**
 * 全局异常处理
 * @author CLY
 * @date 2022/12/9 16:08
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 Bad Request
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e){
        logger.error("缺少请求参数", e);
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "缺少请求参数，"+e.getMessage());
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        logger.error("缺少请求参数", e);
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "缺少请求参数，"+e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        logger.error("参数验证失败", e);
        final BindingResult bindingResult = e.getBindingResult();
        final FieldError fieldError = bindingResult.getFieldError();
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "参数验证失败，"+fieldError.getField()+":"+fieldError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public CommonResponse handleBindException(BindException e){
        logger.error("缺少请求参数", e);
        final BindingResult bindingResult = e.getBindingResult();
        final FieldError fieldError = bindingResult.getFieldError();
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "缺少请求参数，"+fieldError.getField()+":"+fieldError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResponse handleConstraintViolationException(ConstraintViolationException e){
        logger.error("缺少请求参数", e);
        final Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        final ConstraintViolation<?> violation = constraintViolations.iterator().next();
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "缺少请求参数，"+violation.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public CommonResponse handleValidationException(ValidationException e){
        logger.error("参数验证失败", e);
        return CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "参数验证失败，"+e.getMessage());
    }

    /**
     * 405
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
        logger.error("不支持当前请求方法", e);
        return CommonResponse.of(HttpStatus.METHOD_NOT_ALLOWED.value(), "不支持当前请求方法，"+e.getMessage());
    }

    /**
     * 415
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public CommonResponse handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e){
        logger.error("不支持当前媒体类型", e);
        return CommonResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "不支持当前媒体类型，"+e.getMessage());
    }

    /**
     * 自定义业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public CommonResponse businessExceptionHandler(BaseException e){
        logger.error("自定义业务执行失败", e);
        return CommonResponse.of(e.getCode(), e.getDefaultMessage());
    }

    /**
     * 其他异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public CommonResponse defaultErrorHandler(Exception e){
        logger.error("服务执行出错", e);
        return CommonResponse.of(500, "服务执行出错，"+e.getMessage());
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public CommonResponse handleMissingServletRequestParameterException(MaxUploadSizeExceededException e){
        logger.error("上传文件太大", e);
        return CommonResponse.of(HttpStatus.PAYLOAD_TOO_LARGE.value(), "上传文件太大，");
    }


}
