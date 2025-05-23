package org.xue.app.service;

import org.xue.app.dto.*;
import org.xue.agent.model.AgentResponse;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 创建对话
     *
     * @param createDTO 对话创建DTO
     * @return 创建后的对话DTO
     */
    ConversationDTO createConversation(ConversationCreateDTO createDTO);

    /**
     * 获取对话详情
     *
     * @param conversationId 对话ID
     * @return 对话DTO
     */
    ConversationDTO getConversation(String conversationId);
    
    /**
     * 更新对话（重命名）
     *
     * @param conversationId 对话ID
     * @param updateDTO 更新参数
     * @return 更新后的对话DTO
     */
    ConversationDTO updateConversation(String conversationId, ConversationUpdateDTO updateDTO);
    
    /**
     * 删除对话
     *
     * @param conversationId 对话ID
     */
    void deleteConversation(String conversationId);
    
    /**
     * 获取对话中的消息列表
     *
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<ChatMessageDTO> getMessages(String conversationId);
    
    /**
     * 获取所有对话列表
     *
     * @return 对话列表
     */
    List<ConversationDTO> getAllConversations();
    
    /**
     * 根据来源获取对话列表
     * 
     * @param source 对话来源，如果为null则获取所有对话
     * @return 对话列表
     */
    List<ConversationDTO> getConversationsBySource(String source);
    
    /**
     * 根据用户ID获取对话列表
     * 
     * @param userId 用户ID
     * @return 对话列表
     */
    List<ConversationDTO> getConversationsByUserId(String userId);
    
    /**
     * 根据来源和用户ID获取对话列表
     * 
     * @param source 对话来源
     * @param userId 用户ID
     * @return 对话列表
     */
    List<ConversationDTO> getConversationsBySourceAndUserId(String source, String userId);
    
    /**
     * 发送消息并获取回复
     *
     * @param requestDTO 聊天请求DTO
     * @return 助手响应
     */
    AgentResponse sendMessage(ChatRequestDTO requestDTO);

} 