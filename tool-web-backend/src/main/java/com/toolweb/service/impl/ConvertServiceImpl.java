package com.toolweb.service.impl;

import com.toolweb.service.ConvertService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import java.io.*;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.apache.poi.ss.usermodel.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.yaml.snakeyaml.Yaml;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.util.*;
import java.nio.charset.StandardCharsets;
import org.mozilla.universalchardet.UniversalDetector;

@Service
public class ConvertServiceImpl implements ConvertService {
    @Override
    public void pdfToWord(File pdfFile, File outputWordFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile);
             FileOutputStream fos = new FileOutputStream(outputWordFile);
             XWPFDocument doc = new XWPFDocument()) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                String[] lines = pageText.split("\r?\n");
                for (String line : lines) {
                    XWPFParagraph para = doc.createParagraph();
                    para.createRun().setText(line);
                }
            }
            doc.write(fos);
        }
    }

    @Override
    public void wordToPdf(File wordFile, File outputPdfFile) throws Exception {
        org.docx4j.openpackaging.packages.WordprocessingMLPackage wordMLPackage =
                org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(wordFile);
        try (OutputStream os = new FileOutputStream(outputPdfFile)) {
            org.docx4j.convert.out.FOSettings foSettings = org.docx4j.Docx4J.createFOSettings();
            foSettings.setWmlPackage(wordMLPackage);
            org.docx4j.Docx4J.toFO(foSettings, os, org.docx4j.Docx4J.FLAG_EXPORT_PREFER_XSL);
        }
    }

    @Override
    public void txtToWord(File txtFile, File outputWordFile) throws Exception {
        String charset = detectCharset(txtFile);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), charset));
             FileOutputStream fos = new FileOutputStream(outputWordFile);
             XWPFDocument doc = new XWPFDocument()) {
            String line;
            while ((line = reader.readLine()) != null) {
                XWPFParagraph para = doc.createParagraph();
                para.createRun().setText(line);
            }
            doc.write(fos);
        }
    }

    @Override
    public void txtToPdf(File txtFile, File outputPdfFile) throws Exception {
        String charset = detectCharset(txtFile);
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:SimSun;font-size:14px;'>");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            }
        }
        html.append("</body></html>");
        try (FileOutputStream fos = new FileOutputStream(outputPdfFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(fos);
            // 显式注册宋体字体，兼容Windows和Linux
            String simsunPathWin = "C:/Windows/Fonts/simsun.ttc";
            String simsunPathLinux = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"; // 可用思源宋体
            File fontFile = new File(simsunPathWin);
            if (!fontFile.exists()) {
                fontFile = new File(simsunPathLinux);
            }
            if (fontFile.exists()) {
                builder.useFont(fontFile, "SimSun");
            }
            builder.run();
        }
    }

    /**
     * 简单HTML转义，防止内容中有特殊字符影响结构
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @Override
    public void excelToJson(File excelFile, File outputJsonFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis);
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputJsonFile), StandardCharsets.UTF_8)) {
            List<Map<String, Object>> sheetList = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Iterator<Row> rowIterator = sheet.iterator();
                List<String> headers = new ArrayList<>();
                List<Map<String, Object>> rows = new ArrayList<>();
                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    for (Cell cell : headerRow) {
                        headers.add(cell.toString());
                    }
                }
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        rowMap.put(headers.get(j), cell != null ? cell.toString() : "");
                    }
                    rows.add(rowMap);
                }
                Map<String, Object> sheetMap = new LinkedHashMap<>();
                sheetMap.put("sheetName", sheet.getSheetName());
                sheetMap.put("data", rows);
                sheetList.add(sheetMap);
            }
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, sheetList);
        }
    }

    @Override
    public void markdownToHtml(File mdFile, File outputHtmlFile) throws Exception {
        String charset = detectCharset(mdFile);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mdFile), charset));
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputHtmlFile), StandardCharsets.UTF_8)) {
            StringBuilder md = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                md.append(line).append("\n");
            }
            Parser parser = Parser.builder().build();
            Node document = parser.parse(md.toString());
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            writer.write(renderer.render(document));
        }
    }

    @Override
    public void htmlToPdf(File htmlFile, File outputPdfFile) throws Exception {
        String charset = detectCharset(htmlFile);
        StringBuilder html = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFile), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }
        }
        try (FileOutputStream fos = new FileOutputStream(outputPdfFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(fos);
            builder.run();
        }
    }

    @Override
    public void csvToJson(File csvFile, File outputJsonFile) throws Exception {
        String charset = detectCharset(csvFile);
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), charset));
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputJsonFile), StandardCharsets.UTF_8)) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return;
            String[] headers = allRows.get(0);
            List<Map<String, String>> data = new ArrayList<>();
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    map.put(headers[j], j < row.length ? row[j] : "");
                }
                data.add(map);
            }
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, data);
        }
    }

    @Override
    public void jsonToCsv(File jsonFile, File outputCsvFile) throws Exception {
        String charset = detectCharset(jsonFile);
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data;
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), charset);
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputCsvFile), StandardCharsets.UTF_8))) {
            data = mapper.readValue(reader, List.class);
            if (data.isEmpty()) return;
            Set<String> headers = data.get(0).keySet();
            writer.writeNext(headers.toArray(new String[0]));
            for (Map<String, Object> row : data) {
                String[] line = headers.stream().map(h -> row.getOrDefault(h, "").toString()).toArray(String[]::new);
                writer.writeNext(line);
            }
        }
    }

    @Override
    public void yamlToJson(File yamlFile, File outputJsonFile) throws Exception {
        String charset = detectCharset(yamlFile);
        Yaml yaml = new Yaml();
        Object data;
        try (Reader reader = new InputStreamReader(new FileInputStream(yamlFile), charset);
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputJsonFile), StandardCharsets.UTF_8)) {
            data = yaml.load(reader);
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, data);
        }
    }

    @Override
    public void jsonToYaml(File jsonFile, File outputYamlFile) throws Exception {
        String charset = detectCharset(jsonFile);
        ObjectMapper mapper = new ObjectMapper();
        Yaml yaml = new Yaml();
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), charset);
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputYamlFile), StandardCharsets.UTF_8)) {
            Object data = mapper.readValue(reader, Object.class);
            yaml.dump(data, writer);
        }
    }

    /**
     * 自动检测文件编码
     */
    private String detectCharset(File file) throws IOException {
        byte[] buf = new byte[4096];
        try (FileInputStream fis = new FileInputStream(file)) {
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            detector.reset();
            return encoding != null ? encoding : "UTF-8";
        }
    }
} 