package org.xue.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.app.dto.OrderCreateDTO;
import org.xue.app.dto.OrderDTO;
import org.xue.app.dto.OrderUpdateDTO;
import org.xue.app.dto.PageResponseDTO;
import org.xue.app.service.OrderService;

import java.time.LocalDateTime;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderCreateDTO);
        return ResponseEntity.ok(createdOrder);
    }
    
    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable String id, @RequestBody OrderUpdateDTO orderUpdateDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(id, orderUpdateDTO);
        return ResponseEntity.ok(updatedOrder);
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable String id) {
        OrderDTO canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 根据ID获取订单
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * 根据订单号获取订单
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
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
        
        PageResponseDTO<OrderDTO> orderList = orderService.getOrderList(
                keyword, orderNumber, customerName, amount, status, startTime, endTime, page, size);
        return ResponseEntity.ok(orderList);
    }
} 