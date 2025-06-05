package org.xue.agents.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.agents.entity.PopularQuestion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 热门问题Repository
 */
@Repository
public interface PopularQuestionRepository extends JpaRepository<PopularQuestion, String> {
    
    /**
     * 根据问题模式和分类查询热门问题
     */
    Optional<PopularQuestion> findByQuestionPatternAndCategory(String questionPattern, String category);
    
    /**
     * 根据用户ID、问题模式和分类查询热门问题（用户隔离）
     */
    Optional<PopularQuestion> findByUserIdAndQuestionPatternAndCategory(String userId, String questionPattern, String category);
    
    /**
     * 查询热门问题（不分用户）
     */
    @Query("SELECT pq FROM PopularQuestion pq WHERE pq.questionCount >= :minCount ORDER BY pq.trendScore DESC")
    List<PopularQuestion> findHotQuestions(@Param("minCount") int minCount, Pageable pageable);
    
    /**
     * 根据分类查询热门问题（不分用户）
     */
    @Query("SELECT pq FROM PopularQuestion pq WHERE pq.category = :category AND pq.questionCount >= :minCount ORDER BY pq.trendScore DESC")
    List<PopularQuestion> findHotQuestionsByCategory(@Param("category") String category, @Param("minCount") int minCount, Pageable pageable);
    
    /**
     * 查询用户的热门问题（用户隔离）
     */
    @Query("SELECT pq FROM PopularQuestion pq WHERE pq.userId = :userId AND pq.questionCount >= :minCount ORDER BY pq.trendScore DESC")
    List<PopularQuestion> findHotQuestionsByUserId(@Param("userId") String userId, @Param("minCount") int minCount, Pageable pageable);

      /**
     * 根据分类查询热门问题
     */
    @Query("SELECT pq FROM PopularQuestion pq WHERE pq.userId = :userId AND pq.category = :category AND pq.questionCount >= :minCount ORDER BY pq.trendScore DESC")
    List<PopularQuestion> findHotQuestionsByCategoryByUserId(@Param("userId") String userId, @Param("category") String category, @Param("minCount") int minCount, Pageable pageable);


    /**
     * 根据用户ID和分类查询热门问题（用户隔离）
     */
    @Query("SELECT pq FROM PopularQuestion pq WHERE pq.userId = :userId AND pq.category = :category AND pq.questionCount >= :minCount ORDER BY pq.trendScore DESC")
    List<PopularQuestion> findHotQuestionsByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category, @Param("minCount") int minCount, Pageable pageable);
    
    /**
     * 更新所有问题的趋势得分（基于时间衰减）
     */
    @Modifying
    @Query("UPDATE PopularQuestion pq SET pq.trendScore = pq.questionCount * EXP(-1.0 * (TIMESTAMPDIFF(DAY, pq.lastAskedTime, CURRENT_TIMESTAMP)) / 30.0) WHERE pq.lastAskedTime >= :cutoffTime")
    void updateAllTrendScores(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 清理过期的低频问题
     */
    @Modifying
    @Query("DELETE FROM PopularQuestion pq WHERE pq.lastAskedTime < :cutoffTime AND pq.questionCount < :minCount")
    void cleanupOldQuestions(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("minCount") int minCount);
    
    /**
     * 根据用户ID查找所有热门问题
     */
    List<PopularQuestion> findByUserId(String userId);
    
    /**
     * 根据用户ID删除所有热门问题
     */
    void deleteByUserId(String userId);
} 