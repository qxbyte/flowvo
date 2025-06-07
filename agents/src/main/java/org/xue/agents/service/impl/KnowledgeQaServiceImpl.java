package org.xue.agents.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.milvus.MilvusSearchRequest;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.xue.agents.dto.CategoryStatistics;
import org.xue.agents.dto.KnowledgeQaRequest;
import org.xue.agents.dto.KnowledgeQaResponse;
import org.xue.agents.dto.CreateCategoryRequest;
import org.xue.agents.dto.UpdateCategoryRequest;
import org.xue.agents.entity.DocumentCategory;
import org.xue.agents.entity.KnowledgeQaRecord;
import org.xue.agents.entity.PopularQuestion;
import org.xue.agents.repository.DocumentCategoryRepository;
import org.xue.agents.repository.DocumentRepository;
import org.xue.agents.repository.KnowledgeQaRecordRepository;
import org.xue.agents.repository.PopularQuestionRepository;
import org.xue.agents.service.KnowledgeQaService;
import reactor.core.publisher.Flux;
import org.xue.agents.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

/**
 * 知识库问答服务实现类
 * 基于Spring AI RAG实现
 */
@Slf4j
@Service
public class KnowledgeQaServiceImpl implements KnowledgeQaService {
    
    private final DeepSeekChatModel chatModel;
    private final MilvusVectorStore milvusVectorStore;
    private final KnowledgeQaRecordRepository qaRecordRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final PopularQuestionRepository popularQuestionRepository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    
    // 异步执行器
    private final Executor asyncExecutor = Executors.newFixedThreadPool(5);
    
    // 知识库问答的提示词模板
    private static final String KNOWLEDGE_QA_PROMPT = """
            你是一个专业的知识库助手。请基于以下相关文档内容回答用户的问题。
            
            相关文档内容：
            {context}
            
            用户问题：{question}
            
            请注意：
            1. 基于提供的文档内容进行回答，确保准确性
            2. 如果文档中没有相关信息，请明确说明
            3. 如果检索文档中有类似的编号或者数字，则结合文档上下文就行总结概括
            4. 回答要简洁明了，重点突出
            5. 如果涉及多个文档，请综合所有相关信息
            
            请基于以上规则给出准确的回答：
            """;

