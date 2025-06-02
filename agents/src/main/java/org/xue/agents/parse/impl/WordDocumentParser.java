package org.xue.agents.parse.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.xue.agents.parse.DocumentParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Word文档解析器 (支持.doc和.docx)
 */
@Slf4j
@Component
public class WordDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileName, String mimeType) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".doc") || 
               lowerFileName.endsWith(".docx") ||
               "application/msword".equals(mimeType) ||
               "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType);
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析Word文档: {}", fileName);
        
        try {
            String text;
            if (fileName.toLowerCase().endsWith(".docx")) {
                text = parseDocx(inputStream, fileName);
            } else {
                text = parseDoc(inputStream, fileName);
            }
            
            // 限制文本长度
            if (text.length() > 50000) {
                text = text.substring(0, 50000) + "\n...[内容过长，已截断]";
                log.warn("Word文档 {} 内容过长，已截断到50000字符", fileName);
            }
            
            log.info("Word文档 {} 解析完成，提取文本长度: {}", fileName, text.length());
            return text;
            
        } catch (Exception e) {
            log.error("解析Word文档 {} 失败: {}", fileName, e.getMessage(), e);
            throw new IOException("Word文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析.docx文件
     */
    private String parseDocx(InputStream inputStream, String fileName) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                String paragraphText = paragraph.getText();
                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    text.append(paragraphText).append("\n");
                }
            }
            
            return text.toString();
        }
    }

    /**
     * 解析.doc文件
     */
    private String parseDoc(InputStream inputStream, String fileName) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            
            return extractor.getText();
        }
    }

    @Override
    public String getParserType() {
        return "Word";
    }
} 