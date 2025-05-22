package org.xue.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xue.app.entity.Order;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 订单数据访问层
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    
    /**
     * 根据订单号查找订单
     *
     * @param orderNumber 订单号
     * @return 订单对象（可能为空）
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 根据客户名称查找订单并分页
     *
     * @param customerName 客户名称
     * @param pageable 分页参数
     * @return 分页订单结果
     */
    Page<Order> findByCustomerNameContaining(String customerName, Pageable pageable);
    
    /**
     * 根据订单状态查找订单并分页
     *
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 分页订单结果
     */
    Page<Order> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据创建时间范围查找订单并分页
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 分页订单结果
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 复合条件查询订单
     *
     * @param keyword 关键字（匹配订单号或客户名称）
     * @param status 订单状态（可为空）
     * @param startTime 开始时间（可为空）
     * @param endTime 结束时间（可为空）
     * @param pageable 分页参数
     * @return 分页订单结果
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR o.orderNumber LIKE %:keyword% OR o.customerName LIKE %:keyword%) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:startTime IS NULL OR o.createdAt >= :startTime) " +
            "AND (:endTime IS NULL OR o.createdAt <= :endTime)")
    Page<Order> findByConditions(
            @Param("keyword") String keyword, 
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
} 