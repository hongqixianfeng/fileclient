package com.fiberhome.filemanager.fileclient.bean;

import java.util.ArrayList;
import java.util.List;

public class FileQueryResult {
    private List<FileQueryItem> items = new ArrayList<>();
    private List<String> fileNames = new ArrayList<>();
    private String responseBody;

    public FileQueryResult() {
    }

    public FileQueryResult(List<FileQueryItem> items, List<String> fileNames, String responseBody) {
        this.items = items;
        this.fileNames = fileNames;
        this.responseBody = responseBody;
    }

    public List<FileQueryItem> getItems() {
        return items;
    }

    public void setItems(List<FileQueryItem> items) {
        this.items = items;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
