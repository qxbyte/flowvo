package org.xue.mcp_client.sql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xue.mcp_client.util.SqlParamBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 示例服务
 * 演示如何使用MCP数据库服务
 */
@Service
public class ExampleService {

    private final MCPDatabaseServiceWrapper mcpDb;
    
    @Autowired
    public ExampleService(MCPDatabaseServiceWrapper mcpDatabaseServiceWrapper) {
        this.mcpDb = mcpDatabaseServiceWrapper;
    }
    
    /**
     * 获取用户列表
     * 
     * @param minAge 最小年龄
     * @return 用户列表
     */
    public List<Map<String, Object>> getUsersByAge(int minAge) {
        // 使用SqlParamBuilder构建带参数的SQL
        SqlParamBuilder builder = SqlParamBuilder.create(
                "SELECT * FROM users WHERE age >= :minAge ORDER BY age");
        builder.param("minAge", minAge);
        
        // 执行查询
        return mcpDb.query(builder.getSql(), builder.getParams());
    }
    
    /**
     * 创建新用户
     * 
     * @param username 用户名
     * @param age 年龄
     * @return 是否成功
     */
    public boolean createUser(String username, int age) {
        SqlParamBuilder builder = SqlParamBuilder.create(
                "INSERT INTO users (username, age) VALUES (:username, :age)");
        builder.param("username", username)
               .param("age", age);
        
        int affected = mcpDb.update(builder.getSql(), builder.getParams());
        return affected > 0;
    }
    
    /**
     * 批量创建用户
     * 
     * @param users 用户列表 (username, age)
     * @return 创建的用户数
     */
    public int createUsers(List<Map<String, Object>> users) {
        List<SqlParamBuilder> builders = new ArrayList<>();
        
        for (Map<String, Object> user : users) {
            SqlParamBuilder builder = SqlParamBuilder.create(
                    "INSERT INTO users (username, age) VALUES (:username, :age)");
            builder.param("username", user.get("username"))
                   .param("age", user.get("age"));
            builders.add(builder);
        }
        
        // 执行批量操作
        return mcpDb.executeBatch(SqlParamBuilder.createBatchParams(builders));
    }
    
    /**
     * 获取所有表名
     * 
     * @return 表名列表
     */
    public List<String> getAllTables() {
        return mcpDb.listTables();
    }
    
    /**
     * 获取用户表结构
     * 
     * @return 用户表结构
     */
    public List<Map<String, Object>> getUserTableSchema() {
        return mcpDb.getTableSchema("users");
    }
    
    /**
     * 获取数据库元数据
     * 
     * @return 数据库元数据
     */
    public Map<String, Object> getDatabaseInfo() {
        return mcpDb.getDatabaseMetadata();
    }
} 