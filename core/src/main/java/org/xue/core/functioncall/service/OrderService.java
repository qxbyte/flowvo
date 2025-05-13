package org.xue.core.functioncall.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xue.core.functioncall.entity.Order;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 订单管理服务接口
 */
public interface OrderService {
    
    /**
     * 保存订单信息
     * @param order 订单信息
     * @return 保存后的订单信息
     */
    Order saveOrder(Order order);
    
    /**
     * 根据ID查询订单信息
     * @param id 订单ID
     * @return 订单信息
     */
    Optional<Order> findById(Long id);
    
    /**
     * 根据条件分页查询订单列表
     * @param orderNo 订单号（可选）
     * @param customerId 客户ID（可选）
     * @param status 订单状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findOrders(String orderNo, Long customerId, String status, 
                          LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 删除订单
     * @param id 订单ID
     */
    void deleteOrder(Long id);
    
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 更新后的订单信息
     */
    Order updateOrderStatus(Long id, String status);
    
    /**
     * 生成订单号
     * @return 订单号
     */
    String generateOrderNo();
}