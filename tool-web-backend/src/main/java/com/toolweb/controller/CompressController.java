package com.toolweb.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CompressController {

    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> compressFiles(@RequestParam("files") List<MultipartFile> files,
                                           @RequestParam(value = "format", defaultValue = "zip") String format,
                                           @RequestParam(value = "password", required = false) String password) {
        // TODO: 实现压缩逻辑，返回压缩包下载链接
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/files/mock_compressed.zip");
        response.put("message", "压缩成功（模拟）");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/decompress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> decompressFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "password", required = false) String password) {
        // TODO: 实现解压逻辑，返回解压后文件下载链接或列表
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/files/mock_decompressed/");
        response.put("message", "解压成功（模拟）");
        return ResponseEntity.ok(response);
    }
} 