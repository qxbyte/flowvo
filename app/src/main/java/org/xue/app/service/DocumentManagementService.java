package org.xue.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.DocumentSearchRequest;
import org.xue.app.dto.DocumentUploadRequest;
import org.xue.app.dto.SearchResult;
import org.xue.app.entity.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * 文档管理服务
 * 注意：文档接口已移至agents模块，此服务类暂时保留用于兼容性
 * 实际的文档操作应该直接调用agents服务的接口
 */
@Slf4j
@Service
public class DocumentManagementService {

    private final DocumentParserService documentParserService;

    @Autowired
    public DocumentManagementService(
            DocumentParserService documentParserService) {
        this.documentParserService = documentParserService;
        log.info("DocumentManagementService initialized (without feign client)");
    }

    /**
     * 上传文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public Document uploadDocument(MultipartFile file, String userId, String description, List<String> tags) {
        log.warn("uploadDocument method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档上传功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 获取支持的文件类型
     * 使用本地的DocumentParserService
     */
    public List<String> getSupportedFileTypes() {
        try {
            // 使用本地的解析器服务
            return documentParserService.getSupportedTypes();
        } catch (Exception e) {
            log.error("获取支持的文件类型失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public boolean deleteDocument(String documentId, String userId) {
        log.warn("deleteDocument method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档删除功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 更新文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public Document updateDocument(String documentId, String userId, Document updatedDocument) {
        log.warn("updateDocument method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档更新功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 获取文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public Document getDocument(String documentId, String userId) {
        log.warn("getDocument method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档获取功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 获取用户文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public List<Document> getUserDocuments(String userId) {
        log.warn("getUserDocuments method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档查询功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 搜索文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public List<SearchResult> searchDocuments(DocumentSearchRequest request) {
        log.warn("searchDocuments method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档搜索功能已移至agents服务，请直接调用agents服务的接口");
    }

    /**
     * 重新处理文档
     * 注意：此方法已废弃，请直接调用agents服务
     */
    @Deprecated
    public Document reprocessDocument(String documentId, String userId) {
        log.warn("reprocessDocument method is deprecated, please use agents service directly");
        throw new UnsupportedOperationException("文档重新处理功能已移至agents服务，请直接调用agents服务的接口");
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
     * 检查文件类型是否支持
     */
    public boolean isFileSupported(String fileName, String contentType) {
        return documentParserService.isSupported(fileName, contentType);
    }
} 