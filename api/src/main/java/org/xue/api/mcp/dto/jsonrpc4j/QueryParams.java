package org.xue.api.mcp.dto.jsonrpc4j;

import lombok.Data;

import java.util.Map;

@Data
public class QueryParams {
    /** 要执行的 SQL，支持带命名参数，例如 “SELECT * FROM user WHERE id = :id” */
    private String sql;
    /** 命名参数映射：key 即 SQL 中的 “:xxx”，value 为参数值 */
    private Map<String, Object> params;
    // getters/setters
}
