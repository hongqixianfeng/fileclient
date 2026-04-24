package com.fiberhome.filemanager.fileclient.bean;

public class FileOperationReq {
    private FileClientConfig config;
    private String remotePath;
    private String localPath;

    public FileClientConfig getConfig() {
        return config;
    }

    public void setConfig(FileClientConfig config) {
        this.config = config;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
