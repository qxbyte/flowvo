package org.xue.app.service;

import org.xue.app.dto.OrderCreateDTO;
import org.xue.app.dto.OrderDTO;
import org.xue.app.dto.OrderUpdateDTO;
import org.xue.app.dto.PageResponseDTO;

import java.time.LocalDateTime;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     *
     * @param orderCreateDTO 订单创建DTO
     * @return 创建后的订单DTO
     */
    OrderDTO createOrder(OrderCreateDTO orderCreateDTO);
    
    /**
     * 更新订单
     *
     * @param id 订单ID
     * @param orderUpdateDTO 订单更新DTO
     * @return 更新后的订单DTO
     */
    OrderDTO updateOrder(String id, OrderUpdateDTO orderUpdateDTO);
    
    /**
     * 取消订单
     *
     * @param id 订单ID
     * @return 取消后的订单DTO
     */
    OrderDTO cancelOrder(String id);
    
    /**
     * 删除订单
     *
     * @param id 订单ID
     */
    void deleteOrder(String id);
    
    /**
     * 根据ID获取订单
     *
     * @param id 订单ID
     * @return 订单DTO
     */
    OrderDTO getOrderById(String id);
    
    /**
     * 根据订单号获取订单
     *
     * @param orderNumber 订单号
     * @return 订单DTO
     */
    OrderDTO getOrderByOrderNumber(String orderNumber);
    
    /**
     * 分页查询订单列表
     *
     * @param keyword 搜索关键字（订单号或客户名称）
     * @param orderNumber 订单号
     * @param customerName 客户名称
     * @param amount 金额
     * @param status 订单状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页订单DTO列表
     */
    PageResponseDTO<OrderDTO> getOrderList(String keyword, String orderNumber, String customerName, 
                                          String amount, String status, 
                                          LocalDateTime startTime, LocalDateTime endTime, 
                                          int page, int size);
} 