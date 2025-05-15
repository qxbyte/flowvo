package org.xue.core.functioncall.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.xue.core.functioncall.entity.Order;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据订单号模糊查询订单列表
     * @param orderNo 订单号
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByOrderNoContaining(String orderNo, Pageable pageable);

    /**
     * 根据订单号精确查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据客户ID查询订单列表
     * @param customerId 客户ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    /**
     * 根据订单状态查询订单列表
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByStatus(String status, Pageable pageable);
    
    /**
     * 根据创建时间范围查询订单列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 复合查询订单列表
     * @param orderNo 订单号（可选）
     * @param customerId 客户ID（可选）
     * @param status 订单状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    @Query("SELECT o FROM Order o WHERE (:orderNo IS NULL OR o.orderNo LIKE %:orderNo%) " +
           "AND (:customerId IS NULL OR o.customer.id = :customerId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND ((:startTime IS NULL AND :endTime IS NULL) OR " +
           "     (:startTime IS NULL AND o.createTime <= :endTime) OR " +
           "     (:endTime IS NULL AND o.createTime >= :startTime) OR " +
           "     (o.createTime BETWEEN :startTime AND :endTime))")
    Page<Order> findByConditions(
            @Param("orderNo") String orderNo,
            @Param("customerId") Long customerId,
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 统计客户的订单数量
     * @param customerId 客户ID
     * @return 订单数量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 查询客户的最近一次订单时间
     * @param customerId 客户ID
     * @return 最近一次订单时间
     */
    @Query("SELECT MAX(o.createTime) FROM Order o WHERE o.customer.id = :customerId")
    LocalDateTime findLatestOrderTimeByCustomerId(@Param("customerId") Long customerId);
}