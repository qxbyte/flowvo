package org.xue.mcpmysql.rpc.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL查询结果对象
 */
@Data
public class SqlQueryResult {
    /**
     * 查询结果行列表，每行是一个Map，key为列名，value为列值
     */
    private List<Map<String, Object>> rows = new ArrayList<>();
} 