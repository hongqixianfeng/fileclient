package com.fiberhome.filemanager.exception;

/**
 * @description 统一的异常处理类
 * @author X6946
 * @date 2025/5/22 16:48
 * @version 1.0
 */
public class FileManagerException extends RuntimeException {

    private int code;
    private String msg;
    private String moduleName;
    private String methodId;
    private String moduleVersion;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public FileManagerException(int statusCode, String msg) {
        this.code = statusCode;
        this.msg = msg;
    }

    public FileManagerException(String msg) {
        super(msg);
        this.msg = msg;
    }

    /**
     *
     * @param statusCode
     * @param msg
     * @param moduleName
     * @param methodId
     * @param moduleVersion
     */
    public FileManagerException(int statusCode, String msg, String moduleName, String methodId, String moduleVersion) {
        this.code = statusCode;
        this.msg = msg;
        this.moduleName = moduleName;
        this.methodId = methodId;
        this.moduleVersion = moduleVersion;
    }
}
