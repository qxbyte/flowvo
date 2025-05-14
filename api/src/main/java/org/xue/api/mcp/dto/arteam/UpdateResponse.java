package org.xue.api.mcp.dto.arteam;

import lombok.Data;

import java.io.Serializable;

/**
 * DML 返回结果
 */
@Data
public class UpdateResponse implements Serializable {
    /** 受影响的行数 */
    private long affectedRows;
}
