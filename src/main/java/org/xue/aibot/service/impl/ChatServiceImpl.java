package org.xue.aibot.service.impl;

import org.xue.aibot.entity.ChatRecord;
import org.xue.aibot.entity.Message;
import org.xue.aibot.repository.ChatRecordRepository;
import org.xue.aibot.repository.MessageRepository;
import org.xue.aibot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRecordRepository chatRecordRepository;

    @Autowired
    private MessageRepository messageRepository;

    // 获取所有聊天记录
    @Override
    public List<ChatRecord> getAllChatRecords() {
        return chatRecordRepository.findAll();
    }

    // 获取某个聊天记录的消息
    @Override
    public List<Message> getMessagesByChatId(Long chatId) {
        return messageRepository.findByChatId(chatId);
    }

    // 创建新的聊天记录
    @Override
    public ChatRecord createNewChatRecord() {
        ChatRecord newRecord = new ChatRecord();
        newRecord.setName("新的对话 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        newRecord.setCreatedAt(LocalTime.now());
        return chatRecordRepository.save(newRecord);
    }

    // 保存消息
    @Override
    public void saveMessage(Long chatId, String sender, String content) {
        if (chatId == null) {
            ChatRecord newRecord = new ChatRecord();
            newRecord.setName("新的对话 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            newRecord = chatRecordRepository.save(newRecord);
            chatId = newRecord.getId();
        }

        Message message = new Message(chatId, sender, content);
        message.setCreatedAt(LocalTime.now());
        messageRepository.save(message);
    }

    // 删除聊天记录
    @Override
    @Transactional
    public void deleteChatRecord(Long chatId) {
        messageRepository.deleteByChatId(chatId);
        chatRecordRepository.deleteById(chatId);
    }
}
