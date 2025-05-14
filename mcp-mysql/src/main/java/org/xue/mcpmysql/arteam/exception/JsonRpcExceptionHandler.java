package org.xue.mcpmysql.arteam.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON-RPC异常处理器
 */
@ControllerAdvice
public class JsonRpcExceptionHandler {

    /**
     * 构建错误响应
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(int code, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("error", error);
        response.put("id", null);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 处理SQL语法错误
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSqlSyntaxError(SQLSyntaxErrorException e) {
        return buildErrorResponse(-32000, "SQL语法错误: " + e.getMessage());
    }
    
    /**
     * 处理通用SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSqlException(SQLException e) {
        return buildErrorResponse(-32000, "数据库错误: " + e.getMessage());
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        return buildErrorResponse(-32603, "内部错误: " + e.getMessage());
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return buildErrorResponse(-32500, "服务器错误: " + e.getMessage());
    }
} 