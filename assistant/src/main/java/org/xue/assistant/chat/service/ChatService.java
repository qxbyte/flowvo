package org.xue.assistant.chat.service;

import org.springframework.stereotype.Service;
import org.xue.assistant.chat.entity.ChatRecord;
import org.xue.assistant.chat.entity.Messages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface ChatService {

    List<ChatRecord> getAllChatRecords();
    
    /**
     * 获取指定用户的所有聊天记录
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    List<ChatRecord> getChatRecordsByUserId(String userId);
    
    /**
     * 根据ID获取聊天记录
     * @param id 聊天记录ID
     * @return 聊天记录，如果不存在返回null
     */
    ChatRecord getChatRecordById(String id);
    
    /**
     * 保存聊天记录
     * @param chatRecord 聊天记录
     * @return 保存后的聊天记录
     */
    ChatRecord saveChatRecord(ChatRecord chatRecord);

    List<Messages> getMessagesByChatId(String chatId);

    ChatRecord createNewChatRecord();
    
    /**
     * 创建或获取指定类型的聊天记录
     * @param userId 用户ID
     * @param type 聊天类型
     * @return 聊天记录
     */
    ChatRecord getOrCreateChatRecordByType(String userId, String type);

    void saveMessage(String chatId, String role, String content);

    void saveMessage(String chatId, String role, String content, LocalDateTime dateTime);

    void deleteChatRecord(String chatId);

    void renameChatRecord(String chatId, String newTitle);
}

