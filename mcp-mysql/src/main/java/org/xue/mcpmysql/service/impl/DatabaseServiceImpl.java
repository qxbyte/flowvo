package org.xue.mcpmysql.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.mcpmysql.model.SqlQueryResult;
import org.xue.mcpmysql.model.SqlUpdateResult;
import org.xue.mcpmysql.service.DatabaseService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 数据库服务实现类
 */
@Service
public class DatabaseServiceImpl implements DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;
    
    @Autowired
    public DatabaseServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public SqlQueryResult executeQuery(String sql, Map<String, Object> params) {
        logger.debug("执行查询: {}, 参数: {}", sql, params);
        SqlQueryResult result = new SqlQueryResult();
        
        try {
            MapSqlParameterSource paramSource = new MapSqlParameterSource(params);
            List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, paramSource);
            result.setRows(rows);
            result.setCount(rows.size());
            result.setStatus("success");
        } catch (DataAccessException e) {
            logger.error("查询执行失败: {}", e.getMessage(), e);
            result.setStatus("error");
            result.setError(e.getMessage());
            result.setRows(Collections.emptyList());
        }
        
        return result;
    }

    @Override
    public SqlUpdateResult executeUpdate(String sql, Map<String, Object> params) {
        logger.debug("执行更新: {}, 参数: {}", sql, params);
        SqlUpdateResult result = new SqlUpdateResult();
        
        try {
            MapSqlParameterSource paramSource = new MapSqlParameterSource(params);
            int affected = namedParameterJdbcTemplate.update(sql, paramSource);
            result.setAffected(affected);
            result.setStatus("success");
        } catch (DataAccessException e) {
            logger.error("更新执行失败: {}", e.getMessage(), e);
            result.setStatus("error");
            result.setError(e.getMessage());
            result.setAffected(0);
        }
        
        return result;
    }

    @Override
    @Transactional
    public SqlUpdateResult executeBatch(List<Map<String, Object>> statements) {
        logger.debug("执行批处理, 语句数量: {}", statements.size());
        SqlUpdateResult result = new SqlUpdateResult();
        int totalAffected = 0;
        
        try {
            for (Map<String, Object> stmt : statements) {
                String sql = (String) stmt.get("sql");
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) stmt.getOrDefault("params", Collections.emptyMap());
                
                MapSqlParameterSource paramSource = new MapSqlParameterSource(params);
                int affected = namedParameterJdbcTemplate.update(sql, paramSource);
                totalAffected += affected;
            }
            
            result.setAffected(totalAffected);
            result.setStatus("success");
        } catch (DataAccessException e) {
            logger.error("批处理执行失败: {}", e.getMessage(), e);
            result.setStatus("error");
            result.setError(e.getMessage());
            result.setAffected(0);
        }
        
        return result;
    }

    @Override
    public List<String> listTables() {
        logger.debug("获取所有表名");
        List<String> tables = new ArrayList<>();
        
        try {
            tables = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, databaseMetaData -> {
                List<String> tableNames = new ArrayList<>();
                try (ResultSet rs = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tableNames.add(rs.getString("TABLE_NAME"));
                    }
                    return tableNames;
                }
            });
        } catch (MetaDataAccessException e) {
            logger.error("获取表名失败: {}", e.getMessage(), e);
        }
        
        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableSchema(String tableName) {
        logger.debug("获取表结构: {}", tableName);
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try {
            columns = (List<Map<String, Object>>) JdbcUtils.extractDatabaseMetaData(dataSource, databaseMetaData -> {
                List<Map<String, Object>> tableColumns = new ArrayList<>();
                try (ResultSet rs = databaseMetaData.getColumns(null, null, tableName, "%")) {
                    while (rs.next()) {
                        Map<String, Object> column = new HashMap<>();
                        column.put("name", rs.getString("COLUMN_NAME"));
                        column.put("type", rs.getString("TYPE_NAME"));
                        column.put("size", rs.getInt("COLUMN_SIZE"));
                        column.put("nullable", rs.getBoolean("NULLABLE"));
                        column.put("position", rs.getInt("ORDINAL_POSITION"));
                        column.put("remarks", rs.getString("REMARKS"));
                        tableColumns.add(column);
                    }
                    return tableColumns;
                }
            });
            
            // 获取主键信息
            List<String> primaryKeys = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, databaseMetaData -> {
                List<String> keys = new ArrayList<>();
                try (ResultSet rs = databaseMetaData.getPrimaryKeys(null, null, tableName)) {
                    while (rs.next()) {
                        keys.add(rs.getString("COLUMN_NAME"));
                    }
                    return keys;
                }
            });
            
            // 标记主键列
            for (Map<String, Object> column : columns) {
                column.put("primaryKey", primaryKeys.contains(column.get("name")));
            }
        } catch (MetaDataAccessException e) {
            logger.error("获取表结构失败: {}", e.getMessage(), e);
        }
        
        return columns;
    }

    @Override
    public Map<String, Object> getDatabaseMetadata() {
        logger.debug("获取数据库元数据");
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            metadata = (Map<String, Object>) JdbcUtils.extractDatabaseMetaData(dataSource, databaseMetaData -> {
                Map<String, Object> dbMetadata = new HashMap<>();
                dbMetadata.put("databaseProductName", databaseMetaData.getDatabaseProductName());
                dbMetadata.put("databaseProductVersion", databaseMetaData.getDatabaseProductVersion());
                dbMetadata.put("driverName", databaseMetaData.getDriverName());
                dbMetadata.put("driverVersion", databaseMetaData.getDriverVersion());
                dbMetadata.put("url", databaseMetaData.getURL());
                dbMetadata.put("username", databaseMetaData.getUserName());
                return dbMetadata;
            });
        } catch (MetaDataAccessException e) {
            logger.error("获取数据库元数据失败: {}", e.getMessage(), e);
            metadata.put("error", e.getMessage());
        }
        
        return metadata;
    }

    @Override
    public List<Map<String, Object>> getQueryMetadata(String sql) {
        logger.debug("获取查询元数据: {}", sql);
        List<Map<String, Object>> metadata = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSetMetaData rsMetaData = stmt.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", rsMetaData.getColumnName(i));
                column.put("label", rsMetaData.getColumnLabel(i));
                column.put("type", rsMetaData.getColumnTypeName(i));
                column.put("className", rsMetaData.getColumnClassName(i));
                column.put("precision", rsMetaData.getPrecision(i));
                column.put("scale", rsMetaData.getScale(i));
                metadata.add(column);
            }
        } catch (SQLException e) {
            logger.error("获取查询元数据失败: {}", e.getMessage(), e);
        }
        
        return metadata;
    }

    @Override
    public Map<String, Object> heartbeat() {
        logger.debug("执行心跳检查");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 执行一个简单的SQL查询以验证数据库连接
            long timestamp = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            result.put("status", "ok");
            result.put("timestamp", timestamp);
            result.put("responseTime", System.currentTimeMillis() - timestamp);
            
        } catch (DataAccessException e) {
            logger.error("心跳检查失败: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }
} 