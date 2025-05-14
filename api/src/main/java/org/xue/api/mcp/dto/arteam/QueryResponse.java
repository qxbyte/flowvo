package org.xue.api.mcp.dto.arteam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SELECT 返回结果
 */
@Data
public class QueryResponse implements Serializable {
    /** 每一行记录，以列名→值 的 Map 形式 */
    private List<Map<String, Object>> rows;
}
