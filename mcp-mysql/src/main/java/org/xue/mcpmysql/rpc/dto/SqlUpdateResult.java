package org.xue.mcpmysql.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL更新结果对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlUpdateResult {
    /**
     * 影响行数
     */
    private int affected;
} 