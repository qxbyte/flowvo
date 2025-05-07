package org.xue.assistant.functioncall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.assistant.functioncall.dto.api.ApiResponse;
import org.xue.assistant.functioncall.entity.Customer;
import org.xue.assistant.functioncall.entity.Order;
import org.xue.assistant.functioncall.service.CustomerService;
import org.xue.assistant.functioncall.service.OrderService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomerService customerService;

    /**
     * 分页查询订单列表
     * @param orderNo 订单号（可选）
     * @param customerId 客户ID（可选）
     * @param status 订单状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 订单分页列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId, // 保留customerId参数用于查询
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Order> orderPage = orderService.findOrders(orderNo, customerId, status, startTime, endTime, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderPage.getContent());
        response.put("totalElements", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("currentPage", orderPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增订单
     * @param order 订单信息
     * @return 保存后的订单信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        // 验证客户是否存在
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;

        if (customerId == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "客户ID不能为空", null));
        }

        Optional<Order> orderNew = customerService.findById(customerId)
            .map(customer -> {
                order.setCustomer(customer); // 设置完整的Customer对象
                order.setCreateTime(LocalDateTime.now());
                if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
                    order.setOrderNo(orderService.generateOrderNo());
                }
                return orderService.saveOrder(order);
            });
        return ResponseEntity.ok(new ApiResponse<>(200, "订单创建成功", orderNew.get()));
    }

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 更新后的订单信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除订单
     * @param id 订单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> {
                    orderService.deleteOrder(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}