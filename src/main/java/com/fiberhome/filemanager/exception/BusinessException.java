package com.fiberhome.filemanager.exception;

/**
 * @Author ouyongyang
 * @Description 业务异常
 * Date 2021/8/16 11:22
 **/
public class BusinessException extends RuntimeException {

    private String message;
    private Integer code;
    private Throwable cause;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        this.message = message;
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
