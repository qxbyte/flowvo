package org.xue.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.app.dto.OrderCreateDTO;
import org.xue.app.dto.OrderDTO;
import org.xue.app.dto.OrderUpdateDTO;
import org.xue.app.dto.PageResponseDTO;
import org.xue.app.entity.Order;
import org.xue.app.repository.OrderRepository;
import org.xue.app.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 生成订单号
     * 
     * @return 订单号
     */
    private String generateOrderNumber() {
        // 格式：ORD + 当前年月日 + 6位随机数
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = (int) (Math.random() * 900000) + 100000;
        return "ORD" + datePrefix + randomNum;
    }
    
    /**
     * 实体转DTO
     */
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt().format(FORMATTER))
                .updatedAt(order.getUpdatedAt().format(FORMATTER))
                .build();
    }
    
    @Override
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO orderCreateDTO) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(orderCreateDTO.getCustomerName());
        order.setAmount(orderCreateDTO.getAmount());
        order.setStatus(orderCreateDTO.getStatus());
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderDTO updateOrder(String id, OrderUpdateDTO orderUpdateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在，ID: " + id));
        
        order.setAmount(orderUpdateDTO.getAmount());
        order.setStatus(orderUpdateDTO.getStatus());
        
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderDTO cancelOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在，ID: " + id));
        
        // 只有待付款状态的订单才能取消
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只有待付款状态的订单才能取消");
        }
        
        order.setStatus("canceled");
        Order canceledOrder = orderRepository.save(order);
        return convertToDTO(canceledOrder);
    }
    
    @Override
    public OrderDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在，ID: " + id));
        
        return convertToDTO(order);
    }
    
    @Override
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("订单不存在，订单号: " + orderNumber));
        
        return convertToDTO(order);
    }
    
    @Override
    @Transactional
    public void deleteOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在，ID: " + id));
        
        // 删除订单
        orderRepository.delete(order);
    }
    
    @Override
    public PageResponseDTO<OrderDTO> getOrderList(
            String keyword, String orderNumber, String customerName, 
            String amount, String status, LocalDateTime startTime, 
            LocalDateTime endTime, int page, int size) {
        
        // 构建查询条件
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键字搜索（模糊查询订单号或客户名）
            if (keyword != null && !keyword.isEmpty()) {
                Predicate orderNumberPredicate = cb.like(root.get("orderNumber"), "%" + keyword + "%");
                Predicate customerNamePredicate = cb.like(root.get("customerName"), "%" + keyword + "%");
                predicates.add(cb.or(orderNumberPredicate, customerNamePredicate));
            }
            
            // 订单号精确搜索
            if (orderNumber != null && !orderNumber.isEmpty()) {
                predicates.add(cb.like(root.get("orderNumber"), "%" + orderNumber + "%"));
            }
            
            // 客户名称搜索
            if (customerName != null && !customerName.isEmpty()) {
                predicates.add(cb.like(root.get("customerName"), "%" + customerName + "%"));
            }
            
            // 金额搜索
            if (amount != null && !amount.isEmpty()) {
                try {
                    BigDecimal amountValue = new BigDecimal(amount);
                    predicates.add(cb.equal(root.get("amount"), amountValue));
                } catch (NumberFormatException e) {
                    // 如果金额格式不正确，忽略此条件
                }
            }
            
            // 状态筛选
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            // 时间范围筛选
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 创建分页和排序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 执行查询
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        // 转换为DTO
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        PageResponseDTO<OrderDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setItems(orderDTOs);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotal(orderPage.getTotalElements());
        pageResponse.setTotalPages(orderPage.getTotalPages());
        pageResponse.setFirst(orderPage.isFirst());
        pageResponse.setLast(orderPage.isLast());
        
        return pageResponse;
    }
} 