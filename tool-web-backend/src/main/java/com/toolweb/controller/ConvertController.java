package com.toolweb.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConvertController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConvertController.class);

    @Value("${file.upload.dir:${user.home}/uploads}")
    private String uploadDir;

    public ConvertController() {
        System.out.println("==== ConvertController 已加载 ====");
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convertFile(@RequestParam("file") MultipartFile file,
                                       @RequestParam("targetFormat") String targetFormat) {
        System.out.println("【后端】收到文件转换请求，文件名: " + file.getOriginalFilename() + ", 目标格式: " + targetFormat);
        logger.debug("【后端】收到文件转换请求，文件名: {}, 目标格式: {}", file.getOriginalFilename(), targetFormat);
        
        try {
            // 创建上传目录
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            logger.debug("上传目录：{}", directory.getAbsolutePath());

            // 生成唯一文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + targetFormat;
            
            // 保存上传的文件
            Path uploadPath = Paths.get(uploadDir, originalFilename);
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("【后端】保存上传文件到: " + uploadPath);
            logger.debug("原始文件已保存：{}", uploadPath);

            // TODO: 实现实际的文件转换逻辑
            // 这里暂时只是复制文件作为示例
            Path convertedPath = Paths.get(uploadDir, newFilename);
            Files.copy(uploadPath, convertedPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("【后端】转换后文件保存到: " + convertedPath);
            logger.debug("转换后文件已保存：{}", convertedPath);

            // 构建文件URL
            String fileUrl = "/api/files/" + newFilename;
            System.out.println("【后端】返回下载链接: " + fileUrl);
            logger.debug("生成下载URL：{}", fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("message", "文件转换成功");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("【后端】文件转换失败: " + e.getMessage());
            logger.error("【后端】文件转换失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "文件转换失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        System.out.println("【后端】收到文件下载请求: " + filename);
        try {
            Path filePath = Paths.get(uploadDir, filename);
            byte[] data = Files.readAllBytes(filePath);
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(data);
        } catch (IOException e) {
            logger.error("文件下载失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 