package org.xue.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.DocumentSearchRequest;
import org.xue.app.dto.DocumentUploadRequest;
import org.xue.app.dto.SearchResult;
import org.xue.app.entity.Document;
import org.xue.app.feign.DocumentClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文档管理服务
 * 集成文档解析和向量化功能
 */
@Slf4j
@Service
public class DocumentManagementService {

    private final DocumentParserService documentParserService;
    private final DocumentClient documentClient;

    @Autowired
    public DocumentManagementService(DocumentParserService documentParserService, 
                                   DocumentClient documentClient) {
        this.documentParserService = documentParserService;
        this.documentClient = documentClient;
    }

    /**
     * 上传并处理文档
     * @param file 上传的文件
     * @param userId 用户ID
     * @param tags 文档标签
     * @param description 文档描述
     * @return 处理后的文档信息
     */
    public Document uploadDocument(MultipartFile file, String userId, String[] tags, String description) {
        log.info("开始处理文档上传: {}", file.getOriginalFilename());
        
        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            
            // 2. 检查文件类型是否支持（从agents获取支持的类型）
            if (!isFileSupportedByAgents(file.getOriginalFilename(), file.getContentType())) {
                throw new RuntimeException("不支持的文件类型: " + file.getOriginalFilename());
            }
            
            // 3. 直接将文件传递给agents进行解析和向量化处理
            Document result = documentClient.uploadFile(file, userId, description, 
                    tags != null ? Arrays.asList(tags) : null).getBody();
            
            log.info("文档处理成功: {} (ID: {})", file.getOriginalFilename(), 
                    result != null ? result.getId() : "未知");
            
            return result;
            
        } catch (Exception e) {
            log.error("文档上传处理失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("文档上传处理失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查agents是否支持该文件类型
     */
    private boolean isFileSupportedByAgents(String fileName, String contentType) {
        try {
            List<String> supportedTypes = documentClient.getSupportedTypes().getBody();
            if (supportedTypes == null || supportedTypes.isEmpty()) {
                // 如果无法获取支持类型，使用本地检查作为备选
                return documentParserService.isSupported(fileName, contentType);
            }
            
            // 检查文件扩展名是否在支持列表中
            if (fileName != null && fileName.contains(".")) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                return supportedTypes.contains(extension);
            }
            
            return false;
        } catch (Exception e) {
            log.warn("无法从agents获取支持的文件类型，使用本地检查: {}", e.getMessage());
            // 如果调用agents失败，使用本地检查作为备选
            return documentParserService.isSupported(fileName, contentType);
        }
    }
    
    /**
     * 解析文档内容
     */
    private String parseDocumentContent(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        log.info("解析文档: {} (类型: {})", fileName, contentType);
        
        try (InputStream inputStream = file.getInputStream()) {
            String content = documentParserService.parseDocument(inputStream, fileName, contentType);
            
            if (content != null && !content.trim().isEmpty()) {
                log.info("文档解析成功，内容长度: {}", content.length());
                return content;
            } else {
                log.warn("文档解析结果为空: {}", fileName);
                return null;
            }
        }
    }
    
    /**
     * 删除文档
     */
    public boolean deleteDocument(String documentId, String userId) {
        try {
            return documentClient.deleteDocument(documentId, userId).getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("删除文档失败: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * 更新文档
     */
    public Document updateDocument(String documentId, String userId, Document updatedDocument) {
        try {
            return documentClient.updateDocument(documentId, userId, updatedDocument).getBody();
        } catch (Exception e) {
            log.error("更新文档失败: {}", documentId, e);
            throw new RuntimeException("更新文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文档
     */
    public Document getDocument(String documentId, String userId) {
        try {
            return documentClient.getDocument(documentId, userId).getBody();
        } catch (Exception e) {
            log.error("获取文档失败: {}", documentId, e);
            return null;
        }
    }
    
    /**
     * 获取用户所有文档
     */
    public List<Document> getUserDocuments(String userId) {
        try {
            return documentClient.getUserDocuments(userId).getBody();
        } catch (Exception e) {
            log.error("获取用户文档列表失败: {}", userId, e);
            throw new RuntimeException("获取用户文档列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 向量搜索文档
     */
    public List<SearchResult> searchDocuments(String query, String userId, Integer limit, Double threshold) {
        try {
            DocumentSearchRequest request = new DocumentSearchRequest();
            request.setQuery(query);
            request.setUserId(userId);
            request.setLimit(limit != null ? limit : 10);
            request.setThreshold(threshold != null ? threshold : 0.5);
            
            return documentClient.searchDocuments(request).getBody();
        } catch (Exception e) {
            log.error("文档搜索失败: {}", query, e);
            throw new RuntimeException("文档搜索失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重新处理文档
     */
    public Document reprocessDocument(String documentId, String userId) {
        try {
            return documentClient.reprocessDocument(documentId, userId).getBody();
        } catch (Exception e) {
            log.error("重新处理文档失败: {}", documentId, e);
            throw new RuntimeException("重新处理文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取支持的文件类型
     */
    public List<String> getSupportedFileTypes() {
        try {
            // 优先从agents获取支持的文件类型
            List<String> agentsSupportedTypes = documentClient.getSupportedTypes().getBody();
            if (agentsSupportedTypes != null && !agentsSupportedTypes.isEmpty()) {
                return agentsSupportedTypes;
            }
        } catch (Exception e) {
            log.warn("无法从agents获取支持的文件类型，使用本地列表: {}", e.getMessage());
        }
        
        // 如果agents不可用，使用本地的作为备选
        return documentParserService.getSupportedTypes();
    }
    
    /**
     * 检查文件类型是否支持
     */
    public boolean isFileSupported(String fileName, String contentType) {
        return isFileSupportedByAgents(fileName, contentType);
    }
} 