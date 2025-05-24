package org.xue.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档解析服务
 * 统一管理各种文档解析器
 */
@Slf4j
@Service
public class DocumentParserService {

    private final List<DocumentParser> documentParsers;

    @Autowired
    public DocumentParserService(List<DocumentParser> documentParsers) {
        this.documentParsers = documentParsers;
        log.info("注册的文档解析器: {}", 
                documentParsers.stream()
                        .map(DocumentParser::getParserType)
                        .toList());
    }

    /**
     * 解析文档
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param mimeType MIME类型
     * @return 解析结果，如果不支持则返回null
     */
    public String parseDocument(InputStream inputStream, String fileName, String mimeType) {
        log.info("尝试解析文档: {} (类型: {})", fileName, mimeType);
        
        for (DocumentParser parser : documentParsers) {
            if (parser.supports(fileName, mimeType)) {
                try {
                    log.info("使用 {} 解析器解析文档: {}", parser.getParserType(), fileName);
                    String result = parser.parseDocument(inputStream, fileName);
                    log.info("文档 {} 解析成功，内容长度: {}", fileName, result.length());
                    return result;
                } catch (IOException e) {
                    log.error("使用 {} 解析器解析文档 {} 失败: {}", 
                            parser.getParserType(), fileName, e.getMessage());
                    // 继续尝试其他解析器
                }
            }
        }
        
        log.warn("没有找到适合的解析器处理文档: {} (类型: {})", fileName, mimeType);
        return null;
    }

    /**
     * 检查是否支持该文件类型
     */
    public boolean isSupported(String fileName, String mimeType) {
        return documentParsers.stream()
                .anyMatch(parser -> parser.supports(fileName, mimeType));
    }

    /**
     * 获取支持的解析器类型
     */
    public List<String> getSupportedTypes() {
        return documentParsers.stream()
                .map(DocumentParser::getParserType)
                .toList();
    }
} 