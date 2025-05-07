package org.xue.assistant.functioncall.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.xue.assistant.functioncall.entity.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    
    /**
     * 根据业务名称模糊查询业务列表
     * @param name 业务名称
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    Page<Business> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 根据业务类型查询业务列表
     * @param type 业务类型
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    Page<Business> findByType(String type, Pageable pageable);
    
    /**
     * 根据业务状态查询业务列表
     * @param status 业务状态
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    Page<Business> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据业务名称和状态查询业务列表
     * @param name 业务名称
     * @param status 业务状态
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    Page<Business> findByNameContainingAndStatus(String name, String status, Pageable pageable);
    
    /**
     * 复合查询业务列表
     * @param name 业务名称（可选）
     * @param type 业务类型（可选）
     * @param status 业务状态（可选）
     * @param pageable 分页参数
     * @return 业务分页列表
     */
    @Query("SELECT b FROM Business b WHERE (:name IS NULL OR b.name LIKE %:name%) " +
           "AND (:type IS NULL OR b.type = :type) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Business> findByConditions(
            @Param("name") String name,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable);
}