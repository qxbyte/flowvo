package org.xue.core.chat.service;

import org.springframework.stereotype.Service;
import org.xue.core.chat.entity.ChatRecord;
import org.xue.core.chat.entity.Messages;

import java.time.LocalDateTime;
import java.util.List;

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
     * 根据用户ID和对话类型获取聊天记录
     * @param userId 用户ID
     * @param type 对话类型
     * @return 聊天记录列表
     */
    List<ChatRecord> getChatRecordsByUserIdAndType(String userId, String type);
    
    /**
     * 根据ID获取聊天记录
     * @param id 聊天记录ID
     * @return 聊天记录，如果不存在返回null
     */
    ChatRecord getChatRecordById(String id);
    
    /**
     * 根据ID和用户ID获取聊天记录
     * @param id 聊天记录ID
     * @param userId 用户ID
     * @return 聊天记录，如果不存在或不属于该用户返回null
     */
    ChatRecord getChatRecordByIdAndUserId(String id, String userId);
    
    /**
     * 保存聊天记录
     * @param chatRecord 聊天记录
     * @return 保存后的聊天记录
     */
    ChatRecord saveChatRecord(ChatRecord chatRecord);

    List<Messages> getMessagesByChatId(String chatId);

    ChatRecord createNewChatRecord(String userId);
    ChatRecord createNewChatRecordwithType(String userId, String type);
    
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

