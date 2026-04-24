package com.fiberhome.filemanager.fileclient.strategy.impl;

import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;
import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import com.fiberhome.filemanager.fileclient.bean.FileClientType;
import com.fiberhome.filemanager.fileclient.strategy.FileClientStrategy;
import io.minio.DownloadObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.UploadObjectArgs;
import io.minio.messages.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinioFileClientStrategy implements FileClientStrategy {
    @Override
    public boolean supports(FileClientType type) {
         return FileClientType.MINIO == type;
    }

    @Override
    public FileQueryResult query(FileClientConfig config, String directoryPath) {
        validateMinioConfig(config);
        MinioClient minioClient = createMinioClient(config);
        try {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder().bucket(config.getMinioBucket());
            if (StringUtils.isNotBlank(directoryPath)) {
                builder.prefix(directoryPath);
            }
            Iterable<Result<Item>> results = minioClient.listObjects(builder.build());
            List<String> names = new ArrayList<>();
            List<FileQueryItem> items = new ArrayList<>();
            
            String endpoint = config.getMinioEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            String bucketBase = endpoint + config.getMinioBucket() + "/";

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    String objectName = item.objectName();
                    names.add(objectName);
                    items.add(new FileQueryItem(objectName, bucketBase + objectName));
                }
            }
            return new FileQueryResult(items, names, null);
        } catch (Exception e) {
            throw new FileManagerException("MINIO 文件查询失败: " + e.getMessage());
        }
    }

    @Override
    public FileQueryItem upload(FileClientConfig config, String remotePath, String localPath) {
        validateMinioConfig(config);
        File localFile = buildLocalFile(localPath, true);

        MinioClient minioClient = createMinioClient(config);
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(config.getMinioBucket())
                            .object(remotePath)
                            .filename(localFile.getAbsolutePath())
                            .build()
            );
            
            String endpoint = config.getMinioEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            String uri = endpoint + config.getMinioBucket() + "/" + remotePath;
            String fileName = new File(remotePath).getName();
            return new FileQueryItem(fileName, uri);
        } catch (Exception e) {
            throw new FileManagerException("MINIO 文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void download(FileClientConfig config, String remotePath, String localPath) {
        validateMinioConfig(config);
        MinioClient minioClient = createMinioClient(config);
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(config.getMinioBucket())
                            .object(remotePath)
                            .filename(localPath)
                            .build()
            );
        } catch (Exception e) {
            throw new FileManagerException("MINIO 文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(FileClientConfig config, String remotePath) {
        validateMinioConfig(config);
        MinioClient minioClient = createMinioClient(config);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(config.getMinioBucket())
                            .object(remotePath)
                            .build()
            );
        } catch (Exception e) {
            throw new FileManagerException("MINIO 文件删除失败: " + e.getMessage());
        }
    }

    private MinioClient createMinioClient(FileClientConfig config) {
        return MinioClient.builder()
                .endpoint(config.getMinioEndpoint())
                .credentials(config.getMinioAccessKey(), config.getMinioSecretKey())
                .build();
    }

    private void validateMinioConfig(FileClientConfig config) {
        if (StringUtils.isBlank(config.getMinioEndpoint())) {
            throw new FileManagerException("MINIO 模式下 config.minioEndpoint 不能为空");
        }
        if (StringUtils.isBlank(config.getMinioAccessKey())) {
            throw new FileManagerException("MINIO 模式下 config.minioAccessKey 不能为空");
        }
        if (StringUtils.isBlank(config.getMinioSecretKey())) {
            throw new FileManagerException("MINIO 模式下 config.minioSecretKey 不能为空");
        }
        if (StringUtils.isBlank(config.getMinioBucket())) {
            throw new FileManagerException("MINIO 模式下 config.minioBucket 不能为空");
        }
    }
}
