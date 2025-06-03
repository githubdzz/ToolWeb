package com.toolweb.controller;

import com.toolweb.common.Result;
import com.toolweb.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Tag(name = "文件操作接口")
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @Operation(summary = "文件格式转换")
    @PostMapping("/convert")
    public Result<String> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetFormat") String targetFormat) {
        try {
            String fileName = fileService.convertFile(file, targetFormat);
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "文件压缩")
    @PostMapping("/compress")
    public Result<String> compressFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("format") String format,
            @RequestParam(value = "password", required = false) String password) {
        try {
            String fileName = fileService.compressFiles(files, format, password);
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "文件解压")
    @PostMapping("/decompress")
    public Result<String> decompressFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password) {
        try {
            String outputDir = fileService.decompressFile(file, password);
            return Result.success(outputDir);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "文件加密")
    @PostMapping("/encrypt")
    public Result<String> encryptFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try {
            String fileName = fileService.encryptFile(file, password);
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "文件解密")
    @PostMapping("/decrypt")
    public Result<String> decryptFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        try {
            String fileName = fileService.decryptFile(file, password);
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "生成二维码")
    @PostMapping("/qrcode")
    public Result<String> generateQRCode(
            @RequestParam("content") String content,
            @RequestParam("color") String color,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            String fileName = fileService.generateQRCode(content, color, logo);
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            InputStream inputStream = fileService.downloadFile(fileName);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 