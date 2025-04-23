package org.xue.aibot.service.impl;

import org.xue.aibot.entity.ChatRecord;
import org.xue.aibot.entity.Messages;
import org.xue.aibot.repository.ChatRecordRepository;
import org.xue.aibot.repository.MessageRepository;
import org.xue.aibot.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRecordRepository chatRecordRepository;
    private final MessageRepository messageRepository;

    public ChatServiceImpl(ChatRecordRepository chatRecordRepository, MessageRepository messageRepository) {
        this.chatRecordRepository = chatRecordRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public List<ChatRecord> getAllChatRecords() {
        return chatRecordRepository.findAll();
    }

    @Override
    public List<Messages> getMessagesByChatId(String chatId) {
        return messageRepository.findByChatRecordIdOrderByCreateTimeAsc(chatId);
    }

    @Override
    public ChatRecord createNewChatRecord() {
        ChatRecord newRecord = new ChatRecord();
        newRecord.setId(UUID.randomUUID().toString());
        newRecord.setTitle("新的对话");
        newRecord.setCreateTime(LocalDateTime.now());
        newRecord.setUpdateTime(LocalDateTime.now());
        return chatRecordRepository.save(newRecord);
    }

    @Override
    public void saveMessage(String chatId, String role, String content) {
        ChatRecord chatRecord = chatRecordRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat record not found"));

        Messages message = new Messages();
        message.setId(UUID.randomUUID().toString());
        message.setChatRecord(chatRecord);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        
        messageRepository.save(message);

        // 如果是第一条用户消息，更新对话标题
        if (role.equals("user")) {
            Optional<Messages> firstMessage = messageRepository.findFirstUserMessage(chatId);
            if (firstMessage.isEmpty()) {
                String title = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                chatRecord.setTitle(title);
                chatRecord.setUpdateTime(LocalDateTime.now());
                chatRecordRepository.save(chatRecord);
            }
        }
    }

    @Override
    @Transactional
    public void deleteChatRecord(String chatId) {
        chatRecordRepository.deleteById(chatId);
    }
}
