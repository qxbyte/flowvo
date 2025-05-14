package org.xue.mcpmysql.arteam.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * JsonRpc响应模型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private Object error;
    private Object id;
    
    public static JsonRpcResponse success(Object result, Object id) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setResult(result);
        response.setId(id);
        return response;
    }
    
    public static JsonRpcResponse error(int code, String message, Object id) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setError(new JsonRpcError(code, message));
        response.setId(id);
        return response;
    }
    
    /**
     * JSON-RPC错误对象
     */
    @Data
    public static class JsonRpcError {
        private int code;
        private String message;
        
        public JsonRpcError(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
} 