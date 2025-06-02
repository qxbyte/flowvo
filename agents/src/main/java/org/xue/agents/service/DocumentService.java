package org.xue.agents.service;

import org.xue.agents.dto.DocumentSearchRequest;
import org.xue.agents.dto.DocumentUploadRequest;
import org.xue.agents.dto.SearchResult;
import org.xue.agents.entity.Document;

import java.util.List;

/**
 * 文档服务接口
 */
public interface DocumentService {
    
    /**
     * 上传文档
     * @param request 文档上传请求
     * @return 文档实体
     */
    Document uploadDocument(DocumentUploadRequest request);
    
    /**
     * 删除文档
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 是否成功删除
     */
    boolean deleteDocument(String documentId, String userId);
    
    /**
     * 更新文档信息
     * @param documentId 文档ID
     * @param document 更新的文档信息
     * @param userId 用户ID
     * @return 更新后的文档实体
     */
    Document updateDocument(String documentId, Document document, String userId);
    
    /**
     * 根据ID获取文档
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 文档实体
     */
    Document getDocument(String documentId, String userId);
    
    /**
     * 获取用户的所有文档
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Document> getUserDocuments(String userId);
    
    /**
     * 向量搜索文档
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    List<SearchResult> searchDocuments(DocumentSearchRequest request);
    
    /**
     * 重新处理文档（重新切分和向量化）
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 处理后的文档实体
     */
    Document reprocessDocument(String documentId, String userId);
} 