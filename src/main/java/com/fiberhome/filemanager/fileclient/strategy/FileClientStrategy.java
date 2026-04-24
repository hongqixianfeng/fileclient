package com.fiberhome.filemanager.fileclient.strategy;

import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;
import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import com.fiberhome.filemanager.fileclient.bean.FileClientType;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public interface FileClientStrategy {
    boolean supports(FileClientType type);

    FileQueryResult query(FileClientConfig config, String directoryPath);

    FileQueryItem upload(FileClientConfig config, String remotePath, String localPath);

    void download(FileClientConfig config, String remotePath, String localPath);

    void delete(FileClientConfig config, String remotePath);
    
    default File buildLocalFile(String localPath, boolean requiredExists) {
        if (StringUtils.isBlank(localPath)) {
            throw new FileManagerException("localPath 不能为空");
        }
        File localFile = new File(localPath);
        if (requiredExists && (!localFile.exists() || !localFile.isFile())) {
            throw new FileManagerException("本地文件不存在: " + localPath);
        }
        return localFile;
    }
}
