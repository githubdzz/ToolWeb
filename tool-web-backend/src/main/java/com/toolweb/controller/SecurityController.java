package com.toolweb.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecurityController {

    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> encryptFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("password") String password) {
        // TODO: 实现加密逻辑，返回加密文件下载链接
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/files/mock_encrypted.enc");
        response.put("message", "加密成功（模拟）");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> decryptFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("password") String password) {
        // TODO: 实现解密逻辑，返回解密文件下载链接
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/files/mock_decrypted.txt");
        response.put("message", "解密成功（模拟）");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/qrcode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateQRCode(@RequestParam("content") String content,
                                            @RequestParam(value = "color", required = false) String color,
                                            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        // TODO: 生成二维码图片，返回图片下载/预览链接
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/files/mock_qrcode.png");
        response.put("message", "二维码生成成功（模拟）");
        return ResponseEntity.ok(response);
    }
} 