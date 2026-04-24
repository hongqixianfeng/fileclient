package com.nuts.framework.base;

public class Response<T> {
    private int code;
    private String message;
    private T data;
    private Page page;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }
    
    public static <T> Response<T> ok() {
        return success(null);
    }

    public static <T> Response<T> ok(T data) {
        return success(data);
    }

    public static <T> Response<T> ok(T data, Page page) {
        Response<T> response = success(data);
        response.setPage(page);
        return response;
    }
    
    public static <T> Response<T> error(String msg) {
        Response<T> response = new Response<>();
        response.setCode(500);
        response.setMessage(msg);
        return response;
    }

    public static <T> Response<T> error() {
        return error("error");
    }

    public static <T> Response<T> error(T data) {
        Response<T> response = error("error");
        response.setData(data);
        return response;
    }
    
    public static <T> Response<T> error(int code, String msg) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMessage(msg);
        return response;
    }

    public static <T> Response<T> error(int code, String msg, T data) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMessage(msg);
        response.setData(data);
        return response;
    }
}
