package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    /**
     * 订单ID
     */
    private String id;
    
    /**
     * 订单号
     */
    private String orderNumber;
    
    /**
     * 客户名称
     */
    private String customerName;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 更新时间
     */
    private String updatedAt;
} 