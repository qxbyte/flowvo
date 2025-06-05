package org.xue.core.functioncall.service;

import org.xue.core.functioncall.entity.CallMessage;
import reactor.core.publisher.Flux;

import java.util.List;

public interface FunctionCallService {
    /**
     * 处理用户问题
     * @param question 用户问题文本
     */
    void handleUserQuestion(String question);
    
    /**
     * 处理用户问题流式处理
     * @param question 用户问题文本
     * @param chatId 聊天ID
     * @return 流式处理结果
     */
    Flux<String> handleUserQuestionStream(String question, String chatId);
    
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
