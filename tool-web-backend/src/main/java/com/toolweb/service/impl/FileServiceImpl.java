package com.toolweb.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.symmetric.AES;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.toolweb.common.FileUtils;
import com.toolweb.service.FileService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = FileUtils.generateFileName(file.getOriginalFilename());
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return fileName;
    }

    @Override
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .build());
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .build());
    }

    @Override
    public String convertFile(MultipartFile file, String targetFormat) throws Exception {
        String fileType = FileUtils.getFileType(file);
        if (FileUtils.isImageFile(fileType)) {
            return convertImage(file, targetFormat);
        }
        throw new UnsupportedOperationException("Unsupported file type for conversion");
    }

    @Override
    public String compressFiles(MultipartFile[] files, String format, String password) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String zipFileName = UUID.randomUUID().toString() + "." + format;
        Path zipPath = Path.of(tempDir, zipFileName);

        ZipParameters parameters = new ZipParameters();
        if (password != null && !password.isEmpty()) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
        }

        try (ZipFile zipFile = new ZipFile(zipPath.toFile(), password != null ? password.toCharArray() : null)) {
            for (MultipartFile file : files) {
                Path tempFile = Path.of(tempDir, file.getOriginalFilename());
                file.transferTo(tempFile);
                zipFile.addFile(tempFile.toFile(), parameters);
                Files.delete(tempFile);
            }
        }

        String fileName = FileUtils.generateFileName(zipPath.getFileName().toString());
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(new FileInputStream(zipPath.toFile()), Files.size(zipPath), -1)
                .contentType("application/zip")
                .build());
        Files.delete(zipPath);
        return fileName;
    }

    @Override
    public String decompressFile(MultipartFile file, String password) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = FileUtils.generateFileName(file.getOriginalFilename());
        Path filePath = Path.of(tempDir, fileName);
        file.transferTo(filePath);

        String outputDir = tempDir + "/" + UUID.randomUUID().toString();
        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            if (zipFile.isEncrypted() && password != null) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(outputDir);
        }

        Files.delete(filePath);
        return outputDir;
    }

    @Override
    public String encryptFile(MultipartFile file, String password) throws Exception {
        byte[] fileContent = file.getBytes();
        AES aes = new AES(password.getBytes());
        byte[] encryptedContent = aes.encrypt(fileContent);

        String fileName = FileUtils.generateFileName(file.getOriginalFilename() + ".enc");
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(new ByteArrayInputStream(encryptedContent), encryptedContent.length, -1)
                .build());

        return fileName;
    }

    @Override
    public String decryptFile(MultipartFile file, String password) throws Exception {
        byte[] fileContent = file.getBytes();
        AES aes = new AES(password.getBytes());
        byte[] decryptedContent = aes.decrypt(fileContent);

        String fileName = FileUtils.generateFileName(file.getOriginalFilename().replace(".enc", ""));
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(new ByteArrayInputStream(decryptedContent), decryptedContent.length, -1)
                .build());

        return fileName;
    }

    @Override
    public String generateQRCode(String content, String color, MultipartFile logo) throws Exception {
        int width = 300;
        int height = 300;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Color qrColor = Color.decode(color);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? qrColor.getRGB() : Color.WHITE.getRGB());
            }
        }

        if (logo != null) {
            BufferedImage logoImage = ImageIO.read(logo.getInputStream());
            int logoWidth = width / 5;
            int logoHeight = height / 5;
            int logoX = (width - logoWidth) / 2;
            int logoY = (height - logoHeight) / 2;

            BufferedImage resizedLogo = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedLogo.createGraphics();
            g2d.drawImage(logoImage, 0, 0, logoWidth, logoHeight, null);
            g2d.dispose();

            Graphics2D qrGraphics = qrImage.createGraphics();
            qrGraphics.drawImage(resizedLogo, logoX, logoY, null);
            qrGraphics.dispose();
        }

        String fileName = FileUtils.generateFileName("qrcode.png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                .contentType("image/png")
                .build());

        return fileName;
    }

    private String convertImage(MultipartFile file, String targetFormat) throws Exception {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (targetFormat.equalsIgnoreCase("jpg") || targetFormat.equalsIgnoreCase("jpeg")) {
            BufferedImage newImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(originalImage, 0, 0, Color.WHITE, null);
            ImageIO.write(newImage, targetFormat, outputStream);
        } else {
            ImageIO.write(originalImage, targetFormat, outputStream);
        }

        String fileName = FileUtils.generateFileName("converted." + targetFormat);
        byte[] imageBytes = outputStream.toByteArray();

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(fileName)
                .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                .contentType("image/" + targetFormat)
                .build());

        return fileName;
    }
} 