package org.xue.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Entity
@Table(name = "orders")
@Data
public class Order {
    
    /**
     * 订单ID - UUID主键
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    /**
     * 订单号
     */
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    
    /**
     * 客户名称
     */
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    /**
     * 订单金额
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * 订单状态：pending（待付款）、paid（已付款）、processing（处理中）、
     * shipped（已发货）、completed（已完成）、canceled（已取消）
     */
    @Column(name = "status", nullable = false)
    private String status;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 创建之前自动设置时间
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新之前自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 