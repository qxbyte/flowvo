package org.xue.core.functioncall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.core.functioncall.entity.Customer;
import org.xue.core.functioncall.repository.CustomerRepository;
import org.xue.core.functioncall.repository.OrderRepository;
import org.xue.core.functioncall.service.CustomerService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 客户管理服务实现类
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public Customer saveCustomer(Customer customer) {
        // 新增客户时设置创建时间
        if (customer.getId() == null) {
            customer.setCreateTime(LocalDateTime.now());
            // 初始化订单统计信息
            if (customer.getTotalOrder() == null) {
                customer.setTotalOrder(0);
            }
        }
        // 无论是新增还是更新，都设置更新时间
        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Page<Customer> findCustomers(String name, String contactPerson, String contactPhone, String level, Pageable pageable) {
        return customerRepository.findByConditions(name, contactPerson, contactPhone, level, pageable);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Customer updateCustomerLevel(Long id, String level) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setLevel(level);
            return customerRepository.save(customer);
        }
        throw new RuntimeException("客户不存在，ID: " + id);
    }

    @Override
    @Transactional
    public Customer updateOrderStats(Long customerId, LocalDateTime orderTime) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // 更新最近下单时间
            if (orderTime != null) {
                customer.setLatestOrderTime(orderTime);
            } else {
                // 如果未提供订单时间，则从数据库查询最新订单时间
                LocalDateTime latestTime = orderRepository.findLatestOrderTimeByCustomerId(customerId);
                if (latestTime != null) {
                    customer.setLatestOrderTime(latestTime);
                }
            }
            // 更新订单总数
            long orderCount = orderRepository.countByCustomerId(customerId);
            customer.setTotalOrder((int) orderCount);
            return customerRepository.save(customer);
        }
        throw new RuntimeException("客户不存在，ID: " + customerId);
    }
}