package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 关联的对话ID
     */
    private String conversationId;
    
    /**
     * 消息角色
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 工具调用ID
     */
    private String toolCallId;
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 消息序号
     */
    private Integer sequence;
    
    /**
     * 附件信息
     */
    private String attachments;
    
    /**
     * 创建时间
     */
    private String createdAt;
} 