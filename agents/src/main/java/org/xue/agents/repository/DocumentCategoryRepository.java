package org.xue.agents.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.agents.entity.DocumentCategory;

import java.util.List;
import java.util.Optional;

/**
 * 文档分类Repository
 */
@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, String> {
    
    /**
     * 根据状态查询分类，按排序序号排序
     */
    List<DocumentCategory> findByStatusOrderBySortOrder(DocumentCategory.Status status);
    
    /**
     * 根据用户ID和状态查询分类，按排序序号排序（用户隔离）
     */
    List<DocumentCategory> findByUserIdAndStatusOrderBySortOrder(String userId, DocumentCategory.Status status);
    
    /**
     * 根据名称查找分类
     */
    DocumentCategory findByName(String name);
    
    /**
     * 根据用户ID和名称查找分类（用户隔离）
     */
    Optional<DocumentCategory> findByUserIdAndName(String userId, String name);
    
    /**
     * 根据用户ID查找分类（用户隔离）
     */
    List<DocumentCategory> findByUserId(String userId);
    
    /**
     * 根据用户ID和分类ID查找分类（用户隔离）
     */
    Optional<DocumentCategory> findByUserIdAndId(String userId, String id);
    
    /**
     * 查询分类统计信息（包含文档数量）
     */
    @Query("SELECT c.id, c.name, c.icon, COUNT(d), MAX(d.updatedAt), " +
           "CAST(SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS double) / COUNT(d) " +
           "FROM DocumentCategory c LEFT JOIN Document d ON c.id = d.category " +
           "WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.id, c.name, c.icon " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> findCategoryStatistics();
    
    /**
     * 查询用户的分类统计信息（包含文档数量，用户隔离）
     */
    @Query("SELECT c.id, c.name, c.icon, COUNT(d), MAX(d.updatedAt), " +
           "CAST(SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS double) / COUNT(d) " +
           "FROM DocumentCategory c LEFT JOIN Document d ON c.id = d.category AND d.userId = :userId " +
           "WHERE c.userId = :userId AND c.status = 'ACTIVE' " +
           "GROUP BY c.id, c.name, c.icon " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> findCategoryStatisticsByUserId(@Param("userId") String userId);
    
    /**
     * 检查用户是否拥有指定分类
     */
    boolean existsByUserIdAndId(String userId, String id);
    
    /**
     * 检查用户是否已有同名分类
     */
    boolean existsByUserIdAndName(String userId, String name);
} 