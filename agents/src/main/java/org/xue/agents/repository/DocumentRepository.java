package org.xue.agents.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.agents.entity.Document;

import java.util.List;
import java.util.Optional;

/**
 * 文档数据访问层
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    
    /**
     * 根据用户ID查找文档列表，按更新时间降序排列
     */
    List<Document> findByUserIdOrderByUpdatedAtDesc(String userId);
    
    /**
     * 根据文档ID和用户ID查找文档
     */
    Optional<Document> findByIdAndUserId(String id, String userId);
    
    /**
     * 根据用户ID和状态查找文档
     */
    List<Document> findByUserIdAndStatus(String userId, Document.Status status);
    
    /**
     * 统计用户的文档数量
     */
    long countByUserId(String userId);
    
    /**
     * 统计用户特定状态的文档数量
     */
    long countByUserIdAndStatus(String userId, Document.Status status);
    
    /**
     * 根据用户ID删除所有文档
     */
    void deleteAllByUserId(String userId);
    
    /**
     * 检查文档是否属于指定用户
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Document d WHERE d.id = :documentId AND d.userId = :userId")
    boolean existsByIdAndUserId(@Param("documentId") String documentId, @Param("userId") String userId);
    
    /**
     * 根据文档名称和用户ID模糊查询
     */
    List<Document> findByUserIdAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(String userId, String name);

    List<Document> findByCategoryOrderByUpdatedAtDesc(String categoryId);
    
    /**
     * 根据用户ID和分类查询文档（用户隔离）
     */
    List<Document> findByUserIdAndCategoryOrderByUpdatedAtDesc(String userId, String categoryId);
    
    /**
     * 根据用户ID和分类查询文档（简单版本）
     */
    List<Document> findByUserIdAndCategory(String userId, String categoryId);
    
    /**
     * 查询用户的分类统计信息
     */
    @Query("SELECT d.category, c.name, c.icon, COUNT(d), MAX(d.updatedAt) " +
           "FROM Document d LEFT JOIN DocumentCategory c ON d.category = c.id " +
           "WHERE d.userId = :userId AND d.status = 'COMPLETED' " +
           "GROUP BY d.category, c.name, c.icon " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> findCategoryStatisticsByUserId(@Param("userId") String userId);
}