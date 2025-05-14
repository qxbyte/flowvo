package org.xue.mcpmysql.rpc.dto;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL查询参数对象
 */
@Data
public class SqlQueryParams {
    /**
     * 要执行的SQL，支持命名参数，例如：SELECT * FROM user WHERE id = :id
     */
    private String sql;
    
    /**
     * SQL参数，key为参数名，value为参数值
     */
    private Map<String, Object> params = new HashMap<>();
} 