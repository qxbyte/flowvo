package org.xue.agents.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.agents.entity.KnowledgeQaRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 知识库问答记录Repository
 */
@Repository
public interface KnowledgeQaRecordRepository extends JpaRepository<KnowledgeQaRecord, String> {
    
    /**
     * 根据用户ID查询问答记录（分页）
     */
    Page<KnowledgeQaRecord> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查询问答记录
     */
    List<KnowledgeQaRecord> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, KnowledgeQaRecord.Status status);
    
    /**
     * 查询最近的问答记录（用于"最近提问"功能）
     */
    @Query("SELECT qa FROM KnowledgeQaRecord qa WHERE qa.status = 'COMPLETED' ORDER BY qa.createdAt DESC")
    List<KnowledgeQaRecord> findRecentQuestions(Pageable pageable);
    
    /**
     * 根据分类查询最近的问答记录
     */
    @Query("SELECT qa FROM KnowledgeQaRecord qa WHERE qa.status = 'COMPLETED' AND qa.questionCategory = :category ORDER BY qa.createdAt DESC")
    List<KnowledgeQaRecord> findRecentQuestionsByCategory(@Param("category") String category, Pageable pageable);
    
    /**
     * 统计用户问答记录数量
     */
    long countByUserIdAndStatus(String userId, KnowledgeQaRecord.Status status);
    
    /**
     * 查询某时间范围内的问答记录（用于热门问题统计）
     */
    @Query("SELECT qa FROM KnowledgeQaRecord qa WHERE qa.status = 'COMPLETED' AND qa.createdAt >= :startTime AND qa.createdAt <= :endTime")
    List<KnowledgeQaRecord> findQuestionsInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据关键词搜索问答记录
     */
    @Query("SELECT qa FROM KnowledgeQaRecord qa WHERE qa.status = 'COMPLETED' AND (qa.question LIKE %:keyword% OR qa.questionKeywords LIKE %:keyword%)")
    List<KnowledgeQaRecord> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据用户ID和分类查询最近的问答记录（用户隔离）
     */
    @Query("SELECT qa FROM KnowledgeQaRecord qa WHERE qa.userId = :userId AND qa.status = 'COMPLETED' AND qa.questionCategory = :category ORDER BY qa.createdAt DESC")
    List<KnowledgeQaRecord> findByUserIdAndQuestionCategoryOrderByCreatedAtDesc(@Param("userId") String userId, @Param("category") String category, Pageable pageable);
} 