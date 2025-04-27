package org.xue.chat.service;

import org.springframework.stereotype.Service;
import org.xue.chat.entity.ChatRecord;
import org.xue.chat.entity.Messages;

import java.util.List;

@Service
public interface ChatService {

    List<ChatRecord> getAllChatRecords();

    List<Messages> getMessagesByChatId(String chatId);

    ChatRecord createNewChatRecord();  // 添加这行，声明方法

    void saveMessage(String chatId, String role, String content);

    void deleteChatRecord(String chatId);  // 添加删除对话记录的方法声明

    void renameChatRecord(String chatId, String newTitle);  // 添加重命名对话记录的方法声明
}

