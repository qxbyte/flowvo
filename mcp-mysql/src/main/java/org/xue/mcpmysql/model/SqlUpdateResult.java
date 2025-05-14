package org.xue.mcpmysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL更新结果模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlUpdateResult {
    
    /**
     * 更新状态
     */
    private String status = "success";
    
    /**
     * 影响的行数
     */
    private int affected;
    
    /**
     * 错误信息（如果有）
     */
    private String error;
} 