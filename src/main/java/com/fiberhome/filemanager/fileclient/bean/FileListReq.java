package com.fiberhome.filemanager.fileclient.bean;

public class FileListReq {
    private FileClientConfig config;
    private String directoryPath;

    public FileClientConfig getConfig() {
        return config;
    }

    public void setConfig(FileClientConfig config) {
        this.config = config;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
}
