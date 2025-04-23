package org.xue.aibot.service;

import org.xue.aibot.entity.ChatRecord;
import org.xue.aibot.entity.Messages;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {

    List<ChatRecord> getAllChatRecords();

    List<Messages> getMessagesByChatId(String chatId);

    ChatRecord createNewChatRecord();  // 添加这行，声明方法

    void saveMessage(String chatId, String role, String content);

    void deleteChatRecord(String chatId);  // 添加删除对话记录的方法声明

}

