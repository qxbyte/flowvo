package org.xue.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.app.dto.*;
import org.xue.app.service.AuthService;
import org.xue.app.service.OrderService;

import java.time.LocalDateTime;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final AuthService authService;

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("用户未登录");
        }

        return authService.getCurrentUserId(authentication.getName());
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        String currentUserId = getCurrentUserId();
        // 强制设置为当前登录用户
        orderCreateDTO.setUserId(currentUserId);
        
        OrderDTO createdOrder = orderService.createOrder(orderCreateDTO);
        return ResponseEntity.ok(createdOrder);
    }
    
    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable String id, @RequestBody OrderUpdateDTO orderUpdateDTO) {
        String currentUserId = getCurrentUserId();
        
        // 首先验证订单是否属于当前用户
        OrderDTO existingOrder = orderService.getOrderById(id);
        if (!currentUserId.equals(existingOrder.getUserId())) {
            throw new RuntimeException("无权访问此订单");
        }
        
        OrderDTO updatedOrder = orderService.updateOrder(id, orderUpdateDTO);
        return ResponseEntity.ok(updatedOrder);
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        
        // 首先验证订单是否属于当前用户
        OrderDTO existingOrder = orderService.getOrderById(id);
        if (!currentUserId.equals(existingOrder.getUserId())) {
            throw new RuntimeException("无权访问此订单");
        }
        
        OrderDTO canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        
        // 首先验证订单是否属于当前用户
        OrderDTO existingOrder = orderService.getOrderById(id);
        if (!currentUserId.equals(existingOrder.getUserId())) {
            throw new RuntimeException("无权访问此订单");
        }
        
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 根据ID获取订单
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String id) {
        String currentUserId = getCurrentUserId();
        OrderDTO order = orderService.getOrderById(id);
        
        // 验证订单是否属于当前用户
        if (!currentUserId.equals(order.getUserId())) {
            throw new RuntimeException("无权访问此订单");
        }
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * 根据订单号获取订单
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        String currentUserId = getCurrentUserId();
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        
        // 验证订单是否属于当前用户
        if (!currentUserId.equals(order.getUserId())) {
            throw new RuntimeException("无权访问此订单");
        }
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * 分页查询订单列表
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<OrderDTO>> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String currentUserId = getCurrentUserId();
        
        // 获取所有订单，然后过滤出属于当前用户的订单
        PageResponseDTO<OrderDTO> allOrders = orderService.getOrderList(
                keyword, orderNumber, customerName, amount, status, startTime, endTime, page, size);
        
        // 过滤出属于当前用户的订单
        allOrders.getItems().removeIf(order -> !currentUserId.equals(order.getUserId()));
        
        return ResponseEntity.ok(allOrders);
    }
} 