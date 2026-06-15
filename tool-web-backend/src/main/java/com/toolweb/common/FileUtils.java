package com.toolweb.common;

import cn.hutool.core.io.FileTypeUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FileUtils {
    public static String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > -1 ? filename.substring(lastDotIndex) : "";
    }

    public static String getFileType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return FileTypeUtil.getType(inputStream);
        } catch (IOException e) {
            return "";
        }
    }

    public static boolean isImageFile(String fileType) {
        return fileType.equalsIgnoreCase("jpg") ||
               fileType.equalsIgnoreCase("jpeg") ||
               fileType.equalsIgnoreCase("png") ||
               fileType.equalsIgnoreCase("gif") ||
               fileType.equalsIgnoreCase("bmp") ||
               fileType.equalsIgnoreCase("webp");
    }

    public static boolean isCompressedFile(String fileType) {
        return fileType.equalsIgnoreCase("zip") ||
               fileType.equalsIgnoreCase("rar") ||
               fileType.equalsIgnoreCase("7z");
    }
} 