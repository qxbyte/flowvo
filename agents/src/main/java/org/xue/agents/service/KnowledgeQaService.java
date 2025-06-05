package org.xue.agents.service;

import org.xue.agents.dto.CategoryStatistics;
import org.xue.agents.dto.CreateCategoryRequest;
import org.xue.agents.dto.KnowledgeQaRequest;
import org.xue.agents.dto.KnowledgeQaResponse;
import org.xue.agents.dto.UpdateCategoryRequest;
import org.xue.agents.entity.DocumentCategory;
import org.xue.agents.entity.KnowledgeQaRecord;
import org.xue.agents.entity.PopularQuestion;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 知识库问答服务接口
 */
public interface KnowledgeQaService {
    
    /**
     * 处理知识库问答（同步）
     * @param request 问答请求
     * @param userId 用户ID
     * @return 问答响应
     */
    KnowledgeQaResponse askQuestion(KnowledgeQaRequest request, String userId);
    
    /**
     * 处理知识库问答（流式）
     * @param request 问答请求
     * @param userId 用户ID
     * @return 流式响应
     */
    Flux<String> askQuestionStream(KnowledgeQaRequest request, String userId);
    
    /**
     * 获取最近提问
     * @param limit 限制数量
     * @param category 分类过滤（可选）
     * @return 最近问题列表
     */
    List<KnowledgeQaRecord> getRecentQuestions(int limit, String category);
    
    /**
     * 获取热门问题
     * @param limit 限制数量
     * @param category 分类过滤（可选）
     * @return 热门问题列表
     */
    List<PopularQuestion> getHotQuestions(int limit, String category);
    
    /**
     * 获取知识库分类统计信息
     * @return 分类统计列表
     */
    List<CategoryStatistics> getKnowledgeBaseStatistics();
    
    /**
     * 获取分类下的文档列表
     * @param categoryId 分类ID
     * @return 分类统计信息（包含文档列表）
     */
    CategoryStatistics getCategoryDocuments(String categoryId);
    
    /**
     * 获取所有文档分类
     * @return 分类列表
     */
    List<DocumentCategory> getAllCategories();
    
    /**
     * 根据ID获取分类详情
     * @param categoryId 分类ID
     * @return 分类信息
     */
    DocumentCategory getCategoryById(String categoryId);
    
    /**
     * 创建分类
     * @param request 创建请求
     * @return 创建的分类
     */
    DocumentCategory createCategory(CreateCategoryRequest request);
    
    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新后的分类
     */
    DocumentCategory updateCategory(String categoryId, UpdateCategoryRequest request);
    
    /**
     * 删除分类
     * @param categoryId 分类ID
     */
    void deleteCategory(String categoryId);
    
    /**
     * 用户反馈
     * @param recordId 问答记录ID
     * @param rating 评分（1-5）
     * @param comment 评论
     * @param userId 用户ID
     */
    void submitFeedback(String recordId, Integer rating, String comment, String userId);
    
    /**
     * 获取用户的问答历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 问答记录列表
     */
    List<KnowledgeQaRecord> getUserQaHistory(String userId, int page, int size);
    
    /**
     * 更新热门问题趋势得分（定时任务调用）
     */
    void updatePopularQuestionsTrend();
    
    // ================================ 用户隔离方法 ================================
    
    /**
     * 获取用户的最近提问
     * @param userId 用户ID
     * @param limit 限制数量
     * @param category 分类过滤（可选）
     * @return 用户最近问题列表
     */
    List<KnowledgeQaRecord> getUserRecentQuestions(String userId, int limit, String category);
    
    /**
     * 获取用户的热门问题
     * @param userId 用户ID
     * @param limit 限制数量
     * @param category 分类过滤（可选）
     * @return 用户热门问题列表
     */
    List<PopularQuestion> getUserHotQuestions(String userId, int limit, String category);
    
    /**
     * 获取用户的知识库分类统计信息
     * @param userId 用户ID
     * @return 用户分类统计列表
     */
    List<CategoryStatistics> getUserKnowledgeBaseStatistics(String userId);
    
    /**
     * 获取用户分类下的文档列表
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 分类统计信息（包含文档列表）
     */
    CategoryStatistics getUserCategoryDocuments(String userId, String categoryId);
    
    /**
     * 获取用户的所有文档分类
     * @param userId 用户ID
     * @return 用户分类列表
     */
    List<DocumentCategory> getUserCategories(String userId);
    
    /**
     * 根据ID获取用户的分类详情
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 分类信息
     */
    DocumentCategory getUserCategoryById(String userId, String categoryId);
    
    /**
     * 为用户创建分类
     * @param userId 用户ID
     * @param request 创建请求
     * @return 创建的分类
     */
    DocumentCategory createUserCategory(String userId, CreateCategoryRequest request);
    
    /**
     * 更新用户的分类
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新后的分类
     */
    DocumentCategory updateUserCategory(String userId, String categoryId, UpdateCategoryRequest request);
    
    /**
     * 删除用户的分类
     * @param userId 用户ID
     * @param categoryId 分类ID
     */
    void deleteUserCategory(String userId, String categoryId);
} 