    public KnowledgeQaServiceImpl(DeepSeekChatModel chatModel, @Qualifier("customVectorStore") MilvusVectorStore milvusVectorStore, KnowledgeQaRecordRepository qaRecordRepository, DocumentCategoryRepository categoryRepository, PopularQuestionRepository popularQuestionRepository, DocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.milvusVectorStore = milvusVectorStore;
        this.qaRecordRepository = qaRecordRepository;
        this.categoryRepository = categoryRepository;
        this.popularQuestionRepository = popularQuestionRepository;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public KnowledgeQaResponse askQuestion(KnowledgeQaRequest request, String userId) {
        long startTime = System.currentTimeMillis();
        String recordId = UUID.randomUUID().toString();
        
        try {
            // 1. 创建问答记录
            KnowledgeQaRecord record = createQaRecord(recordId, request, userId);
            qaRecordRepository.save(record);
            
            // 2. 向量检索相关文档
            List<Document> relevantDocs = searchRelevantDocuments(request, userId);
            
            // 3. 构建上下文
            String context = buildContext(relevantDocs);
            Double maxSimilarity = relevantDocs.isEmpty() ? 0.0 : 
                relevantDocs.stream()
                    .mapToDouble(doc -> (Double) doc.getMetadata().getOrDefault("distance", 0.0))
                    .max().orElse(0.0);
            
            // 4. 生成AI回答
            String answer = generateAnswer(context, request.getQuestion());
            
            // 5. 构建响应
            List<KnowledgeQaResponse.SourceDocument> sources = buildSourceDocuments(relevantDocs);
            
            // 6. 更新记录
            long responseTime = System.currentTimeMillis() - startTime;
            updateQaRecord(record, answer, sources, (int) responseTime, maxSimilarity);
            qaRecordRepository.save(record);
            
            // 7. 异步更新热门问题统计
            updatePopularQuestionAsync(request.getQuestion(), request.getCategory(), userId);
            
            return KnowledgeQaResponse.builder()
                    .id(recordId)
                    .question(request.getQuestion())
                    .answer(answer)
                    .sources(sources)
                    .questionCategory(request.getCategory())
                    .responseTimeMs((int) responseTime)
                    .similarityScore(maxSimilarity)
                    .createdAt(record.getCreatedAt())
                    .status("COMPLETED")
                    .build();
                    
        } catch (Exception e) {
            log.error("知识库问答处理失败: questionId={}, error={}", recordId, e.getMessage(), e);
            
            // 更新记录状态为失败
            qaRecordRepository.findById(recordId).ifPresent(record -> {
                record.setStatus(KnowledgeQaRecord.Status.FAILED);
                record.setAnswer("抱歉，处理您的问题时出现了错误，请稍后重试。");
                qaRecordRepository.save(record);
            });
            
            throw new RuntimeException("知识库问答处理失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Flux<String> askQuestionStream(KnowledgeQaRequest request, String userId) {
        String recordId = UUID.randomUUID().toString();
        
        return Flux.create(sink -> {
            try {
                long startTime = System.currentTimeMillis();
                
                log.info("开始流式问答: question={}, userId={}, topK={}, similarityThreshold={}, category={}", 
                    request.getQuestion(), userId, request.getTopK(), request.getSimilarityThreshold(), request.getCategory());
                
                // 1. 创建问答记录
                KnowledgeQaRecord record = createQaRecord(recordId, request, userId);
                qaRecordRepository.save(record);
                
                // 2. 向量检索相关文档
                List<Document> relevantDocs = searchRelevantDocuments(request, userId);
                log.info("向量检索完成: 找到{}个相关文档", relevantDocs.size());
                
                // 3. 构建上下文
                String context = buildContext(relevantDocs);
                log.info("构建上下文完成: 上下文长度={}", context.length());
                log.debug("上下文内容: {}", context.substring(0, Math.min(context.length(), 500)) + "...");
                
                String prompt = KNOWLEDGE_QA_PROMPT
                        .replace("{context}", context)
                        .replace("{question}", request.getQuestion());
                
                log.debug("完整提示词: {}", prompt.substring(0, Math.min(prompt.length(), 800)) + "...");
                
                // 4. 创建流式ChatClient
                StringBuilder fullAnswer = new StringBuilder();
                
                ChatClient.create(chatModel)
                        .prompt(prompt)
                        .stream()
                        .content()
                        .doOnNext(chunk -> {
                            log.debug("接收到流式内容块: {}", chunk);
                            fullAnswer.append(chunk);
                            sink.next(chunk);
                        })
                        .doOnComplete(() -> {
                            try {
                                log.info("流式回答完成: 总长度={}", fullAnswer.length());
                                log.debug("完整回答: {}", fullAnswer.toString());
                                
                                // 5. 更新记录
                                long responseTime = System.currentTimeMillis() - startTime;
                                Double maxSimilarity = relevantDocs.isEmpty() ? 0.0 : 
                                    relevantDocs.stream()
                                        .mapToDouble(doc -> (Double) doc.getMetadata().getOrDefault("distance", 0.0))
                                        .max().orElse(0.0);
                                
                                List<KnowledgeQaResponse.SourceDocument> sources = buildSourceDocuments(relevantDocs);
                                updateQaRecord(record, fullAnswer.toString(), sources, (int) responseTime, maxSimilarity);
                                qaRecordRepository.save(record);
                                
                                // 异步更新热门问题统计
                                updatePopularQuestionAsync(request.getQuestion(), request.getCategory(), userId);
                                
                                sink.complete();
                            } catch (Exception e) {
                                log.error("流式问答完成时处理失败: {}", e.getMessage(), e);
                                sink.error(e);
                            }
                        })
                        .doOnError(error -> {
                            log.error("流式问答失败: {}", error.getMessage(), error);
                            // 更新记录状态为失败
                            record.setStatus(KnowledgeQaRecord.Status.FAILED);
                            record.setAnswer("抱歉，处理您的问题时出现了错误。");
                            qaRecordRepository.save(record);
                            sink.error(error);
                        })
                        .subscribe();
                        
            } catch (Exception e) {
                log.error("流式问答初始化失败: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }
    
    @Override
    public List<KnowledgeQaRecord> getRecentQuestions(int limit, String category) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (StringUtils.hasText(category)) {
            return qaRecordRepository.findRecentQuestionsByCategory(category, pageable);
        } else {
            return qaRecordRepository.findRecentQuestions(pageable);
        }
    }
    
    @Override
    public List<PopularQuestion> getHotQuestions(int limit, String category) {
        Pageable pageable = PageRequest.of(0, limit);
        int minCount = 2; // 至少被问过2次才算热门
        
        if (StringUtils.hasText(category)) {
            return popularQuestionRepository.findHotQuestionsByCategory(category, minCount, pageable);
        } else {
            return popularQuestionRepository.findHotQuestions(minCount, pageable);
        }
    }
    
    @Override
    public List<CategoryStatistics> getKnowledgeBaseStatistics() {
        List<Object[]> rawResults = categoryRepository.findCategoryStatistics();
        
        return rawResults.stream()
                .map(this::mapToCategoryStatistics)
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryStatistics getCategoryDocuments(String categoryId) {
        // 获取分类信息
        DocumentCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
        
        // 获取该分类下的文档，使用分类的英文ID查询
        List<org.xue.agents.entity.Document> documents = documentRepository.findByCategoryOrderByUpdatedAtDesc(categoryId);
        
        // 转换为统计信息
        List<CategoryStatistics.DocumentInfo> docInfos = documents.stream()
                .map(this::mapToDocumentInfo)
                .collect(Collectors.toList());
        
        return CategoryStatistics.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryIcon(category.getIcon())
                .documentCount(documents.size())
                .lastUpdatedTime(documents.isEmpty() ? null : documents.get(0).getUpdatedAt())
                .completionRate(calculateCompletionRate(documents))
                .documents(docInfos)
                .build();
    }
    
    @Override
    public List<DocumentCategory> getAllCategories() {
        return categoryRepository.findByStatusOrderBySortOrder(DocumentCategory.Status.ACTIVE);
    }
    
    @Override
    public DocumentCategory getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
    }
    
    @Override
    @Transactional
    public DocumentCategory createCategory(CreateCategoryRequest request) {
        // 检查分类名称是否已存在
        DocumentCategory existing = categoryRepository.findByName(request.getName());
        if (existing != null) {
            throw new BusinessException("分类名称已存在: " + request.getName());
        }
        
        // 生成分类ID，确保唯一性
        String categoryId = generateUniqueCategoryId(request.getName());
        
        // 创建分类
        DocumentCategory category = DocumentCategory.builder()
                .id(categoryId)
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public DocumentCategory updateCategory(String categoryId, UpdateCategoryRequest request) {
        DocumentCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
        
        // 检查名称是否与其他分类冲突
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            DocumentCategory existing = categoryRepository.findByName(request.getName());
            if (existing != null && !existing.getId().equals(categoryId)) {
                throw new BusinessException("分类名称已存在: " + request.getName());
            }
            category.setName(request.getName());
        }
        
        // 更新其他字段
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        DocumentCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
        
        // 检查是否有文档使用该分类
        List<org.xue.agents.entity.Document> documentsInCategory = documentRepository.findByCategoryOrderByUpdatedAtDesc(categoryId);
        if (!documentsInCategory.isEmpty()) {
            throw new BusinessException("该分类下还有 " + documentsInCategory.size() + " 个文档，请先移动或删除这些文档");
        }
        
        categoryRepository.delete(category);
        log.info("分类已删除: {} ({})", category.getName(), categoryId);
    }
    
    @Override
    @Transactional
    public void submitFeedback(String recordId, Integer rating, String comment, String userId) {
        KnowledgeQaRecord record = qaRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("问答记录不存在: " + recordId));
        
        // 验证用户权限
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该记录");
        }
        
        record.setFeedbackRating(rating);
        record.setFeedbackComment(comment);
        qaRecordRepository.save(record);
        
        log.info("用户反馈已提交: recordId={}, rating={}, userId={}", recordId, rating, userId);
    }
    
    @Override
    public List<KnowledgeQaRecord> getUserQaHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return qaRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent();
    }
    
    @Override
    @Transactional
    public void updatePopularQuestionsTrend() {
        log.info("开始更新热门问题趋势得分");
        
        try {
            // 更新所有问题的趋势得分（基于时间衰减）
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(90); // 90天内的问题
            popularQuestionRepository.updateAllTrendScores(cutoffTime);
            
            // 清理过期的低频问题
            LocalDateTime oldCutoffTime = LocalDateTime.now().minusDays(180); // 180天前
            popularQuestionRepository.cleanupOldQuestions(oldCutoffTime, 2);
            
            log.info("热门问题趋势得分更新完成");
        } catch (Exception e) {
            log.error("更新热门问题趋势得分失败", e);
        }
    }
    
    // ================================ 用户隔离方法实现 ================================
    
    @Override
    public List<KnowledgeQaRecord> getUserRecentQuestions(String userId, int limit, String category) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (StringUtils.hasText(category)) {
            return qaRecordRepository.findByUserIdAndQuestionCategoryOrderByCreatedAtDesc(userId, category, pageable);
        } else {
            return qaRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent();
        }
    }
    
    @Override
    public List<PopularQuestion> getUserHotQuestions(String userId, int limit, String category) {
        int minCount = 2; // 至少被问过2次才算热门
        Pageable pageable = PageRequest.of(0, limit);
        
        log.debug("获取热门问题: userId={}, category={}, limit={}", userId, category, limit);
        
        // 直接使用全局热门问题（暂时不区分用户）
        if (StringUtils.hasText(category)) {
            return popularQuestionRepository.findHotQuestionsByCategoryByUserId(userId, category, minCount, pageable);
//            return popularQuestionRepository.findHotQuestionsByCategory(category, minCount, pageable);
        } else {
//            return popularQuestionRepository.findHotQuestions(minCount, pageable);
            return popularQuestionRepository.findHotQuestionsByUserId(userId, minCount, pageable);
        }
    }
    
    @Override
    public List<CategoryStatistics> getUserKnowledgeBaseStatistics(String userId) {
        log.debug("开始获取用户知识库统计: userId={}", userId);
        
        // 获取所有活跃分类
        List<DocumentCategory> userCategories = categoryRepository.findByUserIdAndStatusOrderBySortOrder(userId, DocumentCategory.Status.ACTIVE);
        List<DocumentCategory> systemCategories = categoryRepository.findByUserIdAndStatusOrderBySortOrder("system", DocumentCategory.Status.ACTIVE);

        // 合并用户分类和系统分类
        List<DocumentCategory> allCategories = new ArrayList<>();
        allCategories.addAll(userCategories);
        allCategories.addAll(systemCategories);
        log.debug("获取到{}个全局分类", allCategories.size());
        
        // 为每个分类统计用户的文档数量
        List<CategoryStatistics> result = new ArrayList<>();
        
        for (DocumentCategory category : allCategories) {
            // 查询该分类下的用户文档
            List<org.xue.agents.entity.Document> userDocs = documentRepository
                    .findByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category.getId());
            
            // 只有当用户在该分类下有文档时才包含该分类
            if (!userDocs.isEmpty()) {
                CategoryStatistics categoryStats = CategoryStatistics.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .categoryIcon(category.getIcon())
                        .documentCount(userDocs.size())
                        .lastUpdatedTime(userDocs.get(0).getUpdatedAt())
                        .completionRate(calculateCompletionRate(userDocs))
                        .build();
                
                result.add(categoryStats);
                log.debug("分类 {} ({}) 包含用户文档 {} 个", category.getName(), category.getId(), userDocs.size());
            }
        }
        
        log.info("用户 {} 的分类统计完成: 共 {} 个分类有文档", userId, result.size());
        return result;
    }
    
    @Override
    public CategoryStatistics getUserCategoryDocuments(String userId, String categoryId) {
        log.info("获取用户分类文档开始: userId={}, categoryId={}", userId, categoryId);
        
        try {
            // 临时跳过用户隔离检查，直接使用分类ID查询分类信息
            log.debug("查询分类信息: categoryId={}", categoryId);
            DocumentCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
            
            log.debug("分类信息查询成功: {}", category.getName());
            
            // 获取该用户在该分类下的文档（使用用户隔离）
            log.debug("查询用户分类文档: userId={}, categoryId={}", userId, categoryId);
            List<org.xue.agents.entity.Document> documents = documentRepository.findByUserIdAndCategoryOrderByUpdatedAtDesc(userId, categoryId);
            log.debug("查询到{}个文档", documents.size());
            
            // 转换为统计信息
            List<CategoryStatistics.DocumentInfo> docInfos = documents.stream()
                    .map(this::mapToDocumentInfo)
                    .collect(Collectors.toList());
            
            CategoryStatistics result = CategoryStatistics.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .categoryIcon(category.getIcon())
                    .documentCount(documents.size())
                    .lastUpdatedTime(documents.isEmpty() ? null : documents.get(0).getUpdatedAt())
                    .completionRate(calculateCompletionRate(documents))
                    .documents(docInfos)
                    .build();
            
            log.info("获取用户分类文档成功: userId={}, categoryId={}, 文档数={}", userId, categoryId, documents.size());
            return result;
        } catch (BusinessException e) {
            log.warn("业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取用户分类文档失败: userId={}, categoryId={}, error={}", userId, categoryId, e.getMessage(), e);
            // 最终降级到旧方法（无用户隔离）
            log.warn("降级到无用户隔离模式...");
            return getCategoryDocuments(categoryId);
        }
    }
    
    @Override
    public List<DocumentCategory> getUserCategories(String userId) {
        try {
            // 获取用户创建的分类和系统公共分类
            List<DocumentCategory> userCategories = categoryRepository.findByUserIdAndStatusOrderBySortOrder(userId, DocumentCategory.Status.ACTIVE);
            List<DocumentCategory> systemCategories = categoryRepository.findByUserIdAndStatusOrderBySortOrder("system", DocumentCategory.Status.ACTIVE);
            
            // 合并用户分类和系统分类
            List<DocumentCategory> allCategories = new ArrayList<>();
            allCategories.addAll(userCategories);
            allCategories.addAll(systemCategories);
            
            log.debug("获取用户分类: userId={}, 用户分类{}个, 系统分类{}个, 总计{}个", 
                userId, userCategories.size(), systemCategories.size(), allCategories.size());
            
            return allCategories;
        } catch (Exception e) {
            log.warn("获取用户分类列表失败，可能是数据库结构未更新: userId={}, error={}", userId, e.getMessage());
            // 降级到旧方法，返回所有激活的分类
            return categoryRepository.findByStatusOrderBySortOrder(DocumentCategory.Status.ACTIVE);
        }
    }
    
    @Override
    public DocumentCategory getUserCategoryById(String userId, String categoryId) {
        try {
            return categoryRepository.findByUserIdAndId(userId, categoryId)
                    .orElseThrow(() -> new BusinessException("分类不存在或无访问权限: " + categoryId, 404));
        } catch (Exception e) {
            log.warn("获取用户分类详情失败，可能是数据库结构未更新: userId={}, categoryId={}, error={}", userId, categoryId, e.getMessage());
            // 降级到旧方法
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("分类不存在: " + categoryId, 404));
        }
    }
    
    @Override
    @Transactional
    public DocumentCategory createUserCategory(String userId, CreateCategoryRequest request) {
        try {
            // 检查用户内分类名称是否已存在
            if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new BusinessException("分类名称已存在: " + request.getName());
            }
            
            // 生成分类ID，确保唯一性
            String categoryId = generateUniqueCategoryId(request.getName());
            
            // 创建分类
            DocumentCategory category = DocumentCategory.builder()
                    .id(categoryId)
                    .userId(userId) // 设置用户ID
                    .name(request.getName())
                    .description(request.getDescription())
                    .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                    .status(DocumentCategory.Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            return categoryRepository.save(category);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("创建用户分类失败，可能是数据库结构未更新: userId={}, name={}, error={}", userId, request.getName(), e.getMessage());
            
            // 降级到旧方法，但要确保设置用户ID
            try {
                // 检查全局分类名称是否已存在
                DocumentCategory existing = categoryRepository.findByName(request.getName());
                if (existing != null) {
                    throw new BusinessException("分类名称已存在: " + request.getName());
                }
                
                // 生成分类ID，确保唯一性
                String categoryId = generateUniqueCategoryId(request.getName());
                
                // 创建分类，强制设置用户ID（即使数据库字段不存在也不会报错）
                DocumentCategory category = DocumentCategory.builder()
                        .id(categoryId)
                        .userId(userId) // 强制设置用户ID
                        .name(request.getName())
                        .description(request.getDescription())
                        .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                        .status(DocumentCategory.Status.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                log.info("降级模式创建分类: userId={}, categoryId={}, name={}", userId, categoryId, request.getName());
                return categoryRepository.save(category);
            } catch (Exception fallbackError) {
                log.error("降级创建分类也失败: userId={}, name={}, error={}", userId, request.getName(), fallbackError.getMessage());
                throw new BusinessException("创建分类失败: " + fallbackError.getMessage());
            }
        }
    }
    
    @Override
    @Transactional
    public DocumentCategory updateUserCategory(String userId, String categoryId, UpdateCategoryRequest request) {
        try {
            DocumentCategory category = categoryRepository.findByUserIdAndId(userId, categoryId)
                    .orElseThrow(() -> new BusinessException("分类不存在或无访问权限: " + categoryId, 404));
            
            // 检查名称是否与用户的其他分类冲突
            if (request.getName() != null && !request.getName().equals(category.getName())) {
                if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
                    throw new BusinessException("分类名称已存在: " + request.getName());
                }
                category.setName(request.getName());
            }
            
            // 更新其他字段
            if (request.getDescription() != null) {
                category.setDescription(request.getDescription());
            }
            if (request.getSortOrder() != null) {
                category.setSortOrder(request.getSortOrder());
            }
            if (request.getStatus() != null) {
                category.setStatus(request.getStatus());
            }
            
            category.setUpdatedAt(LocalDateTime.now());
            return categoryRepository.save(category);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("更新用户分类失败，可能是数据库结构未更新: userId={}, categoryId={}, error={}", userId, categoryId, e.getMessage());
            // 降级到旧方法
            return updateCategory(categoryId, request);
        }
    }
    
    @Override
    @Transactional
    public void deleteUserCategory(String userId, String categoryId) {
        try {
            DocumentCategory category = categoryRepository.findByUserIdAndId(userId, categoryId)
                    .orElseThrow(() -> new BusinessException("分类不存在或无访问权限: " + categoryId, 404));
            
            // 检查该用户是否有文档使用该分类
            List<org.xue.agents.entity.Document> documentsInCategory = documentRepository.findByUserIdAndCategory(userId, categoryId);
            if (!documentsInCategory.isEmpty()) {
                throw new BusinessException("该分类下还有 " + documentsInCategory.size() + " 个文档，请先移动或删除这些文档");
            }
            
            categoryRepository.delete(category);
            log.info("用户 {} 删除分类: {} ({})", userId, category.getName(), categoryId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("删除用户分类失败，可能是数据库结构未更新: userId={}, categoryId={}, error={}", userId, categoryId, e.getMessage());
            // 降级到旧方法
            deleteCategory(categoryId);
        }
    }
    
    // ================================ 私有方法 ================================
    
    private KnowledgeQaRecord createQaRecord(String id, KnowledgeQaRequest request, String userId) {
        return KnowledgeQaRecord.builder()
                .id(id)
                .userId(userId)
                .question(request.getQuestion())
                .questionCategory(request.getCategory())
                .questionKeywords(extractKeywords(request.getQuestion()))
                .status(KnowledgeQaRecord.Status.PROCESSING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private List<Document> searchRelevantDocuments(KnowledgeQaRequest request, String userId) {
        try {
//            SearchRequest.Builder searchBuilder = SearchRequest.builder()
//                    .query(request.getQuestion())
//                    .topK(request.getTopK()) // 扩大检索范围，为用户筛选留出余量
//                    .similarityThreshold(request.getSimilarityThreshold());
//
            MilvusSearchRequest.MilvusBuilder searchBuilder = MilvusSearchRequest.milvusBuilder()
                .query(request.getQuestion())
                .topK(request.getTopK())
                .similarityThreshold(request.getSimilarityThreshold());

            // 只添加分类过滤条件（如果指定了分类），不在向量库层面进行用户过滤
            if (StringUtils.hasText(request.getCategory())) {
//                searchBuilder.filterExpression("category == '" + request.getCategory() + "'");
//                searchBuilder.nativeExpression("metadata['category'] == 'science'");
                searchBuilder.nativeExpression("metadata['category'] == '" + request.getCategory() + "'");
            }
            
            SearchRequest searchRequest = searchBuilder.build();
            
            log.debug("向量检索参数: category={}, topK={}, threshold={}, filter={}", 
                request.getCategory(), request.getTopK(), request.getSimilarityThreshold(),
                StringUtils.hasText(request.getCategory()) ? "category == '" + request.getCategory() + "'" : "none");

            // 执行向量检索
            List<Document> allResults = milvusVectorStore.similaritySearch(searchRequest);
            log.debug("向量检索完成: 总共找到{}个文档块", allResults.size());
            
            // 在结果层面进行严格的用户隔离筛选
            List<Document> userFilteredResults = filterDocumentsByUserId(allResults, userId);
            
            // 限制最终结果数量为用户请求的topK
            List<Document> finalResults = userFilteredResults.stream()
                    .limit(request.getTopK())
                    .collect(Collectors.toList());
            
            log.info("用户隔离筛选完成: 用户={}, 问题={}, 总检索{}个 -> 用户筛选{}个 -> 最终返回{}个", 
                userId, request.getQuestion(), allResults.size(), userFilteredResults.size(), finalResults.size());
            
            return finalResults;
            
        } catch (Exception e) {
            log.error("向量检索失败: userId={}, question={}, error={}", userId, request.getQuestion(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 在检索结果中按用户ID进行严格筛选
     */
    private List<Document> filterDocumentsByUserId(List<Document> documents, String currentUserId) {
        if (documents.isEmpty()) {
            return documents;
        }
        
        List<Document> userDocuments = new ArrayList<>();
        int totalCount = documents.size();
        int userCount = 0;
        int noUserIdCount = 0;
        int otherUserCount = 0;
        
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            String docUserId = (String) metadata.get("user_id");
            
            if (docUserId == null || docUserId.trim().isEmpty()) {
                // 文档没有用户ID信息，可能是旧数据，暂时包含但记录警告
                noUserIdCount++;
                log.debug("发现无用户ID的文档: documentId={}", metadata.get("document_id"));
                // 暂时不包含无用户ID的文档，严格用户隔离
                // userDocuments.add(doc);
            } else if (currentUserId.equals(docUserId)) {
                // 属于当前用户的文档
                userCount++;
                userDocuments.add(doc);
            } else {
                // 属于其他用户的文档，严格排除
                otherUserCount++;
                log.debug("排除其他用户文档: documentId={}, docUserId={}, currentUserId={}", 
                    metadata.get("document_id"), docUserId, currentUserId);
            }
        }
        
        log.debug("用户文档筛选统计: 总数={}, 当前用户={}, 无用户ID={}, 其他用户={}", 
            totalCount, userCount, noUserIdCount, otherUserCount);
        
        if (noUserIdCount > 0) {
            log.warn("发现{}个无用户ID的文档，已严格排除。建议重新处理这些文档以添加用户信息", noUserIdCount);
        }
        
        return userDocuments;
    }
    
    private String buildContext(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return "暂无相关文档信息。";
        }
        
        StringBuilder context = new StringBuilder();
        int totalLength = 0;
        final int MAX_CONTEXT_LENGTH = 4000; // 限制上下文总长度
        final int MIN_FRAGMENT_LENGTH = 50;   // 最小片段长度
        
        for (int i = 0; i < documents.size() && totalLength < MAX_CONTEXT_LENGTH; i++) {
            Document doc = documents.get(i);
            String content = doc.getText();
            
            // 内容质量检查
            if (content == null || content.trim().length() < MIN_FRAGMENT_LENGTH) {
                log.debug("跳过质量不佳的文档片段: 长度={}", content != null ? content.length() : 0);
                continue;
            }
            
            // 检查内容是否过于混乱（包含大量特殊字符或格式标记）
            String cleanContent = content.replaceAll("[\\p{Punct}&&[^\\u4e00-\\u9fa5]]+", " ").trim();
            if (cleanContent.length() < content.length() * 0.3) {
                log.debug("跳过格式混乱的文档片段: 原长度={}, 清理后长度={}", content.length(), cleanContent.length());
                continue;
            }
            
            // 限制单个片段长度
            String truncatedContent = content.length() > 800 ? content.substring(0, 800) + "..." : content;
            
            // 检查是否会超出总长度限制
            String fragmentText = String.format("文档%d: %s\n\n", i + 1, truncatedContent);
            if (totalLength + fragmentText.length() > MAX_CONTEXT_LENGTH) {
                break;
            }
            
            context.append(fragmentText);
            totalLength += fragmentText.length();
            
            log.debug("添加文档片段{}: 长度={}, 累计长度={}", i + 1, truncatedContent.length(), totalLength);
        }
        
        String finalContext = context.toString();
        log.info("最终上下文构建完成: 总长度={}, 包含文档片段数={}", 
            finalContext.length(), 
            finalContext.split("文档\\d+:").length - 1);
        
        return finalContext.isEmpty() ? "找到的文档内容质量不佳，无法提供准确回答。" : finalContext;
    }
    
    private String generateAnswer(String context, String question) {
        String prompt = KNOWLEDGE_QA_PROMPT
                .replace("{context}", context)
                .replace("{question}", question);
        
        return ChatClient.create(chatModel)
                .prompt(prompt)
                .call()
                .content();
    }
    
    private List<KnowledgeQaResponse.SourceDocument> buildSourceDocuments(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    
                    // 安全地转换数值类型，处理可能的Double to Integer转换
                    Integer page = null;
                    if (metadata.get("page") instanceof Number) {
                        page = ((Number) metadata.get("page")).intValue();
                    }
                    
                    Integer chunkIndex = null;
                    if (metadata.get("chunk_index") instanceof Number) {
                        chunkIndex = ((Number) metadata.get("chunk_index")).intValue();
                    }
                    
                    Double score = 0.0;
                    if (metadata.get("distance") instanceof Number) {
                        score = ((Number) metadata.get("distance")).doubleValue();
                    }
                    
                    // 获取文档标题：优先从metadata获取，如果没有则从数据库查询
                    String title = getDocumentTitle(metadata);
                    
                    return KnowledgeQaResponse.SourceDocument.builder()
                            .documentId((String) metadata.get("document_id"))
                            .title(title)
                            .content(doc.getText())
                            .page(page)
                            .chunkIndex(chunkIndex)
                            .score(score)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取文档标题
     */
    private String getDocumentTitle(Map<String, Object> metadata) {
        // 添加调试日志
        log.debug("获取文档标题，metadata内容: {}", metadata);
        
        // 优先从metadata的document_name字段获取
        String title = (String) metadata.get("document_name");
        if (StringUtils.hasText(title)) {
            log.debug("从metadata.document_name获取标题: {}", title);
            return title;
        }
        
        // 兼容旧的title字段
        title = (String) metadata.get("title");
        if (StringUtils.hasText(title)) {
            log.debug("从metadata.title获取标题: {}", title);
            return title;
        }
        
        // 如果metadata中没有，从数据库查询
        String documentId = (String) metadata.get("document_id");
        if (StringUtils.hasText(documentId)) {
            try {
                Optional<org.xue.agents.entity.Document> dbDoc = documentRepository.findById(documentId);
                if (dbDoc.isPresent()) {
                    String dbTitle = dbDoc.get().getName();
                    log.debug("从数据库获取标题: {}", dbTitle);
                    return dbTitle;
                }
            } catch (Exception e) {
                log.warn("查询文档名称失败: documentId={}", documentId, e);
            }
        }
        
        log.debug("无法获取文档标题，返回默认值");
        return "未知文档";
    }
    
    private void updateQaRecord(KnowledgeQaRecord record, String answer, 
                               List<KnowledgeQaResponse.SourceDocument> sources, 
                               int responseTime, Double similarity) {
        try {
            record.setAnswer(answer);
            record.setContextSources(objectMapper.writeValueAsString(sources));
            record.setResponseTimeMs(responseTime);
            record.setSimilarityScore(similarity);
            record.setStatus(KnowledgeQaRecord.Status.COMPLETED);
            record.setUpdatedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("更新问答记录失败", e);
            record.setStatus(KnowledgeQaRecord.Status.FAILED);
        }
    }
    
    /**
     * 异步更新热门问题统计（修复异步任务中无法获取用户ID的问题）
     */
    private void updatePopularQuestionAsync(String question, String category, String userId) {
        CompletableFuture.runAsync(() -> {
            try {
                String questionPattern = normalizeQuestion(question);
                
                log.debug("更新热门问题统计: userId={}, question={}, category={}, pattern={}", 
                    userId, question, category, questionPattern);
                
                // 直接使用传递的userId，而不是从SecurityContext获取
                if (StringUtils.hasText(userId)) {
                    // 使用用户隔离的查询
                    Optional<PopularQuestion> existingOpt = popularQuestionRepository
                            .findByUserIdAndQuestionPatternAndCategory(userId, questionPattern, category);
                    
                    if (existingOpt.isPresent()) {
                        // 更新现有记录
                        PopularQuestion existing = existingOpt.get();
                        existing.setQuestionCount(existing.getQuestionCount() + 1);
                        existing.setLastAskedTime(LocalDateTime.now());
                        popularQuestionRepository.save(existing);
                        
                        log.debug("更新现有热门问题记录: questionPattern={}, count={}", 
                            questionPattern, existing.getQuestionCount());
                    } else {
                        // 1. 生成文档ID
                        String popularId = UUID.randomUUID().toString();
                        // 创建新热门问题记录
                        PopularQuestion newPopular = PopularQuestion.builder()
                                .id(popularId)
                                .userId(userId)
                                .questionPattern(questionPattern)
                                .representativeQuestion(question)
                                .questionCount(1)
                                .category(category)
                                .lastAskedTime(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .build();
                        
                        popularQuestionRepository.save(newPopular);
                        
                        log.debug("创建新热门问题记录: userId={}, questionPattern={}, question={}", 
                            userId, questionPattern, question);
                    }
                } else {
                    log.warn("用户ID为空，无法更新热门问题统计: question={}, category={}", question, category);
                }
            } catch (Exception e) {
                log.error("异步更新热门问题统计失败: question={}, category={}, userId={}", question, category, userId, e);
            }
        });
    }
    
    private String normalizeQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return "";
        }
        
        // 简单的问题标准化：去除标点符号，转换为小写，去除多余空格
        return question.toLowerCase()
                .replaceAll("[\\p{Punct}\\s]+", " ")
                .trim();
    }
    
    private String extractKeywords(String question) {
        if (!StringUtils.hasText(question)) {
            return "";
        }
        
        // 简单的关键词提取：移除停用词，提取名词性词汇
        String[] words = question.split("\\s+");
        Set<String> stopWords = Set.of("的", "了", "是", "在", "有", "和", "与", "或", "但", "而", "如何", "什么", "怎么", "为什么");
        
        return Arrays.stream(words)
                .filter(word -> word.length() > 1)
                .filter(word -> !stopWords.contains(word))
                .filter(word -> Pattern.matches("[\u4e00-\u9fa5a-zA-Z0-9]+", word))
                .distinct()
                .limit(10)
                .collect(Collectors.joining(","));
    }
    
    private BigDecimal calculateTrendScore(int questionCount, LocalDateTime lastAskedTime) {
        // 趋势得分 = 问题频次 * 时间衰减因子
        long daysSinceLastAsked = java.time.Duration.between(lastAskedTime, LocalDateTime.now()).toDays();
        double timeDecayFactor = Math.exp(-daysSinceLastAsked / 30.0); // 30天衰减周期
        
        return BigDecimal.valueOf(questionCount * timeDecayFactor);
    }
    
    private CategoryStatistics mapToCategoryStatistics(Object[] row) {
        // 处理时间类型转换：数据库返回的可能是java.sql.Timestamp，需要转换为LocalDateTime
        LocalDateTime lastUpdatedTime = null;
        if (row[4] != null) {
            if (row[4] instanceof java.sql.Timestamp) {
                lastUpdatedTime = ((java.sql.Timestamp) row[4]).toLocalDateTime();
            } else if (row[4] instanceof LocalDateTime) {
                lastUpdatedTime = (LocalDateTime) row[4];
            }
        }
        
        return CategoryStatistics.builder()
                .categoryId((String) row[0])
                .categoryName((String) row[1])
                .categoryIcon((String) row[2])
                .documentCount(((Number) row[3]).intValue())
                .lastUpdatedTime(lastUpdatedTime)
                .completionRate(((Number) row[5]).doubleValue())
                .build();
    }
    
    private CategoryStatistics.DocumentInfo mapToDocumentInfo(org.xue.agents.entity.Document doc) {
        return CategoryStatistics.DocumentInfo.builder()
                .id(doc.getId())
                .name(doc.getName())
                .size(doc.getSize())
                .type(doc.getType())
                .status(doc.getStatus().name())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
    
    private Double calculateCompletionRate(List<org.xue.agents.entity.Document> documents) {
        if (documents.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = documents.stream()
                .mapToLong(doc -> doc.getStatus() == org.xue.agents.entity.Document.Status.COMPLETED ? 1 : 0)
                .sum();
        
        return (double) completedCount / documents.size();
    }
    
    private String generateUniqueCategoryId(String name) {
        // 基础ID生成：移除特殊字符，转换为合法的ID格式
        String baseId = name.toLowerCase()
                .replaceAll("[\\s\\-]+", "_")           // 空格和横线转为下划线
                .replaceAll("[^a-z0-9_\\u4e00-\\u9fa5]", "") // 只保留字母、数字、下划线和中文
                .replaceAll("_+", "_")                  // 多个下划线合并为一个
                .replaceAll("^_|_$", "");               // 去除首尾下划线
        
        // 如果处理后为空，使用默认名称
        if (baseId.isEmpty()) {
            baseId = "category";
        }
        
        // 生成完整ID
        String categoryId = "cat_" + baseId;
        
        // 检查ID是否已存在，如果存在则添加数字后缀
        String finalId = categoryId;
        int counter = 1;
        while (categoryRepository.findById(finalId).isPresent()) {
            finalId = categoryId + "_" + counter;
            counter++;
        }
        
        log.info("为分类 '{}' 生成ID: {}", name, finalId);
        return finalId;
    }
    
    /**
     * 初始化默认分类数据
     */
    @PostConstruct
    public void initializeDefaultCategories() {
        try {
            // 检查是否已有分类数据
            long categoryCount = categoryRepository.count();
            if (categoryCount == 0) {
                log.info("初始化默认分类数据...");
                createDefaultCategories();
            } else {
                log.info("已有 {} 个分类，跳过默认分类初始化", categoryCount);
            }
        } catch (Exception e) {
            log.error("初始化默认分类失败", e);
        }
    }
    
    /**
     * 创建默认分类（不带用户ID，为所有用户可见的公共分类）
     */
    private void createDefaultCategories() {
        List<DocumentCategory> defaultCategories = Arrays.asList(
            DocumentCategory.builder()
                .id("cat_user_manual")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("用户手册")
                .description("产品使用指南和用户操作手册")
                .icon("book")
                .sortOrder(1)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            DocumentCategory.builder()
                .id("cat_technical_doc")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("技术文档")
                .description("开发文档、API文档、技术规范")
                .icon("code")
                .sortOrder(2)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            DocumentCategory.builder()
                .id("cat_training_material")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("培训材料")
                .description("培训课件、学习资料")
                .icon("graduation-cap")
                .sortOrder(3)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            DocumentCategory.builder()
                .id("cat_faq")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("常见问题")
                .description("常见问题解答、疑难解答")
                .icon("question-circle")
                .sortOrder(4)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            DocumentCategory.builder()
                .id("cat_policy")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("政策制度")
                .description("公司政策、规章制度")
                .icon("file-text")
                .sortOrder(5)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            DocumentCategory.builder()
                .id("cat_other")
                .userId("system") // 设置为系统用户，表示公共分类
                .name("其他")
                .description("其他类型文档")
                .icon("folder")
                .sortOrder(99)
                .status(DocumentCategory.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        try {
            categoryRepository.saveAll(defaultCategories);
            log.info("成功创建 {} 个默认分类（系统公共分类）", defaultCategories.size());
        } catch (Exception e) {
            log.error("创建默认分类失败", e);
        }
    }
} 