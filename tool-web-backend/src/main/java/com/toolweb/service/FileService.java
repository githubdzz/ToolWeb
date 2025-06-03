package com.toolweb.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    String uploadFile(MultipartFile file) throws Exception;
    
    InputStream downloadFile(String fileName) throws Exception;
    
    void deleteFile(String fileName) throws Exception;
    
    String convertFile(MultipartFile file, String targetFormat) throws Exception;
    
    String compressFiles(MultipartFile[] files, String format, String password) throws Exception;
    
    String decompressFile(MultipartFile file, String password) throws Exception;
    
    String encryptFile(MultipartFile file, String password) throws Exception;
    
    String decryptFile(MultipartFile file, String password) throws Exception;
    
    String generateQRCode(String content, String color, MultipartFile logo) throws Exception;
} 