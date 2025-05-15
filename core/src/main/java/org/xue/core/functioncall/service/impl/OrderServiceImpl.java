package org.xue.core.functioncall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.core.functioncall.entity.Order;
import org.xue.core.functioncall.repository.OrderRepository;
import org.xue.core.functioncall.service.CustomerService;
import org.xue.core.functioncall.service.OrderService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

/**
 * 订单管理服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerService customerService;

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        // 新增订单时设置创建时间和订单号
        if (order.getId() == null) {
            order.setCreateTime(LocalDateTime.now());
            if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
                order.setOrderNo(generateOrderNo());
            }
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // 更新客户的订单统计信息
        if (savedOrder.getCustomer() != null) {
            customerService.updateOrderStats(savedOrder.getCustomer().getId(), savedOrder.getCreateTime());
        }
        
        return savedOrder;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    @Override
    public Page<Order> findOrders(String orderNo, Long customerId, String status,
                                 LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderRepository.findByConditions(orderNo, customerId, status, startTime, endTime, pageable);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
            
            // 删除订单
            orderRepository.deleteById(id);
            
            // 更新客户的订单统计信息
            if (customerId != null) {
                customerService.updateOrderStats(customerId, null);
            }
        }
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("订单不存在，ID: " + id);
    }
    
    @Override
    @Transactional
    public Order updateOrderStatusByOrderNo(String orderNo, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("订单不存在，订单号: " + orderNo);
    }

    @Override
    public String generateOrderNo() {
        // 生成订单号：ORD + 年月日 + 6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDateTime.now().format(formatter);
        
        // 生成6位随机数
        Random random = new Random();
        int randomNum = 100000 + random.nextInt(900000); // 生成100000-999999之间的随机数
        
        return "ORD" + dateStr + randomNum;
    }
}