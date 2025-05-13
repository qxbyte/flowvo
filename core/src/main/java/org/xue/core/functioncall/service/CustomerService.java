package org.xue.core.functioncall.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xue.core.functioncall.entity.Customer;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 客户管理服务接口
 */
public interface CustomerService {
    
    /**
     * 保存客户信息
     * @param customer 客户信息
     * @return 保存后的客户信息
     */
    Customer saveCustomer(Customer customer);
    
    /**
     * 根据ID查询客户信息
     * @param id 客户ID
     * @return 客户信息
     */
    Optional<Customer> findById(Long id); // Ensure the customer ID is of type Long
    
    /**
     * 根据条件分页查询客户列表
     * @param name 客户名称（可选）
     * @param contactPerson 联系人（可选）
     * @param contactPhone 联系电话（可选）
     * @param level 客户等级（可选）
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findCustomers(String name, String contactPerson, String contactPhone, String level, Pageable pageable);
    
    /**
     * 删除客户
     * @param id 客户ID
     */
    void deleteCustomer(Long id);
    
    /**
     * 更新客户等级
     * @param id 客户ID
     * @param level 新等级
     * @return 更新后的客户信息
     */
    Customer updateCustomerLevel(Long id, String level);
    
    /**
     * 更新客户订单统计信息
     * @param customerId 客户ID
     * @param orderTime 订单时间
     * @return 更新后的客户信息
     */
    Customer updateOrderStats(Long customerId, LocalDateTime orderTime);
}