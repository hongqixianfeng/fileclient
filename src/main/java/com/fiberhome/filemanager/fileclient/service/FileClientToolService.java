package com.fiberhome.filemanager.fileclient.service;

import com.fiberhome.filemanager.fileclient.bean.FileListReq;
import com.fiberhome.filemanager.fileclient.bean.FileOperationReq;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;

public interface FileClientToolService {
    FileQueryResult query(FileListReq req);

    FileQueryItem upload(FileOperationReq req);
    
    FileQueryItem uploadLocalFile(String localFilePath, String originalFileName);

    void download(FileOperationReq req);

    void delete(FileOperationReq req);
}
