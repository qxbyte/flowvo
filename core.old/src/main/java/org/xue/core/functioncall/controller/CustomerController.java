package org.xue.core.functioncall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.core.functioncall.entity.Customer;
import org.xue.core.functioncall.service.CustomerService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 分页查询客户列表
     * @param name 客户名称（可选）
     * @param contactPerson 联系人（可选）
     * @param contactPhone 联系电话（可选）
     * @param level 客户等级（可选）
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 客户分页列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCustomerList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Customer> customerPage = customerService.findCustomers(name, contactPerson, contactPhone, level, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", customerPage.getContent());
        response.put("totalElements", customerPage.getTotalElements());
        response.put("totalPages", customerPage.getTotalPages());
        response.put("currentPage", customerPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取客户详情
     * @param id 客户ID
     * @return 客户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增客户
     * @param customer 客户信息
     * @return 保存后的客户信息
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        customer.setCreateTime(LocalDateTime.now());
        if (customer.getTotalOrder() == null) {
            customer.setTotalOrder(0);
        }
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    /**
     * 更新客户
     * @param id 客户ID
     * @param customer 客户信息
     * @return 更新后的客户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.findById(id)
                .map(existingCustomer -> {
                    customer.setId(id);
                    customer.setCreateTime(existingCustomer.getCreateTime());
                    // 保留原有的订单统计信息
                    if (customer.getTotalOrder() == null) {
                        customer.setTotalOrder(existingCustomer.getTotalOrder());
                    }
                    if (customer.getLatestOrderTime() == null) {
                        customer.setLatestOrderTime(existingCustomer.getLatestOrderTime());
                    }
                    Customer updatedCustomer = customerService.saveCustomer(customer);
                    return ResponseEntity.ok(updatedCustomer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新客户等级
     * @param id 客户ID
     * @param level 新等级
     * @return 更新后的客户信息
     */
    @PatchMapping("/{id}/level")
    public ResponseEntity<?> updateCustomerLevel(
            @PathVariable Long id,
            @RequestParam String level) {
        try {
            Customer updatedCustomer = customerService.updateCustomerLevel(id, level);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除客户
     * @param id 客户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> {
                    customerService.deleteCustomer(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}