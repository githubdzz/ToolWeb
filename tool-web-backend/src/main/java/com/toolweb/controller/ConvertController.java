package com.toolweb.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.toolweb.service.ConvertService;
import java.io.*;
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

    @Autowired
    private ConvertService convertService;

    public ConvertController() {
        System.out.println("==== ConvertController 已加载 ====");
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convertFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("targetFormat") String targetFormat,
                                         @RequestParam(value = "sourceFormat", required = false) String sourceFormat) {
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
            String srcFormat = sourceFormat != null ? sourceFormat.toLowerCase() : (fileExtension != null ? fileExtension.toLowerCase() : "");
            String newFilename = UUID.randomUUID().toString() + "." + targetFormat;

            // 保存上传的文件
            Path uploadPath = Paths.get(uploadDir, originalFilename);
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("原始文件已保存：{}", uploadPath);

            // 转换目标文件路径
            Path convertedPath = Paths.get(uploadDir, newFilename);

            // 分发调用不同格式转换
            switch ((srcFormat + "->" + targetFormat).toLowerCase()) {
                case "docx->pdf":
                    convertService.wordToPdf(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "docx->html":
                    // 这里只做简单实现，可用docx4j导出HTML
                    throw new UnsupportedOperationException("docx转html暂未实现");
                case "pdf->docx":
                    convertService.pdfToWord(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "txt->docx":
                    convertService.txtToWord(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "txt->pdf":
                    convertService.txtToPdf(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "xlsx->json":
                    convertService.excelToJson(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "md->html":
                    convertService.markdownToHtml(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "html->pdf":
                    convertService.htmlToPdf(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "csv->json":
                    convertService.csvToJson(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "json->csv":
                    convertService.jsonToCsv(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "yaml->json":
                    convertService.yamlToJson(uploadPath.toFile(), convertedPath.toFile());
                    break;
                case "json->yaml":
                    convertService.jsonToYaml(uploadPath.toFile(), convertedPath.toFile());
                    break;
                default:
                    throw new UnsupportedOperationException("暂不支持该格式转换: " + srcFormat + "->" + targetFormat);
            }

            // 判断目标格式是否为文本，直接返回内容，否则返回下载链接
            String[] textFormats = {"json", "html", "md", "yaml", "txt", "csv"};
            boolean isText = false;
            for (String fmt : textFormats) {
                if (targetFormat.equalsIgnoreCase(fmt)) {
                    isText = true;
                    break;
                }
            }
            if (isText) {
                String content = Files.readString(convertedPath);
                String contentType = "text/plain";
                if (targetFormat.equalsIgnoreCase("json")) contentType = "application/json";
                if (targetFormat.equalsIgnoreCase("html")) contentType = "text/html";
                if (targetFormat.equalsIgnoreCase("yaml")) contentType = "application/x-yaml";
                if (targetFormat.equalsIgnoreCase("csv")) contentType = "text/csv";
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(content);
            } else {
                String fileUrl = "/api/files/" + newFilename;
                Map<String, String> response = new HashMap<>();
                response.put("url", fileUrl);
                response.put("message", "文件转换成功");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("【后端】文件转换失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "文件转换失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/convert/files/{fileName}")
    public ResponseEntity<?> downloadConvertedFile(@PathVariable String fileName) {
        System.out.println("【后端】收到文件下载请求: " + fileName);
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            byte[] data = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(data);
        } catch (IOException e) {
            logger.error("文件下载失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 