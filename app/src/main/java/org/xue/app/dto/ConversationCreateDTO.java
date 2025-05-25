package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话创建数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateDTO {
    /**
     * 对话名称
     */
    private String title;
    
    /**
     * 服务名称
     */
    private String service;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 对话来源
     * chat: 普通聊天
     * business: 业务系统
     */
    private String source;
    
    /**
     * 初始消息
     */
    private String initialMessage;
    
    /**
     * 用户ID
     */
    private String userId;
} 