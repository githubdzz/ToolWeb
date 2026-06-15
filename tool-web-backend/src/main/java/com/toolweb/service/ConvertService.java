package com.toolweb.service;

import java.io.File;

public interface ConvertService {
    /**
     * PDF转Word
     * @param pdfFile PDF文件
     * @param outputWordFile 输出Word文件
     * @throws Exception
     */
    void pdfToWord(File pdfFile, File outputWordFile) throws Exception;

    /**
     * Word转PDF
     * @param wordFile Word文件
     * @param outputPdfFile 输出PDF文件
     * @throws Exception
     */
    void wordToPdf(File wordFile, File outputPdfFile) throws Exception;

    /**
     * TXT转Word
     * @param txtFile TXT文件
     * @param outputWordFile 输出Word文件
     * @throws Exception
     */
    void txtToWord(File txtFile, File outputWordFile) throws Exception;

    /**
     * TXT转PDF
     * @param txtFile TXT文件
     * @param outputPdfFile 输出PDF文件
     * @throws Exception
     */
    void txtToPdf(File txtFile, File outputPdfFile) throws Exception;

    /**
     * Excel转JSON
     * @param excelFile Excel文件
     * @param outputJsonFile 输出JSON文件
     * @throws Exception
     */
    void excelToJson(File excelFile, File outputJsonFile) throws Exception;

    /**
     * Markdown转HTML
     * @param mdFile Markdown文件
     * @param outputHtmlFile 输出HTML文件
     * @throws Exception
     */
    void markdownToHtml(File mdFile, File outputHtmlFile) throws Exception;

    /**
     * HTML转PDF
     * @param htmlFile HTML文件
     * @param outputPdfFile 输出PDF文件
     * @throws Exception
     */
    void htmlToPdf(File htmlFile, File outputPdfFile) throws Exception;

    /**
     * CSV转JSON
     * @param csvFile CSV文件
     * @param outputJsonFile 输出JSON文件
     * @throws Exception
     */
    void csvToJson(File csvFile, File outputJsonFile) throws Exception;

    /**
     * JSON转CSV
     * @param jsonFile JSON文件
     * @param outputCsvFile 输出CSV文件
     * @throws Exception
     */
    void jsonToCsv(File jsonFile, File outputCsvFile) throws Exception;

    /**
     * YAML转JSON
     * @param yamlFile YAML文件
     * @param outputJsonFile 输出JSON文件
     * @throws Exception
     */
    void yamlToJson(File yamlFile, File outputJsonFile) throws Exception;

    /**
     * JSON转YAML
     * @param jsonFile JSON文件
     * @param outputYamlFile 输出YAML文件
     * @throws Exception
     */
    void jsonToYaml(File jsonFile, File outputYamlFile) throws Exception;
} 