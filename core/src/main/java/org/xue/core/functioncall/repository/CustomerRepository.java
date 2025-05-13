package org.xue.core.functioncall.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.xue.core.functioncall.entity.Customer;

import java.time.LocalDateTime;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * 根据客户名称模糊查询客户列表
     * @param name 客户名称
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 根据联系人模糊查询客户列表
     * @param contactPerson 联系人
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByContactPersonContaining(String contactPerson, Pageable pageable);
    
    /**
     * 根据联系电话模糊查询客户列表
     * @param contactPhone 联系电话
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByContactPhoneContaining(String contactPhone, Pageable pageable);
    
    /**
     * 根据客户等级查询客户列表
     * @param level 客户等级
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByLevel(String level, Pageable pageable);
    
    /**
     * 根据最近下单时间范围查询客户列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByLatestOrderTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 复合查询客户列表
     * @param name 客户名称（可选）
     * @param contactPerson 联系人（可选）
     * @param contactPhone 联系电话（可选）
     * @param level 客户等级（可选）
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    @Query("SELECT c FROM Customer c WHERE (:name IS NULL OR c.name LIKE %:name%) " +
           "AND (:contactPerson IS NULL OR c.contactPerson LIKE %:contactPerson%) " +
           "AND (:contactPhone IS NULL OR c.contactPhone LIKE %:contactPhone%) " +
           "AND (:level IS NULL OR c.level = :level)")
    Page<Customer> findByConditions(
            @Param("name") String name,
            @Param("contactPerson") String contactPerson,
            @Param("contactPhone") String contactPhone,
            @Param("level") String level,
            Pageable pageable);
}