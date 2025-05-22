package org.xue.milvus.exception;

/**
 * MCP服务端异常
 */
public class McpServerException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final int errorCode;
    
    public McpServerException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public McpServerException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getErrorCode() {
        return errorCode;
    }
} 