package com.fiberhome.filemanager.fileclient.controller;

import com.fiberhome.filemanager.exception.FileManagerException;
import com.fiberhome.filemanager.fileclient.bean.FileListReq;
import com.fiberhome.filemanager.fileclient.bean.FileOperationReq;
import com.fiberhome.filemanager.fileclient.bean.FileQueryItem;
import com.fiberhome.filemanager.fileclient.config.FileClientProperties;
import com.fiberhome.filemanager.fileclient.service.FileClientToolService;
import com.nuts.framework.base.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/file-client")
@Tag(name = "文件服务器工具模块")
@Validated
public class FileClientToolController {
    @Autowired
    private FileClientToolService fileClientToolService;

    @Autowired
    private FileClientProperties fileClientProperties;

    @Autowired
    private com.fiberhome.filemanager.fileclient.service.UploadProgressManager uploadProgressManager;

    /**
     * 文件查询接口

     * 根据传入的配置信息和目标路径，查询文件服务器（FTP/HTTP/MINIO）上的文件列表。
     *
     * @param req 查询请求参数，包含文件服务器配置(config)及目录路径(directoryPath)
     * @return 包含文件名称和对应 URI 列表的查询结果
     */
    @PostMapping("/query")
    @Operation(summary = "文件查询", description = "响应（code）成功：0，失败：-1")
    public Response query(@RequestBody FileListReq req) {
        return Response.ok(fileClientToolService.query(req));
    }

    /**
     * 查询文件上传到文件服务器的进度（服务端转存阶段）
     * 注意：由于 SpringBoot Multipart 机制，浏览器到服务端的上传进度应直接通过前端原生 XMLHttpRequest / Axios 获取。
     * 本接口仅提供“服务端 -> FTP/MinIO” 阶段的转存进度（0-100）。
     *
     * @param uploadId 上传的唯一任务标识
     * @return 进度百分比 (0-100)
     */
    @GetMapping("/upload/progress")
    @Operation(summary = "查询服务端转存进度", description = "响应（code）成功：0，返回0-100的整数。注意：前端上传到服务端的进度请使用 XHR 原生 onUploadProgress")
    public Response getUploadProgress(@RequestParam("uploadId") String uploadId) {
        return Response.ok(uploadProgressManager.getProgress(uploadId));
    }

    /**
     * 文件上传接口
     * 接收客户端直接上传的 MultipartFile 文件数据，并根据配置文件中的 file-client.upload 将文件转存到远端服务器。
     * 
     * @param file 客户端上传的二进制文件
     * @param uploadId (可选) 唯一任务标识，用于查询服务端转存进度
     * @return 成功返回标准响应格式，并带有 URI 信息
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传", description = "响应（code）成功：0，失败：-1")
    public Response upload(@RequestParam("file") MultipartFile file, 
                           @RequestParam(value = "uploadId", required = false) String uploadId) {
        if (file == null || file.isEmpty()) {
            throw new FileManagerException("上传文件不能为空");
        }
        
        Long maxSize = fileClientProperties.getUpload().getMaxSize();
        if (maxSize != null && file.getSize() > maxSize) {
            throw new FileManagerException("上传文件大小超过限制，最大允许: " + (maxSize / 1024 / 1024) + "MB");
        }
        
        File tempFile = null;
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = file.getOriginalFilename();
            if (org.apache.commons.lang3.StringUtils.isBlank(fileName)) {
                fileName = UUID.randomUUID().toString();
            }
            tempFile = new File(tempDir, UUID.randomUUID().toString() + "_" + fileName);
            file.transferTo(tempFile);
            
            // 开始向文件服务器上传（如果在转存层需要使用进度，可以将 uploadId 透传进去；由于本系统设计为简单的服务代理，当前这里主要提供接口预留，供前端先获取文件转存完成的成功信号）
            if (org.apache.commons.lang3.StringUtils.isNotBlank(uploadId)) {
                uploadProgressManager.setProgress(uploadId, 0); // 转存开始
            }
            
            FileQueryItem result = fileClientToolService.uploadLocalFile(tempFile.getAbsolutePath(), fileName);
            
            if (org.apache.commons.lang3.StringUtils.isNotBlank(uploadId)) {
                uploadProgressManager.setProgress(uploadId, 100); // 转存结束
            }
            return Response.ok(result);
        } catch (Exception e) {
            throw new FileManagerException("文件上传失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                org.apache.commons.io.FileUtils.deleteQuietly(tempFile);
            }
        }
    }

    /**
     * 文件下载接口
     * 从指定的文件服务器（FTP/HTTP/MINIO）下载文件，并直接以二进制流的形式向客户端输出。
     *
     * @param req 下载请求参数，包含文件服务器配置(config)及需下载的远端文件路径(remotePath)
     * @param response HTTP 响应对象，用于设置 Header 以及直接向客户端写入文件流
     */
    @PostMapping("/download")
    @Operation(summary = "文件下载", description = "响应（code）成功：0，失败：-1")
    public void download(@RequestBody FileOperationReq req, HttpServletResponse response) {
        File tempFile = null;
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String remotePath = req.getRemotePath();
            String fileName = "downloaded_file";
            if (org.apache.commons.lang3.StringUtils.isNotBlank(remotePath) && remotePath.contains("/")) {
                fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1);
            } else if (org.apache.commons.lang3.StringUtils.isNotBlank(remotePath)) {
                fileName = remotePath;
            }
            
            tempFile = new File(tempDir, UUID.randomUUID().toString() + "_" + fileName);
            req.setLocalPath(tempFile.getAbsolutePath());
            
            fileClientToolService.download(req);
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
            
            try (InputStream is = new FileInputStream(tempFile);
                 OutputStream os = response.getOutputStream()) {
                org.apache.commons.io.IOUtils.copy(is, os);
                os.flush();
            }
        } catch (Exception e) {
            throw new FileManagerException("文件下载失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                org.apache.commons.io.FileUtils.deleteQuietly(tempFile);
            }
        }
    }

    /**
     * 文件删除接口
     * 从指定的文件服务器（FTP/HTTP/MINIO）中删除目标文件。
     *
     * @param req 删除请求参数，包含文件服务器配置(config)及需删除的远端文件路径(remotePath)
     * @return 成功返回标准响应格式
     */
    @PostMapping("/delete")
    @Operation(summary = "文件删除", description = "响应（code）成功：0，失败：-1")
    public Response delete(@RequestBody FileOperationReq req) {
        fileClientToolService.delete(req);
        return Response.ok();
    }
}
