package com.fiberhome.filemanager.fileclient.config;

import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file-client")
public class FileClientProperties {
    private Upload upload = new Upload();

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public static class Upload {
        /**
         * 默认上传目标（远端文件目录或完整文件路径）
         */
        private String remotePath;
        /**
         * 默认上传所使用的文件服务器配置
         */
        private FileClientConfig config = new FileClientConfig();

        /**
         * 限制上传文件大小，默认 10MB (以字节为单位，10 * 1024 * 1024)
         */
        private Long maxSize = 10485760L;

        /**
         * 独立的文件服务下载基准URL（例如: http://file.service.com/download/）
         */
        private String downloadBaseUrl;

        public String getDownloadBaseUrl() {
            return downloadBaseUrl;
        }

        public void setDownloadBaseUrl(String downloadBaseUrl) {
            this.downloadBaseUrl = downloadBaseUrl;
        }

        public String getRemotePath() {
            return remotePath;
        }

        public void setRemotePath(String remotePath) {
            this.remotePath = remotePath;
        }

        public FileClientConfig getConfig() {
            return config;
        }

        public void setConfig(FileClientConfig config) {
            this.config = config;
        }

        public Long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Long maxSize) {
            this.maxSize = maxSize;
        }
    }
}
