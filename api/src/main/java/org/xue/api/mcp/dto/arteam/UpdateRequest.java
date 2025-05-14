package org.xue.api.mcp.dto.arteam;


import lombok.Data;

import java.io.Serializable;

/**
 * INSERT/UPDATE/DELETE 请求
 */
@Data
public class UpdateRequest implements Serializable {
    /** 要执行的 DML 语句 */
    private String sql;
}
