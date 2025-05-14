package org.xue.mcpmysql.arteam.service.impl;

import org.springframework.stereotype.Service;
import org.xue.mcpmysql.arteam.service.RpcDatabaseService;
import org.xue.mcpmysql.rpc.dto.SqlQueryParams;
import org.xue.mcpmysql.rpc.service.DatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple-json-rpc-server实现的数据库RPC服务
 */
@Service
public class RpcDatabaseServiceImpl implements RpcDatabaseService {

    private final DatabaseService databaseService;

    public RpcDatabaseServiceImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Map<String, Object> executeQuery(String sql, Map<String, Object> params) {
        SqlQueryParams queryParams = new SqlQueryParams();
        queryParams.setSql(sql);
        queryParams.setParams(params);
        
        var result = databaseService.executeQuery(queryParams);
        Map<String, Object> response = new HashMap<>();
        response.put("rows", result.getRows());
        response.put("success", true);
        return response;
    }

    @Override
    public Map<String, Object> executeUpdate(String sql, Map<String, Object> params) {
        SqlQueryParams queryParams = new SqlQueryParams();
        queryParams.setSql(sql);
        queryParams.setParams(params);
        
        var result = databaseService.executeUpdate(queryParams);
        Map<String, Object> response = new HashMap<>();
        response.put("affected", result.getAffected());
        response.put("success", true);
        return response;
    }

    @Override
    public Map<String, Object> executeBatch(List<Map<String, Object>> statements) {
        List<SqlQueryParams> paramsList = new ArrayList<>();
        
        for (Map<String, Object> stmt : statements) {
            SqlQueryParams queryParams = new SqlQueryParams();
            queryParams.setSql((String) stmt.get("sql"));
            queryParams.setParams((Map<String, Object>) stmt.get("params"));
            paramsList.add(queryParams);
        }
        
        var result = databaseService.executeBatch(paramsList);
        Map<String, Object> response = new HashMap<>();
        response.put("affected", result.getAffected());
        response.put("success", true);
        return response;
    }

    @Override
    public List<String> listTables() {
        return databaseService.listTables();
    }

    @Override
    public List<Map<String, Object>> getTableSchema(String tableName) {
        return databaseService.getTableSchema(tableName);
    }

    @Override
    public Map<String, Object> getDatabaseMetadata() {
        return databaseService.getDatabaseMetadata();
    }

    @Override
    public List<Map<String, Object>> getQueryMetadata(String sql) {
        return databaseService.getQueryMetadata(sql);
    }
} 