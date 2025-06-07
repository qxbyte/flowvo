package org.xue.agents.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.agents.config.EmbeddingConfig;
import org.xue.agents.dto.DocumentSearchRequest;
import org.xue.agents.dto.DocumentUploadRequest;
import org.xue.agents.dto.DocumentWithCategoryDTO;
import org.xue.agents.dto.SearchResult;
import org.xue.agents.entity.Document;
import org.xue.agents.entity.DocumentCategory;
import org.xue.agents.repository.DocumentCategoryRepository;
import org.xue.agents.repository.DocumentRepository;
import org.xue.agents.parse.DocumentParserService;
import org.xue.agents.service.DocumentService;
import org.xue.agents.embed.EmbeddingClient;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档服务实现类
 * 支持外部向量化服务和Spring AI内置服务的灵活切换
 */
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

//    private final VectorStore vectorStore;
    private final MilvusVectorStore milvusVectorStore;
    private final EmbeddingConfig embeddingConfig;
    private final TextSplitter textSplitter;
    private final EmbeddingClient embeddingClient; // 可选依赖
    private final DocumentRepository documentRepository;
    private final DocumentParserService documentParserService;
    private final DocumentCategoryRepository categoryRepository;

    @Autowired
    public DocumentServiceImpl(MilvusVectorStore milvusVectorStore,
                               EmbeddingConfig embeddingConfig,
                               DocumentRepository documentRepository,
                               DocumentParserService documentParserService,
                               @Autowired(required = false) EmbeddingClient embeddingClient, DocumentCategoryRepository categoryRepository) {
        this.milvusVectorStore = milvusVectorStore;
        this.embeddingConfig = embeddingConfig;
        this.documentRepository = documentRepository;
        this.documentParserService = documentParserService;
        this.embeddingClient = embeddingClient;
        this.categoryRepository = categoryRepository;

        // 根据配置创建文本切分器
        EmbeddingConfig.ExternalService external = embeddingConfig.getExternal();
        this.textSplitter = new TokenTextSplitter(external.getChunkSize(), external.getChunkOverlap(), 0, 10000, false);
        
        log.info("文档服务已初始化，向量化模式: {}", embeddingConfig.getType());
    }

    /**
     * 服务启动后自动执行，尝试从Milvus恢复丢失的文档元数据
     */
    @PostConstruct
    public void initializeDocuments() {
        try {
            log.info("检查是否需要从Milvus恢复文档元数据...");
            
            // 检查数据库中的文档数量
            long dbDocumentCount = documentRepository.count();
            log.info("数据库中现有文档数量: {}", dbDocumentCount);
            
            if (dbDocumentCount == 0) {
                log.info("数据库中没有文档记录，尝试从Milvus恢复...");
                
                // 在EXTERNAL模式下，跳过Milvus恢复，因为需要OpenAI API
                if (embeddingConfig.getType() == EmbeddingConfig.ServiceType.EXTERNAL) {
                    log.warn("当前为EXTERNAL模式，跳过Milvus文档恢复。如需恢复，请切换到SPRING_AI模式并配置OpenAI API");
                } else {
                    recoverDocumentsFromMilvus();
                }
            } else {
                log.info("数据库中已有文档记录，跳过恢复操作");
            }
            
        } catch (Exception e) {
            log.warn("从Milvus恢复文档元数据时出现错误: {}", e.getMessage());
        }
    }

    /**
     * 从Milvus的metadata中恢复文档元数据到数据库
     */
    private void recoverDocumentsFromMilvus() {
        try {
            // 通过空查询获取所有文档块，用于恢复元数据
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("") // 空查询
                    .topK(1000) // 获取最多1000个块
                    .similarityThreshold(0.0) // 最低阈值
                    .build();

            List<org.springframework.ai.document.Document> allChunks = milvusVectorStore.similaritySearch(searchRequest);
            log.info("从Milvus获取到 {} 个文档块", allChunks.size());
            
            // 按document_id分组，恢复文档
            Map<String, List<org.springframework.ai.document.Document>> docGroups = new HashMap<>();
            for (org.springframework.ai.document.Document chunk : allChunks) {
                String documentId = (String) chunk.getMetadata().get("document_id");
                if (documentId != null) {
                    docGroups.computeIfAbsent(documentId, k -> new ArrayList<>()).add(chunk);
                }
            }
            
            int recoveredCount = 0;
            for (Map.Entry<String, List<org.springframework.ai.document.Document>> entry : docGroups.entrySet()) {
                String documentId = entry.getKey();
                List<org.springframework.ai.document.Document> chunks = entry.getValue();
                
                if (!chunks.isEmpty() && !documentRepository.existsById(documentId)) {
                    // 从第一个块的metadata恢复文档信息
                    Map<String, Object> metadata = chunks.get(0).getMetadata();
                    
                    Document document = Document.builder()
                            .id(documentId)
                            .name((String) metadata.get("document_name"))
                            .content(chunks.stream().map(org.springframework.ai.document.Document::getText)
                                    .reduce("", (a, b) -> a + "\n" + b)) // 简单合并内容
                            .type((String) metadata.get("document_type"))
                            .userId((String) metadata.get("user_id"))
                            .status(Document.Status.COMPLETED)
                            .chunkCount(chunks.size())
                            .createdAt(LocalDateTime.now()) // 使用当前时间
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    // 处理可选字段
                    String tags = (String) metadata.get("tags");
                    if (tags != null && !tags.trim().isEmpty()) {
                        document.setTags(Arrays.asList(tags.split(",")));
                    }
                    
                    String description = (String) metadata.get("description");
                    if (description != null) {
                        document.setDescription(description);
                    }
                    
                    documentRepository.save(document);
                    recoveredCount++;
                    log.info("恢复文档: {} (用户: {})", document.getName(), document.getUserId());
                }
            }
            
            log.info("成功从Milvus恢复 {} 个文档", recoveredCount);
            
        } catch (Exception e) {
            log.error("从Milvus恢复文档失败", e);
        }
    }

    @Override
    @Transactional
    public Document uploadDocument(DocumentUploadRequest request) {
        log.info("开始上传文档: {}，向量化模式: {}", request.getName(), embeddingConfig.getType());
        
        // 1. 生成文档ID
        String documentId = UUID.randomUUID().toString();
        
        try {
            // 2. 创建文档实体
            Document document = Document.builder()
                    .id(documentId)
                    .name(request.getName())
                    .content(request.getContent())
                    .size(request.getSize())
                    .type(request.getType())
                    .tags(request.getTags())
                    .description(request.getDescription())
                    .filePath(request.getFilePath())
                    .userId(request.getUserId())
                    .category(request.getCategory())
                    .status(Document.Status.PROCESSING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // 3. 先保存到数据库
            document = documentRepository.save(document);

            List<org.springframework.ai.document.Document> documentsToStore;
            
            // 4. 根据配置选择处理方式
            if (embeddingConfig.getType() == EmbeddingConfig.ServiceType.EXTERNAL && embeddingClient != null) {
                // 使用外部向量化服务
                documentsToStore = processWithExternalService(documentId, request);
            } else {
                // 使用Spring AI内置服务
                documentsToStore = processWithSpringAI(documentId, request);
            }
            
            // 5. 批量添加到向量数据库
            milvusVectorStore.add(documentsToStore);
            
            // 6. 更新文档状态
            document.setStatus(Document.Status.COMPLETED);
            document.setChunkCount(documentsToStore.size());
            document.setUpdatedAt(LocalDateTime.now());
            document = documentRepository.save(document);
            
            log.info("文档上传成功: {} (ID: {}, 块数: {})", request.getName(), documentId, documentsToStore.size());
            return document;
            
        } catch (Exception e) {
            log.error("文档上传失败: {}", request.getName(), e);
            // 如果处理失败，更新状态
            try {
                Document failedDoc = documentRepository.findById(documentId).orElse(null);
                if (failedDoc != null) {
                    failedDoc.setStatus(Document.Status.FAILED);
                    failedDoc.setUpdatedAt(LocalDateTime.now());
                    documentRepository.save(failedDoc);
                }
            } catch (Exception dbError) {
                log.error("更新失败状态时出错", dbError);
            }
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用外部向量化服务处理文档
     */
    private List<org.springframework.ai.document.Document> processWithExternalService(String documentId, DocumentUploadRequest request) {
        log.info("使用外部向量化服务处理文档: {}", request.getName());
        
        try {
            // 使用外部服务进行切分和向量化
            List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(request.getContent());
            
            List<org.springframework.ai.document.Document> documents = new ArrayList<>();
            for (int i = 0; i < chunkEmbeddings.size(); i++) {
                EmbeddingClient.ChunkEmbedding chunkEmbedding = chunkEmbeddings.get(i);
                
                // 创建包含所有字段的完整元数据
                Map<String, Object> metadata = createCompleteMetadata(documentId, request, i, chunkEmbeddings.size());
                
                // 创建Spring AI Document
                org.springframework.ai.document.Document doc = org.springframework.ai.document.Document.builder()
                    .id(documentId + "_chunk_" + i)
                    .text(chunkEmbedding.getChunk())
                    .metadata(metadata)
                    .build();
                
                documents.add(doc);
            }
            
            return documents;
        } catch (Exception e) {
            log.error("外部向量化服务处理失败: {}", e.getMessage());
            throw new RuntimeException("外部向量化服务处理失败", e);
        }
    }
    
    /**
     * 使用Spring AI内置服务处理文档
     */
    private List<org.springframework.ai.document.Document> processWithSpringAI(String documentId, DocumentUploadRequest request) {
        log.info("使用Spring AI内置服务处理文档: {}", request.getName());
        
        // 创建原始文档
        org.springframework.ai.document.Document originalDoc = org.springframework.ai.document.Document.builder()
            .id(documentId)
            .text(request.getContent())
            .metadata(createCompleteMetadata(documentId, request, 0, 1))
            .build();
        
        // 使用Spring AI进行文本切分
        List<org.springframework.ai.document.Document> chunks = textSplitter.split(originalDoc);
        
        // 为每个块添加特定的元数据
        List<org.springframework.ai.document.Document> documentsToStore = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            org.springframework.ai.document.Document chunk = chunks.get(i);
            
            // 创建完整的元数据，包含所有独立字段的值
            Map<String, Object> metadata = createCompleteMetadata(documentId, request, i, chunks.size());
            
            org.springframework.ai.document.Document enhancedDoc = org.springframework.ai.document.Document.builder()
                .id(documentId + "_chunk_" + i)
                .text(chunk.getText())
                .metadata(metadata)
                .build();
            
            documentsToStore.add(enhancedDoc);
        }
        
        return documentsToStore;
    }
    
    /**
     * 创建完整的元数据，包含所有独立字段和metadata JSON
     */
    private Map<String, Object> createCompleteMetadata(String documentId, DocumentUploadRequest request, int chunkIndex, int totalChunks) {
        Map<String, Object> metadata = new HashMap<>();
        
        // 简化版本：只使用Spring AI完全支持的基础字段
        // 所有业务信息都作为简单的metadata字段
        metadata.put("user_id", request.getUserId());
        metadata.put("document_id", documentId);
        metadata.put("document_name", request.getName());
        metadata.put("chunk_index", chunkIndex);
        metadata.put("total_chunks", totalChunks);
        metadata.put("document_type", request.getType());
        metadata.put("embedding_type", embeddingConfig.getType().toString());
        
        String currentTime = LocalDateTime.now().toString();
        metadata.put("created_at", currentTime);
        metadata.put("updated_at", currentTime);
        
        // 可选字段
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            metadata.put("tags", String.join(",", request.getTags()));
        }
        if (request.getDescription() != null) {
            metadata.put("description", request.getDescription());
        }
        if (request.getCategory() != null) {
            metadata.put("category", request.getCategory());
        }
        
        return metadata;
    }

    /**
     * 创建基础元数据（保留用于兼容性）
     * @deprecated 使用 createCompleteMetadata 替代
     */
    @Deprecated
    private Map<String, Object> createBaseMetadata(String documentId, DocumentUploadRequest request) {
        return createCompleteMetadata(documentId, request, 0, 1);
    }

    @Override
    public boolean deleteDocument(String documentId, String userId) {
        log.info("删除文档: {} (用户: {})", documentId, userId);
        
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null || !document.getUserId().equals(userId)) {
                log.warn("文档不存在或无权限删除: {}", documentId);
                return false;
            }
            
            // 1. 从向量数据库删除相关的文档块，使用过滤表达式
            try {
                String filterExpression = "document_id == '" + documentId + "'";
                milvusVectorStore.delete(filterExpression);
            } catch (Exception e) {
                log.warn("从向量数据库删除文档块时出现警告: {}", e.getMessage());
            }
            
            // 2. 删除文档元数据
            documentRepository.delete(document);
            
            log.info("文档删除成功: {}", documentId);
            return true;
            
        } catch (Exception e) {
            log.error("删除文档失败: {}", documentId, e);
            return false;
        }
    }

    @Override
    public Document updateDocument(String documentId, Document updatedDocument, String userId) {
        log.info("更新文档: {} (用户: {})", documentId, userId);
        
        Document existingDocument = documentRepository.findById(documentId).orElse(null);
        if (existingDocument == null || !existingDocument.getUserId().equals(userId)) {
            log.warn("文档不存在或无权限更新: {}", documentId);
            return null;
        }
        
        // 更新允许修改的字段
        if (updatedDocument.getName() != null) {
            existingDocument.setName(updatedDocument.getName());
        }
        if (updatedDocument.getTags() != null) {
            existingDocument.setTags(updatedDocument.getTags());
        }
        if (updatedDocument.getDescription() != null) {
            existingDocument.setDescription(updatedDocument.getDescription());
        }
        if (updatedDocument.getCategory() != null) {
            existingDocument.setCategory(updatedDocument.getCategory());
        }
        existingDocument.setUpdatedAt(LocalDateTime.now());
        
        // 如果内容发生变化，需要重新向量化
        if (updatedDocument.getContent() != null && !updatedDocument.getContent().equals(existingDocument.getContent())) {
            log.info("文档内容已更改，重新向量化: {}", documentId);
            existingDocument.setContent(updatedDocument.getContent());
            return reprocessDocument(documentId, userId);
        }
        
        documentRepository.save(existingDocument);
        log.info("文档更新成功: {}", documentId);
        return existingDocument;
    }

    @Override
    public Document getDocument(String documentId, String userId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null || !document.getUserId().equals(userId)) {
            log.warn("文档不存在或无权限访问: {}", documentId);
            return null;
        }
        return document;
    }

    @Override
    public List<Document> getUserDocuments(String userId) {
        return documentRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public List<DocumentWithCategoryDTO> getUserDocumentsWithCategory(String userId) {
        // 获取用户的所有文档
        List<Document> documents = documentRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        
        // 转换为DocumentWithCategoryDTO
        return documents.stream().map(document -> {
            DocumentWithCategoryDTO dto = DocumentWithCategoryDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .content(document.getContent())
                .size(document.getSize())
                .type(document.getType())
                .tags(document.getTags())
                .description(document.getDescription())
                .filePath(document.getFilePath())
                .userId(document.getUserId())
                .categoryId(document.getCategory())
                .categoryName(getCategoryNameById(document.getCategory())) // 通过分类ID获取分类名称
                .categoryIcon(getCategoryIconById(document.getCategory())) // 通过分类ID获取分类图标
                .status(document.getStatus() != null ? document.getStatus().toString() : "UNKNOWN")
                .chunkCount(document.getChunkCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 根据分类ID获取分类名称
     */
    private String getCategoryNameById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return "未分类";
        }

        Optional<DocumentCategory> category = categoryRepository.findById(categoryId);

        if (category.isPresent()) {
            DocumentCategory docCategory = category.get();
            // 使用 docCategory 做后续逻辑
            return docCategory.getName();
        } else {
            // 没有找到对应 ID 的记录
            return "未分类";
        }
    }
    
    /**
     * 根据分类ID获取分类图标
     */
    private String getCategoryIconById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return "folder";
        }
        
        // 先使用固定的映射
        switch (categoryId) {
            case "cat_user_manual": return "book";
            case "cat_technical_doc": return "code";
            case "cat_training_material": return "graduation-cap";
            case "cat_faq": return "help-circle";
            case "cat_policy": return "shield";
            case "cat_other": return "file";
            default: return "folder";
        }
    }

    @Override
    public List<SearchResult> searchDocuments(DocumentSearchRequest request) {
        log.info("执行向量搜索: {}", request.getQuery());
        
        try {
            // 构建搜索请求
            SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                    .query(request.getQuery())
                    .topK(request.getLimit() != null ? request.getLimit() : 10)
                    .similarityThreshold(request.getThreshold() != null ? request.getThreshold() : 0.5);
            
            // 如果指定用户ID，添加过滤条件
            if (request.getUserId() != null) {
                searchRequestBuilder.filterExpression("user_id == '" + request.getUserId() + "'");
            }
            
            SearchRequest searchRequest = searchRequestBuilder.build();
            
            // 执行搜索
            List<org.springframework.ai.document.Document> searchResults = milvusVectorStore.similaritySearch(searchRequest);
            
            // 转换为业务对象
            List<SearchResult> results = new ArrayList<>();
            for (org.springframework.ai.document.Document doc : searchResults) {
                Map<String, Object> metadata = doc.getMetadata();
                
                SearchResult result = SearchResult.builder()
                        .documentId((String) metadata.get("document_id"))
                        .documentName((String) metadata.get("document_name"))
                        .content(doc.getText())
                        .score(1.0) // Spring AI暂不提供具体分数，使用默认值
                        .chunkIndex((Integer) metadata.get("chunk_index"))
                        .build();
                
                results.add(result);
            }
            
            log.info("搜索完成，返回 {} 个结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("向量搜索失败: {}", request.getQuery(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Document reprocessDocument(String documentId, String userId) {
        log.info("重新处理文档: {} (用户: {})", documentId, userId);
        
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null || !document.getUserId().equals(userId)) {
            log.warn("文档不存在或无权限处理: {}", documentId);
            return null;
        }
        
        try {
            // 1. 删除旧的向量数据
            deleteDocument(documentId, userId);
            
            // 2. 重新上传处理
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setName(document.getName());
            request.setContent(document.getContent());
            request.setSize(document.getSize());
            request.setType(document.getType());
            request.setTags(document.getTags());
            request.setDescription(document.getDescription());
            request.setFilePath(document.getFilePath());
            request.setUserId(document.getUserId());
            request.setCategory(document.getCategory());
            
            return uploadDocument(request);
            
        } catch (Exception e) {
            log.error("重新处理文档失败: {}", documentId, e);
            document.setStatus(Document.Status.FAILED);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);
            return document;
        }
    }

    @Override
    public Document reprocessDocumentWithFile(String documentId, String userId, MultipartFile file) {
        log.info("使用新文件重新处理文档: {} (用户: {}, 新文件: {})", documentId, userId, file.getOriginalFilename());
        
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null || !document.getUserId().equals(userId)) {
            log.warn("文档不存在或无权限处理: {}", documentId);
            return null;
        }
        
        try {
            // 1. 删除旧的向量数据（保留文档记录但删除向量存储）
            log.info("删除文档 {} 的旧向量数据", documentId);
            List<String> vectorIds = new ArrayList<>();
            for (int i = 0; i < document.getChunkCount(); i++) {
                vectorIds.add(documentId + "_chunk_" + i);
            }
            
            if (!vectorIds.isEmpty()) {
                milvusVectorStore.delete(vectorIds);
                log.info("已删除 {} 个向量块", vectorIds.size());
            }
            
            // 2. 解析新文件内容
            String content;
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            try (InputStream inputStream = file.getInputStream()) {
                content = documentParserService.parseDocument(inputStream, originalFilename, contentType);
                if (content == null || content.trim().isEmpty()) {
                    throw new RuntimeException("文件解析失败或内容为空");
                }
            }
            
            // 3. 更新文档信息
            document.setName(originalFilename);
            document.setContent(content);
            document.setSize(file.getSize());
            document.setType(getFileExtension(originalFilename));
            document.setStatus(Document.Status.PROCESSING);
            document.setUpdatedAt(LocalDateTime.now());
            
            // 4. 重新向量化处理
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setName(originalFilename);
            request.setContent(content);
            request.setSize(file.getSize());
            request.setType(getFileExtension(originalFilename));
            request.setTags(document.getTags());
            request.setDescription(document.getDescription());
            request.setFilePath(document.getFilePath());
            request.setUserId(document.getUserId());
            request.setCategory(document.getCategory());
            
            // 5. 进行向量化处理（使用原文档ID）
            List<org.springframework.ai.document.Document> vectorDocuments;
            
            // 根据配置选择处理方式
            if (embeddingConfig.getType() == EmbeddingConfig.ServiceType.EXTERNAL && embeddingClient != null) {
                // 使用外部向量化服务
                vectorDocuments = processWithExternalService(documentId, request);
            } else {
                // 使用Spring AI内置服务
                vectorDocuments = processWithSpringAI(documentId, request);
            }
            
            // 6. 存储到向量数据库
            milvusVectorStore.add(vectorDocuments);
            
            // 7. 更新文档状态和块数量
            document.setStatus(Document.Status.COMPLETED);
            document.setChunkCount(vectorDocuments.size());
            document.setUpdatedAt(LocalDateTime.now());
            
            // 8. 保存更新的文档信息
            document = documentRepository.save(document);
            
            log.info("文档 {} 重新处理完成，新文件: {}, 向量块数: {}", 
                    documentId, originalFilename, vectorDocuments.size());
            
            return document;
            
        } catch (Exception e) {
            log.error("重新处理文档失败: {}", documentId, e);
            document.setStatus(Document.Status.FAILED);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);
            throw new RuntimeException("重新处理文档失败: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toUpperCase();
        }
        return "unknown";
    }
} 