package org.xue.mcpmysql.rpc.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.mcpmysql.rpc.dto.SqlQueryParams;
import org.xue.mcpmysql.rpc.dto.SqlQueryResult;
import org.xue.mcpmysql.rpc.dto.SqlUpdateResult;
import org.xue.mcpmysql.rpc.service.DatabaseService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库服务实现
 */
@Service
public class DatabaseServiceImpl implements DatabaseService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DatabaseServiceImpl(NamedParameterJdbcTemplate namedJdbcTemplate, JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public SqlQueryResult executeQuery(SqlQueryParams params) {
        List<Map<String, Object>> rows = namedJdbcTemplate.queryForList(params.getSql(), params.getParams());
        SqlQueryResult result = new SqlQueryResult();
        result.setRows(rows);
        return result;
    }

    @Override
    public SqlUpdateResult executeUpdate(SqlQueryParams params) {
        int affected = namedJdbcTemplate.update(params.getSql(), params.getParams());
        return new SqlUpdateResult(affected);
    }

    @Override
    @Transactional
    public SqlUpdateResult executeBatch(List<SqlQueryParams> paramsList) {
        int totalAffected = 0;
        for (SqlQueryParams params : paramsList) {
            totalAffected += namedJdbcTemplate.update(params.getSql(), params.getParams());
        }
        return new SqlUpdateResult(totalAffected);
    }

    @Override
    public List<String> listTables() {
        return namedJdbcTemplate.queryForList("SHOW TABLES", Map.of())
                .stream()
                .map(row -> row.values().iterator().next().toString())
                .toList();
    }

    @Override
    public List<Map<String, Object>> getTableSchema(String tableName) {
        String sql = "DESCRIBE " + tableName;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> getDatabaseMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMetadata = conn.getMetaData();
            metadata.put("databaseProductName", dbMetadata.getDatabaseProductName());
            metadata.put("databaseProductVersion", dbMetadata.getDatabaseProductVersion());
            metadata.put("driverName", dbMetadata.getDriverName());
            metadata.put("driverVersion", dbMetadata.getDriverVersion());
            metadata.put("url", dbMetadata.getURL());
            metadata.put("username", dbMetadata.getUserName());
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库元数据失败", e);
        }
        return metadata;
    }

    @Override
    public List<Map<String, Object>> getQueryMetadata(String sql) {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        jdbcTemplate.query(
            sql.replace(";", "") + " LIMIT 0", // 不执行查询，只获取元数据
            (ResultSetExtractor<Void>) rs -> {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("name", metaData.getColumnLabel(i));
                    column.put("type", metaData.getColumnTypeName(i));
                    column.put("className", metaData.getColumnClassName(i));
                    column.put("displaySize", metaData.getColumnDisplaySize(i));
                    columns.add(column);
                }
                return null;
            }
        );
        
        return columns;
    }
} 