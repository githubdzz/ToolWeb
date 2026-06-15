package com.toolweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.toolweb.service.FileService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class CompressController {

    private static final Logger log = LoggerFactory.getLogger(CompressController.class);

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> compressFiles(@RequestParam("files") List<MultipartFile> files,
                                           @RequestParam(value = "format", defaultValue = "zip") String format,
                                           @RequestParam(value = "password", required = false) String password) {
        try {
            String fileName = fileService.compressFiles(files.toArray(new MultipartFile[0]), format, password);
            Map<String, String> response = new HashMap<>();
            response.put("url", "/api/files/" + fileName);
            response.put("message", "压缩成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "压缩失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping(value = "/decompress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> decompressFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "password", required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            String outputDir = fileService.decompressFile(file, password);
            response.put("success", true);
            response.put("outputDir", outputDir);
            response.put("message", "解压成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "解压失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/files/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) {
        try (InputStream is = fileService.downloadFile(fileName)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
} 