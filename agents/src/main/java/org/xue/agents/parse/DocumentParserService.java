package org.xue.agents.parse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档解析服务
 * 管理所有解析器并提供统一的解析接口
 */
@Slf4j
@Service
public class DocumentParserService {

    @Autowired
    private List<DocumentParser> parsers;

    /**
     * 解析文档
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param mimeType MIME类型
     * @return 解析后的文本内容
     * @throws IOException 解析异常
     */
    public String parseDocument(InputStream inputStream, String fileName, String mimeType) throws IOException {
        log.info("开始解析文档: {}, MIME类型: {}", fileName, mimeType);
        
        // 查找支持的解析器
        DocumentParser parser = findSuitableParser(fileName, mimeType);
        if (parser == null) {
            throw new IOException("不支持的文件类型: " + fileName + " (MIME类型: " + mimeType + ")");
        }
        
        log.info("使用解析器: {} 解析文档: {}", parser.getParserType(), fileName);
        return parser.parseDocument(inputStream, fileName);
    }

    /**
     * 检查文件是否支持解析
     * @param fileName 文件名
     * @param mimeType MIME类型
     * @return 是否支持
     */
    public boolean isSupported(String fileName, String mimeType) {
        return findSuitableParser(fileName, mimeType) != null;
    }

    /**
     * 获取支持的文件类型列表
     * @return 支持的文件扩展名列表
     */
    public List<String> getSupportedTypes() {
        return List.of(
            // Office文档
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            // 文本文件
            "txt", "md", "markdown", "csv", "log", 
            // 配置文件
            "json", "xml", "yaml", "yml", "ini", "conf", "config", "properties",
            // 代码文件
            "java", "js", "ts", "py", "php", "go", "rs", "cpp", "c", "h",
            // 样式文件
            "css", "scss", "less", "html", "htm", "vue", "jsx", "tsx",
            // 脚本文件
            "sh", "bat", "ps1", "sql"
        );
    }

    /**
     * 查找合适的解析器
     */
    private DocumentParser findSuitableParser(String fileName, String mimeType) {
        for (DocumentParser parser : parsers) {
            if (parser.supports(fileName, mimeType)) {
                return parser;
            }
        }
        return null;
    }
} 