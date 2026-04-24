package com.fiberhome.filemanager.exception;

import com.google.common.collect.Maps;
import com.nuts.framework.base.Response;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.Map;


/**
 * @description 统一的异常处理类
 * @author X6946
 * @date 2025/5/22 16:48
 * @version 1.0
 */
@RestControllerAdvice
public class ControllerExceptionAdvice {


    private final static Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response handler(MethodArgumentNotValidException ex) {
        LOGGER.error("参数校验异常!");
        Map<String, String> errors = Maps.newHashMap();
        ex.getBindingResult().getAllErrors().forEach(
                error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                }
        );
        return Response.error( HttpServletResponse.SC_BAD_REQUEST, "参数校验异常!", errors);
    }

    @ExceptionHandler({FileManagerException.class})
    @ResponseBody
    public Response exceptionHandle(FileManagerException e) {
        LOGGER.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }

    @ExceptionHandler({PSQLException.class})
    public Response exceptionHandle(PSQLException e) {
        LOGGER.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }

    @ExceptionHandler({ArithmeticException.class})
    public Response exceptionHandle(ArithmeticException e) {
        LOGGER.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public Response exceptionHandle(ConstraintViolationException e) {
        LOGGER.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Response handleBizException(BusinessException e) {
        LOGGER.error(e.getMessage(), e);
        return Response.error(e.getMessage());
    }
}
