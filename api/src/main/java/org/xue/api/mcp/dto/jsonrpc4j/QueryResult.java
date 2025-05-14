package org.xue.api.mcp.dto.jsonrpc4j;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResult {
    /** 返回的所有行，每行为 Map<列名, 列值> */
    private List<Map<String, Object>> rows;
    // getters/setters
}
