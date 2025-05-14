package org.xue.mcpmysql.rpc.service;

import org.xue.mcpmysql.rpc.dto.SqlQueryParams;
import org.xue.mcpmysql.rpc.dto.SqlQueryResult;
import org.xue.mcpmysql.rpc.dto.SqlUpdateResult;

import java.util.List;
import java.util.Map;

/**
 * 数据库服务接口
 */
public interface DatabaseService {
    /**
     * 执行SQL查询
     *
     * @param params SQL查询参数
     * @return 查询结果
     */
    SqlQueryResult executeQuery(SqlQueryParams params);

    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     *
     * @param params SQL查询参数
     * @return 更新结果
     */
    SqlUpdateResult executeUpdate(SqlQueryParams params);

    /**
     * 批量执行SQL语句（在事务中）
     *
     * @param paramsList SQL参数列表
     * @return 更新结果
     */
    SqlUpdateResult executeBatch(List<SqlQueryParams> paramsList);

    /**
     * 获取所有表名
     *
     * @return 表名列表
     */
    List<String> listTables();

    /**
     * 获取表结构
     *
     * @param tableName 表名
     * @return 表结构信息
     */
    List<Map<String, Object>> getTableSchema(String tableName);

    /**
     * 获取数据库元数据
     *
     * @return 数据库元数据
     */
    Map<String, Object> getDatabaseMetadata();

    /**
     * 获取查询元数据
     *
     * @param sql SQL语句
     * @return 查询元数据
     */
    List<Map<String, Object>> getQueryMetadata(String sql);
} 