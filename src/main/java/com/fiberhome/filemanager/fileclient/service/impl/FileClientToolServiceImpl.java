package com.fiberhome.filemanager.fileclient.service.impl;

import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileListReq;
import com.fiberhome.filemanager.fileclient.bean.FileOperationReq;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;
import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import com.fiberhome.filemanager.fileclient.bean.FileClientType;
import com.fiberhome.filemanager.fileclient.config.FileClientProperties;
import com.fiberhome.filemanager.fileclient.service.FileClientToolService;
import com.fiberhome.filemanager.fileclient.strategy.FileClientStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileClientToolServiceImpl implements FileClientToolService {
    @Autowired
    private List<FileClientStrategy> fileClientStrategies;

    @Autowired
    private FileClientProperties fileClientProperties;

    @Override
    public FileQueryResult query(FileListReq req) {
        FileClientConfig config = validateConfig(req == null ? null : req.getConfig());
        String directoryPath = req == null ? null : req.getDirectoryPath();
        return resolveStrategy(config.getType()).query(config, directoryPath);
    }

    @Override
    public FileQueryItem upload(FileOperationReq req) {
        validateOperationReq(req, true);
        FileClientConfig config = req.getConfig();
        return resolveStrategy(config.getType()).upload(config, req.getRemotePath(), req.getLocalPath());
    }

    @Override
    public FileQueryItem uploadLocalFile(String localFilePath, String originalFileName) {
        FileClientProperties.Upload uploadConfig = fileClientProperties.getUpload();
        if (uploadConfig == null) {
            throw new FileManagerException("配置文件中未读取到 file-client.upload");
        }
        FileClientConfig config = uploadConfig.getConfig();
        if (config == null || config.getType() == null) {
            throw new FileManagerException("配置文件中未读取到 file-client.upload.config.type");
        }
        String remotePath = uploadConfig.getRemotePath();
        if (StringUtils.isBlank(remotePath)) {
            throw new FileManagerException("配置文件中未读取到 file-client.upload.remote-path");
        }

        FileOperationReq req = new FileOperationReq();
        req.setConfig(config);
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String finalRemotePath = remotePath.endsWith("/") ? (remotePath + uuid + "/" + originalFileName) : (remotePath + "/" + uuid + "/" + originalFileName);
        req.setRemotePath(finalRemotePath);
        req.setLocalPath(localFilePath);
        
        FileQueryItem result = upload(req);
        
        String downloadBaseUrl = uploadConfig.getDownloadBaseUrl();
        if (StringUtils.isNotBlank(downloadBaseUrl)) {
            if (!downloadBaseUrl.endsWith("/")) {
                downloadBaseUrl += "/";
            }
            if (finalRemotePath.startsWith("/")) {
                finalRemotePath = finalRemotePath.substring(1);
            }
            result.setUri(downloadBaseUrl + finalRemotePath);
        }
        return result;
    }

    @Override
    public void download(FileOperationReq req) {
        validateOperationReq(req, true);
        FileClientConfig config = req.getConfig();
        resolveStrategy(config.getType()).download(config, req.getRemotePath(), req.getLocalPath());
    }

    @Override
    public void delete(FileOperationReq req) {
        validateOperationReq(req, false);
        FileClientConfig config = req.getConfig();
        resolveStrategy(config.getType()).delete(config, req.getRemotePath());
    }

    private FileClientStrategy resolveStrategy(FileClientType type) {
        for (FileClientStrategy strategy : fileClientStrategies) {
            if (strategy.supports(type)) {
                return strategy;
            }
        }
        throw new FileManagerException("不支持的文件服务器类型: " + type);
    }

    private FileClientConfig validateConfig(FileClientConfig config) {
        if (config == null) {
            throw new FileManagerException("config 不能为空");
        }
        if (config.getType() == null) {
            throw new FileManagerException("config.type 不能为空");
        }
        return config;
    }

    private void validateOperationReq(FileOperationReq req, boolean requiredLocalPath) {
        if (req == null) {
            throw new FileManagerException("请求参数不能为空");
        }
        validateConfig(req.getConfig());
        if (StringUtils.isBlank(req.getRemotePath())) {
            throw new FileManagerException("remotePath 不能为空");
        }
        if (requiredLocalPath && StringUtils.isBlank(req.getLocalPath())) {
            throw new FileManagerException("localPath 不能为空");
        }
    }
}
