package org.xue.app.agents.client.exception;

/**
 * MCP客户端异常
 */
public class McpClientException extends RuntimeException {
    
    public McpClientException(String message) {
        super(message);
    }
    
    public McpClientException(String message, Throwable cause) {
        super(message, cause);
    }
} 