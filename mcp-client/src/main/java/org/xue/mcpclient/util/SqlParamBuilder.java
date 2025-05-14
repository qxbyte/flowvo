package org.xue.mcpclient.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL参数构建器
 * 用于方便地构建带命名参数的SQL语句和参数
 */
@Getter
public class SqlParamBuilder {

    /**
     * -- GETTER --
     *  获取SQL语句
     *
     * @return SQL语句
     */
    private final String sql;
    /**
     * -- GETTER --
     *  获取参数Map
     *
     * @return 参数Map
     */
    private final Map<String, Object> params;
    
    private SqlParamBuilder(String sql) {
        this.sql = sql;
        this.params = new HashMap<>();
    }
    
    /**
     * 创建一个SQL参数构建器
     * 
     * @param sql SQL语句
     * @return SQL参数构建器
     */
    public static SqlParamBuilder create(String sql) {
        return new SqlParamBuilder(sql);
    }
    
    /**
     * 添加一个参数
     * 
     * @param name 参数名
     * @param value 参数值
     * @return SQL参数构建器
     */
    public SqlParamBuilder param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    /**
     * 创建批量操作所需的参数列表
     * 
     * @param sqlParamBuilders SQL参数构建器列表
     * @return 批量操作参数列表
     */
    public static List<Map<String, Object>> createBatchParams(List<SqlParamBuilder> sqlParamBuilders) {
        List<Map<String, Object>> batchParams = new ArrayList<>();
        
        for (SqlParamBuilder builder : sqlParamBuilders) {
            Map<String, Object> statement = new HashMap<>();
            statement.put("sql", builder.getSql());
            statement.put("params", builder.getParams());
            batchParams.add(statement);
        }
        
        return batchParams;
    }
} 