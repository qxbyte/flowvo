package org.xue.agents.parse;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档解析器接口
 * 用于解析各种格式的文档文件
 */
public interface DocumentParser {
    
    /**
     * 检查是否支持该文件类型
     * @param fileName 文件名
     * @param mimeType MIME类型
     * @return 是否支持
     */
    boolean supports(String fileName, String mimeType);
    
    /**
     * 解析文档内容
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @return 解析后的文本内容
     * @throws IOException 解析异常
     */
    String parseDocument(InputStream inputStream, String fileName) throws IOException;
    
    /**
     * 获取解析器类型
     * @return 解析器类型名称
     */
    String getParserType();
} 