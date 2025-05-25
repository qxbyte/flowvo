package org.xue.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.xue.app.service.DocumentParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * PDF文档解析器
 */
@Slf4j
@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileName, String mimeType) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".pdf") || 
               "application/pdf".equals(mimeType);
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析PDF文档: {}", fileName);
        
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            // 设置页面范围（如果文档太大，只解析前50页）
            int totalPages = document.getNumberOfPages();
            if (totalPages > 50) {
                stripper.setEndPage(50);
                log.warn("PDF文档 {} 页数过多 ({}页)，仅解析前50页", fileName, totalPages);
            }
            
            String text = stripper.getText(document);
            
            // 限制文本长度，避免内容过长
            if (text.length() > 50000) {
                text = text.substring(0, 50000) + "\n...[内容过长，已截断]";
                log.warn("PDF文档 {} 内容过长，已截断到50000字符", fileName);
            }
            
            log.info("PDF文档 {} 解析完成，提取文本长度: {}", fileName, text.length());
            return text;
            
        } catch (Exception e) {
            log.error("解析PDF文档 {} 失败: {}", fileName, e.getMessage(), e);
            throw new IOException("PDF文档解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getParserType() {
        return "PDF";
    }
} 