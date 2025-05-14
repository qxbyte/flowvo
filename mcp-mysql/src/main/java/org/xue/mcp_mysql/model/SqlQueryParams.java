package org.xue.mcp_mysql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL查询参数模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlQueryParams {
    
    /**
     * SQL查询语句
     */
    private String sql;
    
    /**
     * 查询参数
     */
    private Map<String, Object> params = new HashMap<>();
} 