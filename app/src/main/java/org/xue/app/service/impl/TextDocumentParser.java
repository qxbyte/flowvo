package org.xue.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xue.app.service.DocumentParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 通用文本文档解析器
 * 处理TXT、CSV、Markdown等纯文本格式
 */
@Slf4j
@Component
public class TextDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileName, String mimeType) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        
        // 支持的文本文件扩展名
        return lowerFileName.endsWith(".txt") || 
               lowerFileName.endsWith(".csv") ||
               lowerFileName.endsWith(".md") ||
               lowerFileName.endsWith(".markdown") ||
               lowerFileName.endsWith(".log") ||
               lowerFileName.endsWith(".json") ||
               lowerFileName.endsWith(".xml") ||
               lowerFileName.endsWith(".yaml") ||
               lowerFileName.endsWith(".yml") ||
               lowerFileName.endsWith(".ini") ||
               lowerFileName.endsWith(".conf") ||
               lowerFileName.endsWith(".config") ||
               "text/plain".equals(mimeType) ||
               "text/csv".equals(mimeType) ||
               "text/markdown".equals(mimeType) ||
               "application/json".equals(mimeType) ||
               "text/xml".equals(mimeType) ||
               "application/xml".equals(mimeType);
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileName) throws IOException {
        log.info("开始解析文本文档: {}", fileName);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;
            int maxLines = 2000; // 限制行数避免内容过长
            
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }
            
            String result = content.toString();
            
            // 如果超过最大行数，添加截断提示
            if (lineCount >= maxLines) {
                result += "\n...[文档内容过长，已截断到" + maxLines + "行]";
                log.warn("文档 {} 内容过长，已截断到{}行", fileName, maxLines);
            }
            
            // 限制总字符数
            if (result.length() > 50000) {
                result = result.substring(0, 50000) + "\n...[内容过长，已截断]";
                log.warn("文档 {} 内容过长，已截断到50000字符", fileName);
            }
            
            log.info("文本文档 {} 解析完成，提取文本长度: {}", fileName, result.length());
            return result;
            
        } catch (Exception e) {
            log.error("解析文本文档 {} 失败: {}", fileName, e.getMessage(), e);
            throw new IOException("文本文档解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getParserType() {
        return "Text";
    }
} 