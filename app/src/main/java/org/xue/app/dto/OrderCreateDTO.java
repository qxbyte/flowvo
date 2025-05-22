package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单创建数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {
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
} 