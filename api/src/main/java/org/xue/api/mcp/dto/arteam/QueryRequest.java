package org.xue.api.mcp.dto.arteam;

import lombok.Data;

import java.io.Serializable;

/**
 * SELECT 请求
 */
@Data
public class QueryRequest implements Serializable {
    /** 要执行的 SELECT 语句 */
    private String sql;
}
