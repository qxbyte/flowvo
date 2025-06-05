package org.xue.core.chat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.core.chat.entity.ChatRecord;
import org.xue.core.chat.entity.Messages;
import org.xue.core.chat.repository.ChatRecordRepository;
import org.xue.core.chat.repository.MessageRepository;
import org.xue.core.chat.service.ChatService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRecordRepository chatRecordRepository;
    private final MessageRepository messageRepository;

    @Override
    public List<ChatRecord> getAllChatRecords() {
        return chatRecordRepository.findAll();
    }

    @Override
    public List<ChatRecord> getChatRecordsByUserId(String userId) {
        return chatRecordRepository.findByUserIdOrderByUpdateTimeDesc(userId);
    }
    
    @Override
    public List<ChatRecord> getChatRecordsByUserIdAndType(String userId, String type) {
        return chatRecordRepository.findByUserIdAndTypeOrderByUpdateTimeDesc(userId, type);
    }

    @Override
    public ChatRecord getChatRecordById(String id) {
        return chatRecordRepository.findById(id).orElse(null);
    }
    
    @Override
    public ChatRecord getChatRecordByIdAndUserId(String id, String userId) {
        return chatRecordRepository.findByIdAndUserId(id, userId).orElse(null);
    }

    @Override
    public ChatRecord saveChatRecord(ChatRecord chatRecord) {
        return chatRecordRepository.save(chatRecord);
    }

    @Override
    public List<Messages> getMessagesByChatId(String chatId) {
        return messageRepository.findByChatIdOrderByCreateTimeAsc(chatId);
    }

    @Override
    public ChatRecord createNewChatRecord(String userId) {
        ChatRecord newRecord = new ChatRecord();
        newRecord.setId(UUID.randomUUID().toString());
        newRecord.setTitle("新的对话");
        newRecord.setUserId(userId);
        newRecord.setCreateTime(LocalDateTime.now());
        newRecord.setUpdateTime(LocalDateTime.now());
        return chatRecordRepository.save(newRecord);
    }

    @Override
    public ChatRecord createNewChatRecordwithType(String userId, String type) {
        ChatRecord newRecord = new ChatRecord();
        newRecord.setId(UUID.randomUUID().toString());
        newRecord.setTitle("新的对话");
        newRecord.setUserId(userId);
        newRecord.setType(type);
        newRecord.setCreateTime(LocalDateTime.now());
        newRecord.setUpdateTime(LocalDateTime.now());
        return chatRecordRepository.save(newRecord);
    }
    
    @Override
    public ChatRecord getOrCreateChatRecordByType(String userId, String type) {
        // 先查找用户已有的该类型对话
        Optional<ChatRecord> existingChat = chatRecordRepository.findFirstByUserIdAndTypeOrderByUpdateTimeDesc(userId, type);
        
        if (existingChat.isPresent()) {
            // 如果存在，更新时间并返回
            ChatRecord chatRecord = existingChat.get();
            chatRecord.setUpdateTime(LocalDateTime.now());
            return chatRecordRepository.save(chatRecord);
        } else {
            // 如果不存在，创建新的
            ChatRecord newRecord = new ChatRecord();
            newRecord.setId(UUID.randomUUID().toString());
            newRecord.setUserId(userId);
            newRecord.setType(type);
            newRecord.setTitle("AI对话");
            newRecord.setCreateTime(LocalDateTime.now());
            newRecord.setUpdateTime(LocalDateTime.now());
            return chatRecordRepository.save(newRecord);
        }
    }

    @Override
    public void saveMessage(String chatId, String role, String content) {
        saveMessage(chatId, role, content, LocalDateTime.now());
    }

    @Override
    public void saveMessage(String chatId, String role, String content, LocalDateTime dateTime) {
        Messages message = new Messages();
        message.setId(UUID.randomUUID().toString());
        message.setChatId(chatId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(dateTime);
        messageRepository.save(message);
        
        // 更新聊天记录的更新时间
        Optional<ChatRecord> chatRecord = chatRecordRepository.findById(chatId);
        chatRecord.ifPresent(record -> {
            record.setUpdateTime(dateTime);
            chatRecordRepository.save(record);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChatRecord(String chatId) {
        // 先删除聊天记录的所有消息
        messageRepository.deleteByChatId(chatId);
        // 再删除聊天记录
        chatRecordRepository.deleteById(chatId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameChatRecord(String chatId, String newTitle) {
        Optional<ChatRecord> chatRecord = chatRecordRepository.findById(chatId);
        chatRecord.ifPresent(record -> {
            record.setTitle(newTitle);
            record.setUpdateTime(LocalDateTime.now());
            chatRecordRepository.save(record);
        });
    }
}
