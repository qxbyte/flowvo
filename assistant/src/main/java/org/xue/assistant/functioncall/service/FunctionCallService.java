package org.xue.assistant.functioncall.service;

import org.xue.assistant.functioncall.entity.CallMessage;

import java.util.List;

public interface FunctionCallService {
    /**
     * 处理用户问题
     * @param question 用户问题文本
     */
    void handleUserQuestion(String question);
    
    /**
     * 获取聊天历史记录
     * @param chatId 聊天ID
     * @return 聊天消息列表
     */
    List<CallMessage> getChatHistory(String chatId);
    
    /**
     * 加载指定聊天历史到当前会话
     * @param chatId 聊天ID
     */
    void loadChatHistory(String chatId);
    
    /**
     * 持久化当前聊天历史到数据库
     */
    void persistChatHistory();
}
