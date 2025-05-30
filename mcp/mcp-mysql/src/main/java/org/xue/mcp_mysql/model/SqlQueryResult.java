package org.xue.mcp_mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL查询结果模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlQueryResult {
    
    /**
     * 查询状态
     */
    private String status = "success";
    
    /**
     * 结果行数据
     */
    private List<Map<String, Object>> rows = new ArrayList<>();
    
    /**
     * 错误信息（如果有）
     */
    private String error;
    
    /**
     * 结果行数
     */
    private int count;
} 