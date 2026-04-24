package com.fiberhome.filemanager.fileclient.strategy.impl;

import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;
import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import com.fiberhome.filemanager.fileclient.bean.FileClientType;
import com.fiberhome.filemanager.fileclient.strategy.FileClientStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class FtpFileClientStrategy implements FileClientStrategy {

    @Override
    public boolean supports(FileClientType type) {
        return FileClientType.FTP == type;
    }

    @Override
    public FileQueryResult query(FileClientConfig config, String directoryPath) {
        validateFtpConfig(config);
        FTPClient ftpClient = connectAndLogin(config);
        try {
            if (StringUtils.isNotBlank(directoryPath)) {
                if (!ftpClient.changeWorkingDirectory(directoryPath)) {
                    throw new FileManagerException("无法切换到FTP目录: " + directoryPath);
                }
            }

            FTPFile[] files = ftpClient.listFiles();
            List<String> names = new ArrayList<>();
            List<FileQueryItem> items = new ArrayList<>();

            String basePath = StringUtils.defaultString(directoryPath, "");
            if (StringUtils.isNotBlank(basePath) && !basePath.endsWith("/")) {
                basePath += "/";
            }
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }

            for (FTPFile file : files) {
                if (file.isFile()) {
                    names.add(file.getName());
                    String uri = "ftp://" + config.getFtpHost() + ":" + config.getFtpPort() + basePath + file.getName();
                    items.add(new FileQueryItem(file.getName(), uri));
                }
            }
            return new FileQueryResult(items, names, null);
        } catch (IOException e) {
            throw new FileManagerException("FTP 文件查询失败: " + e.getMessage());
        } finally {
            closeFtp(ftpClient);
        }
    }

    @Override
    public FileQueryItem upload(FileClientConfig config, String remotePath, String localPath) {
        validateFtpConfig(config);
        File localFile = buildLocalFile(localPath, true);

        FTPClient ftpClient = connectAndLogin(config);
        try (InputStream is = new FileInputStream(localFile)) {
            String parentPath = extractParentPath(remotePath);
            createAndChangeDir(ftpClient, parentPath);

            String fileName = new File(remotePath).getName();
            if (StringUtils.isBlank(fileName)) {
                fileName = localFile.getName();
            }

            if (!ftpClient.storeFile(fileName, is)) {
                throw new FileManagerException("FTP 文件上传失败: " + remotePath);
            }
            
            String basePath = parentPath;
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            String uri = "ftp://" + config.getFtpHost() + ":" + config.getFtpPort() + basePath + fileName;
            return new FileQueryItem(fileName, uri);
        } catch (IOException e) {
            throw new FileManagerException("FTP 文件上传失败: " + e.getMessage());
        } finally {
            closeFtp(ftpClient);
        }
    }

    @Override
    public void download(FileClientConfig config, String remotePath, String localPath) {
        validateFtpConfig(config);
        File localFile = buildLocalFile(localPath, false);

        FTPClient ftpClient = connectAndLogin(config);
        try {
            FileUtils.forceMkdirParent(localFile);
            try (OutputStream os = new FileOutputStream(localFile)) {
                if (!ftpClient.retrieveFile(remotePath, os)) {
                    throw new FileManagerException("FTP 文件下载失败: " + remotePath);
                }
            }
        } catch (IOException e) {
            throw new FileManagerException("FTP 文件下载失败: " + e.getMessage());
        } finally {
            closeFtp(ftpClient);
        }
    }

    @Override
    public void delete(FileClientConfig config, String remotePath) {
        validateFtpConfig(config);
        FTPClient ftpClient = connectAndLogin(config);
        try {
            if (!ftpClient.deleteFile(remotePath)) {
                throw new FileManagerException("FTP 文件删除失败: " + remotePath);
            }
        } catch (IOException e) {
            throw new FileManagerException("FTP 文件删除失败: " + e.getMessage());
        } finally {
            closeFtp(ftpClient);
        }
    }

    private FTPClient connectAndLogin(FileClientConfig config) {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(config.getFtpEncoding());
        ftpClient.setConnectTimeout(config.getTimeout());
        ftpClient.setDataTimeout(config.getTimeout());
        try {
            ftpClient.connect(config.getFtpHost(), config.getFtpPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                throw new FileManagerException("FTP 服务器拒绝连接");
            }
            if (!ftpClient.login(config.getFtpUsername(), config.getFtpPassword())) {
                ftpClient.disconnect();
                throw new FileManagerException("FTP 登录失败");
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException e) {
            throw new FileManagerException("FTP 连接失败: " + e.getMessage());
        }
    }

    private void closeFtp(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ignored) {
            }
        }
    }

    private void createAndChangeDir(FTPClient ftpClient, String path) throws IOException {
        if (StringUtils.isBlank(path) || "/".equals(path)) {
            ftpClient.changeWorkingDirectory("/");
            return;
        }
        ftpClient.changeWorkingDirectory("/");
        String[] dirs = path.split("/");
        for (String dir : dirs) {
            if (StringUtils.isNotBlank(dir)) {
                if (!ftpClient.changeWorkingDirectory(dir)) {
                    ftpClient.makeDirectory(dir);
                    ftpClient.changeWorkingDirectory(dir);
                }
            }
        }
    }

    private String extractParentPath(String remotePath) {
        String normalized = StringUtils.defaultString(remotePath);
        int index = normalized.lastIndexOf("/");
        if (index <= 0) {
            return "/";
        }
        return normalized.substring(0, index);
    }

    private void validateFtpConfig(FileClientConfig config) {
        if (StringUtils.isBlank(config.getFtpHost())) {
            throw new FileManagerException("FTP 模式下 config.ftpHost 不能为空");
        }
        if (StringUtils.isBlank(config.getFtpUsername())) {
            throw new FileManagerException("FTP 模式下 config.ftpUsername 不能为空");
        }
        if (StringUtils.isBlank(config.getFtpPassword())) {
            throw new FileManagerException("FTP 模式下 config.ftpPassword 不能为空");
        }
        if (config.getFtpPort() == null) {
            config.setFtpPort(21);
        }
        if (StringUtils.isBlank(config.getFtpEncoding())) {
            config.setFtpEncoding("UTF-8");
        }
    }
}
