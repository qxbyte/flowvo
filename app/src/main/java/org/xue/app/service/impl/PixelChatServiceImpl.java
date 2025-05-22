package org.xue.app.service.impl;

import org.xue.app.entity.ChatMessage;
import org.xue.app.entity.Conversation;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;
import org.xue.app.repository.ChatMessageRepository;
import org.xue.app.repository.ConversationRepository;
import org.xue.app.service.ChatService;
import org.xue.app.service.PixelChatService;
import org.xue.agent.model.AgentResponse; // As specified in the prompt

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PixelChatServiceImpl implements PixelChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService; // Existing chat service

    @Autowired
    public PixelChatServiceImpl(ConversationRepository conversationRepository,
                                ChatMessageRepository chatMessageRepository,
                                ChatService chatService) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatService = chatService;
    }

    @Override
    @Transactional
    public ConversationDTO createPixelConversation(ConversationCreateDTO createDTO) {
        log.info("Creating new PixelChat conversation for user: {}", createDTO.getUserId());
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setUserId(createDTO.getUserId());
        conversation.setTitle(createDTO.getTitle());
        conversation.setSource("pixel_chat"); // Specific source for PixelChat
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("PixelChat conversation created with ID: {}", savedConversation.getId());
        return mapToConversationDTO(savedConversation);
    }

    @Override
    public ConversationDTO getPixelConversation(String conversationId) {
        log.info("Fetching PixelChat conversation with ID: {}", conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("PixelChat conversation not found with ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });
        return mapToConversationDTO(conversation);
    }

    @Override
    @Transactional
    public ConversationDTO updatePixelConversationTitle(String conversationId, ConversationUpdateDTO updateDTO) {
        log.info("Updating title for PixelChat conversation ID: {}", conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("PixelChat conversation not found for update with ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });

        conversation.setTitle(updateDTO.getTitle());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation updatedConversation = conversationRepository.save(conversation);
        log.info("PixelChat conversation title updated for ID: {}", updatedConversation.getId());
        return mapToConversationDTO(updatedConversation);
    }

    @Override
    @Transactional
    public void deletePixelConversation(String conversationId) {
        log.info("Deleting PixelChat conversation with ID: {}", conversationId);
        if (!conversationRepository.existsById(conversationId)) {
            log.warn("Attempted to delete non-existing PixelChat conversation with ID: {}", conversationId);
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId + " for deletion.");
        }
        // First delete associated messages
        // Assuming ChatMessageRepository has this method:
        // For example: long deletedMessagesCount = chatMessageRepository.deleteByConversationId(conversationId);
        // log.info("Deleted {} messages for conversation ID: {}", deletedMessagesCount, conversationId);
        // To avoid compilation errors if the method doesn't exist, I'll simulate its effect or skip direct call
        // For now, let's assume it exists or will be added to the repository.
        // If it's a custom query, it might look like:
        // chatMessageRepository.deleteAll(chatMessageRepository.findByConversationId(conversationId));
        // For the purpose of this task, I will trust the requirement that `chatMessageRepository.deleteByConversationId` exists.
        chatMessageRepository.deleteByConversationId(conversationId); // As per requirement
        log.info("Associated messages deleted for conversation ID: {}", conversationId);

        conversationRepository.deleteById(conversationId);
        log.info("PixelChat conversation deleted with ID: {}", conversationId);
    }

    @Override
    public List<ChatMessageDTO> getPixelMessages(String conversationId) {
        log.info("Fetching messages for PixelChat conversation ID: {}", conversationId);
        if (!conversationRepository.existsById(conversationId)) {
            log.warn("Attempted to fetch messages for non-existing PixelChat conversation ID: {}", conversationId);
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }
        // Assuming ChatMessageRepository has this method:
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationDTO> getAllPixelConversations() {
        log.info("Fetching all PixelChat conversations");
        // Assuming ConversationRepository has this method:
        // List<Conversation> conversations = conversationRepository.findBySource("pixel_chat");
        // If not, a more generic approach and then filtering:
        List<Conversation> conversations = conversationRepository.findAll().stream()
            .filter(c -> "pixel_chat".equals(c.getSource()))
            .collect(Collectors.toList());
        return conversations.stream()
                .map(this::mapToConversationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentResponse sendPixelMessage(ChatRequestDTO requestDTO) {
        log.info("Sending message to PixelChat conversation ID: {}", requestDTO.getConversationId());
        String conversationId = requestDTO.getConversationId();

        if (!conversationRepository.existsById(conversationId)) {
            log.warn("PixelChat conversation not found for sending message, ID: {}", conversationId);
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }

        // 1. Save user's message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setId(UUID.randomUUID().toString());
        userMessage.setConversationId(conversationId);
        userMessage.setUserId(requestDTO.getUserId()); // Assuming ChatRequestDTO has userId
        userMessage.setRole("user");
        userMessage.setContent(requestDTO.getMessage());
        userMessage.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(userMessage);
        log.info("User message saved with ID: {} for conversation ID: {}", userMessage.getId(), conversationId);

        // 2. Call existing ChatService to get agent's response
        // The prompt implies ChatService.sendMessage(ChatRequestDTO) returns AgentResponse
        AgentResponse agentResponse = chatService.sendMessage(requestDTO);
        log.info("Received agent response for conversation ID: {}", conversationId);

        // 3. Save assistant's reply
        // Assuming AgentResponse has a field like getMessage() or getContent() for the reply text
        // And also assuming we should save it only if a message is present
        if (agentResponse != null && agentResponse.getAssistantReply() != null && !agentResponse.getAssistantReply().isEmpty()) {
            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setId(UUID.randomUUID().toString());
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(agentResponse.getAssistantReply()); // Assuming this method exists
            assistantMessage.setCreatedAt(LocalDateTime.now());
            // assistantMessage.setUserId(null); // Or a system user ID
            chatMessageRepository.save(assistantMessage);
            log.info("Assistant message saved with ID: {} for conversation ID: {}", assistantMessage.getId(), conversationId);
        } else {
            log.info("No assistant reply to save for conversation ID: {}", conversationId);
        }

        // 4. Return AgentResponse
        return agentResponse;
    }

    // --- Helper methods for DTO mapping ---

    private ConversationDTO mapToConversationDTO(Conversation conversation) {
        if (conversation == null) return null;
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUserId());
        dto.setTitle(conversation.getTitle());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        dto.setSource(conversation.getSource());
        // Assuming ConversationDTO has a field for message count or recent message, which is not specified.
        // For now, keeping it simple.
        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage message) {
        if (message == null) return null;
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUserId(message.getUserId());
        return dto;
    }
}

// Assumptions made:
// 1. Structure of Conversation and ChatMessage entities (ID, userId, title, source, createdAt, updatedAt for Conversation;
//    ID, conversationId, role, content, createdAt, userId for ChatMessage).
// 2. Existence of corresponding getters/setters in entities and DTOs.
// 3. ConversationRepository has save, findById, deleteById, existsById, findAll.
// 4. ChatMessageRepository has save, findByConversationIdOrderByCreatedAtAsc (or similar for ordered messages),
//    and deleteByConversationId.
// 5. ChatRequestDTO has getConversationId(), getMessage(), getUserId().
// 6. AgentResponse has getAssistantReply() for the content of the assistant's message.
// 7. DTOs (ConversationDTO, ChatMessageDTO, ConversationCreateDTO, ConversationUpdateDTO) have necessary fields and constructors/setters.
// 8. `org.xue.agent.model.AgentResponse` is the correct path for AgentResponse.
// 9. `chatService.sendMessage(requestDTO)` is the correct method to call on the existing ChatService and it returns `AgentResponse`.
// 10. For `getAllPixelConversations`, if `conversationRepository.findBySource("pixel_chat")` is not available,
//     `findAll()` followed by a stream filter is an alternative. I've used the stream filter for robustness.
// 11. For `getPixelMessages`, using `findByConversationIdOrderByCreatedAtAsc` for ordered messages.
//     If not available, `findByConversationId` and then sorting in Java would be an alternative.
// 12. `javax.persistence.EntityNotFoundException` is appropriate.
// 13. `ConversationCreateDTO` has `getUserId()` and `getTitle()`.
// 14. `ConversationUpdateDTO` has `getTitle()`.

