package org.xue.assistant.chat.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.assistant.chat.entity.ChatRecord;
import org.xue.assistant.chat.entity.Messages;
import org.xue.assistant.chat.repository.ChatRecordRepository;
import org.xue.assistant.chat.repository.MessageRepository;
import org.xue.assistant.chat.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        return chatRecordRepository.findAllOrderByCreateTime();
    }

    @Override
    public List<Messages> getMessagesByChatId(String chatId) {
        return messageRepository.findByChatIdOrderByCreateTimeAsc(chatId);
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
        message.setChatId(chatId);
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatRecord(String chatId) {
        chatRecordRepository.deleteById(chatId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameChatRecord(String chatId, String newTitle) {
        ChatRecord chatRecord = chatRecordRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat record not found"));
        chatRecord.setTitle(newTitle);
        chatRecord.setUpdateTime(LocalDateTime.now());
        chatRecordRepository.save(chatRecord);
    }
}
