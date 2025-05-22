package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 温度参数（可选）
     */
    private Double temperature;
} 