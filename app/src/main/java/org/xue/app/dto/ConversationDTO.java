package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    /**
     * 对话ID
     */
    private String id;
    
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
     * 用户ID
     */
    private String userId;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 更新时间
     */
    private String updatedAt;
    
    /**
     * 最后一条消息（可能是用户或助手）
     */
    private String lastMessage;
} 