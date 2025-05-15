package org.xue.mcp_mysql.enums;

/**
 * API格式类型枚举
 * 用于标识不同的API格式输出类型
 */
public enum ApiFormatType {
    /**
     * 用于大模型Function Calling的API格式
     */
    FUNCTION_CALLING,
    
    /**
     * 标准JSON-RPC的API格式
     */
    RPC_JSON
} 