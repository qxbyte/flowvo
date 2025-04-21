package org.xue.aibot.service;

import org.xue.aibot.entity.ChatRecord;
import org.xue.aibot.entity.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {

    List<ChatRecord> getAllChatRecords();

    List<Message> getMessagesByChatId(Long chatId);

    ChatRecord createNewChatRecord();  // 添加这行，声明方法

    void saveMessage(Long chatId, String sender, String content);

    void deleteChatRecord(Long chatId);  // 添加删除对话记录的方法声明

}

