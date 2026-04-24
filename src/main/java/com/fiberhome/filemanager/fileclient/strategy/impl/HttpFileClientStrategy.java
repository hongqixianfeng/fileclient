package com.fiberhome.filemanager.fileclient.strategy.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.bean.FileQueryResult;
import com.fiberhome.filemanager.fileclient.bean.FileClientConfig;
import com.fiberhome.filemanager.fileclient.bean.FileClientType;
import com.fiberhome.filemanager.fileclient.strategy.FileClientStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HttpFileClientStrategy implements FileClientStrategy {
    @Override
    public boolean supports(FileClientType type) {
        return FileClientType.HTTP == type;
    }

    @Override
    public FileQueryResult query(FileClientConfig config, String directoryPath) {
        validateHttpConfig(config);
        String url = buildHttpUrl(config.getHttpBaseUrl(), directoryPath);
        HttpRequest request = HttpRequest.get(url).timeout(config.getTimeout());
        appendHeaders(request, config.getHeaders());
        try (HttpResponse response = request.execute()) {
            checkHttpStatus(response, "HTTP 文件查询失败");
            String body = response.body();
            List<String> names = StringUtils.isBlank(body) ? Collections.emptyList() :
                    Arrays.stream(body.split("\\r?\\n")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            List<FileQueryItem> items = new ArrayList<>();
            for (String name : names) {
                items.add(new FileQueryItem(name, buildHttpUrl(url, name)));
            }
            if (items.isEmpty()) {
                items.add(new FileQueryItem(url, url));
            }
            return new FileQueryResult(items, names, body);
        }
    }

    @Override
    public FileQueryItem upload(FileClientConfig config, String remotePath, String localPath) {
        validateHttpConfig(config);
        File localFile = buildLocalFile(localPath, true);
        String url = buildHttpUrl(config.getHttpBaseUrl(), remotePath);
        try {
            HttpRequest request = HttpRequest.put(url).timeout(config.getTimeout()).body(FileUtils.readFileToByteArray(localFile));
            appendHeaders(request, config.getHeaders());
            try (HttpResponse response = request.execute()) {
                checkHttpStatus(response, "HTTP 文件上传失败");
            }
            
            String fileName = new File(remotePath).getName();
            if (StringUtils.isBlank(fileName)) {
                fileName = remotePath;
            }
            return new FileQueryItem(fileName, url);
        } catch (IOException e) {
            throw new FileManagerException("HTTP 文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void download(FileClientConfig config, String remotePath, String localPath) {
        validateHttpConfig(config);
        File localFile = buildLocalFile(localPath, false);
        String url = buildHttpUrl(config.getHttpBaseUrl(), remotePath);
        HttpRequest request = HttpRequest.get(url).timeout(config.getTimeout());
        appendHeaders(request, config.getHeaders());
        try (HttpResponse response = request.execute()) {
            checkHttpStatus(response, "HTTP 文件下载失败");
            FileUtils.forceMkdirParent(localFile);
            FileUtils.writeByteArrayToFile(localFile, response.bodyBytes());
        } catch (IOException e) {
            throw new FileManagerException("HTTP 文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(FileClientConfig config, String remotePath) {
        validateHttpConfig(config);
        String url = buildHttpUrl(config.getHttpBaseUrl(), remotePath);
        HttpRequest request = HttpRequest.delete(url).timeout(config.getTimeout());
        appendHeaders(request, config.getHeaders());
        try (HttpResponse response = request.execute()) {
            checkHttpStatus(response, "HTTP 文件删除失败");
        }
    }

    private String buildHttpUrl(String baseUrl, String remotePath) {
        if (StringUtils.isBlank(remotePath)) {
            return baseUrl;
        }
        if (remotePath.startsWith("http://") || remotePath.startsWith("https://")) {
            return remotePath;
        }
        String left = StringUtils.removeEnd(baseUrl, "/");
        String right = StringUtils.removeStart(remotePath, "/");
        return left + "/" + right;
    }

    private void appendHeaders(HttpRequest request, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        request.addHeaders(headers);
    }

    private void checkHttpStatus(HttpResponse response, String message) {
        int status = response.getStatus();
        if (status < 200 || status >= 300) {
            throw new FileManagerException(message + ": status=" + status + ", body=" + response.body());
        }
    }

    private void validateHttpConfig(FileClientConfig config) {
        if (StringUtils.isBlank(config.getHttpBaseUrl())) {
            throw new FileManagerException("HTTP 模式下 config.httpBaseUrl 不能为空");
        }
        if (config.getTimeout() == null || config.getTimeout() <= 0) {
            config.setTimeout(30000);
        }
    }
}
