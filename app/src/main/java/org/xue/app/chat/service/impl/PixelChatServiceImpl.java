package org.xue.app.chat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.xue.app.chat.service.PixelChatService;
import org.xue.app.client.OpenAiClient;
import org.xue.app.entity.ChatMessage;
import org.xue.app.entity.Conversation;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;
import org.xue.app.repository.ChatMessageRepository;
import org.xue.app.repository.ConversationRepository;
import org.xue.agent.model.AgentResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PixelChatServiceImpl implements PixelChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PixelChatServiceImpl(ConversationRepository conversationRepository,
                                ChatMessageRepository chatMessageRepository,
                                OpenAiClient openAiClient,
                                ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    @Value("${ai.openai.chat.options.model}")
    private String defaultModel;

    @Override
    @Transactional
    public ConversationDTO createPixelConversation(ConversationCreateDTO createDTO) {
        log.info("Creating new PixelChat conversation for user: {}", createDTO.getUserId());
        Conversation conversation = new Conversation();
        conversation.setUserId(createDTO.getUserId());
        conversation.setTitle(createDTO.getTitle());
        
        // 使用传入的source参数，如果为空则使用默认值"chat"
        String source = createDTO.getSource();
        conversation.setSource(source != null && !source.isEmpty() ? source : "chat");
        
        // 设置service字段，如果DTO中没有提供则使用默认值
        String service = createDTO.getService();
        conversation.setService(service != null && !service.isEmpty() ? service : "openai");
        
        // 设置model字段，如果DTO中没有提供则使用默认值
        String model = createDTO.getModel();
        conversation.setModel(model != null && !model.isEmpty() ? model : "gpt-3.5-turbo");

        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("PixelChat conversation created with ID: {}", savedConversation.getId());
        return mapToConversationDTO(savedConversation);
    }

    @Override
    public ConversationDTO getPixelConversation(String conversationId, String userId) {
        log.info("Fetching PixelChat conversation with ID: {} for user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("PixelChat conversation not found with ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });
        
        // 验证用户权限
        if (!userId.equals(conversation.getUserId())) {
            log.warn("User {} attempted to access conversation {} owned by {}", userId, conversationId, conversation.getUserId());
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }
        
        return mapToConversationDTO(conversation);
    }

    @Override
    @Transactional
    public ConversationDTO updatePixelConversationTitle(String conversationId, ConversationUpdateDTO updateDTO, String userId) {
        log.info("Updating conversation ID: {} by user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("PixelChat conversation not found for update with ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });

        // 验证用户权限
        if (!userId.equals(conversation.getUserId())) {
            log.warn("User {} attempted to update conversation {} owned by {}", userId, conversationId, conversation.getUserId());
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }

        // 更新标题（如果提供）
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().trim().isEmpty()) {
            conversation.setTitle(updateDTO.getTitle());
            log.info("Updated title for conversation ID: {}", conversationId);
        }
        
        // 更新模型（如果提供）
        if (updateDTO.getModel() != null && !updateDTO.getModel().trim().isEmpty()) {
            conversation.setModel(updateDTO.getModel());
            log.info("Updated model to {} for conversation ID: {}", updateDTO.getModel(), conversationId);
        }
        
        // 更新服务（如果提供）
        if (updateDTO.getService() != null && !updateDTO.getService().trim().isEmpty()) {
            conversation.setService(updateDTO.getService());
            log.info("Updated service to {} for conversation ID: {}", updateDTO.getService(), conversationId);
        }
        
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation updatedConversation = conversationRepository.save(conversation);
        log.info("PixelChat conversation updated for ID: {}", updatedConversation.getId());
        return mapToConversationDTO(updatedConversation);
    }

    @Override
    @Transactional
    public void deletePixelConversation(String conversationId, String userId) {
        log.info("Deleting PixelChat conversation with ID: {} by user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("Attempted to delete non-existing PixelChat conversation with ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId + " for deletion.");
                });
        
        // 验证用户权限
        if (!userId.equals(conversation.getUserId())) {
            log.warn("User {} attempted to delete conversation {} owned by {}", userId, conversationId, conversation.getUserId());
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId + " for deletion.");
        }
        
        // 先删除关联的消息
        chatMessageRepository.deleteByConversationId(conversationId);
        log.info("Associated messages deleted for conversation ID: {}", conversationId);

        conversationRepository.deleteById(conversationId);
        log.info("PixelChat conversation deleted with ID: {}", conversationId);
    }

    @Override
    public List<ChatMessageDTO> getPixelMessages(String conversationId, String userId) {
        log.info("Fetching messages for PixelChat conversation ID: {} by user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("Attempted to fetch messages for non-existing PixelChat conversation ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });
        
        // 验证用户权限
        if (!userId.equals(conversation.getUserId())) {
            log.warn("User {} attempted to access messages for conversation {} owned by {}", userId, conversationId, conversation.getUserId());
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }
        
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationDTO> getAllPixelConversations(String userId) {
        log.info("Fetching all PixelChat conversations for user: {}", userId);
        // 根据用户ID和来源筛选对话
        List<Conversation> conversations = conversationRepository.findBySourceAndUserIdOrderByCreatedAtDesc("chat", userId);
        return conversations.stream()
                .map(this::mapToConversationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentResponse sendPixelMessage(ChatRequestDTO requestDTO) {
        log.info("Sending message to PixelChat conversation ID: {}", requestDTO.getConversationId());
        String conversationId = requestDTO.getConversationId();
        String userId = requestDTO.getUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> {
                    log.warn("PixelChat conversation not found for sending message, ID: {}", conversationId);
                    return new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
                });
        
        // 验证用户权限
        if (!userId.equals(conversation.getUserId())) {
            log.warn("User {} attempted to send message to conversation {} owned by {}", userId, conversationId, conversation.getUserId());
            throw new EntityNotFoundException("PixelChat Conversation not found with id: " + conversationId);
        }

        try {
            // 1. 保存用户消息
            ChatMessage userMessage = new ChatMessage();
            userMessage.setConversationId(conversationId);
            userMessage.setRole("user");
            userMessage.setContent(requestDTO.getMessage());
            userMessage.setCreatedAt(LocalDateTime.now());
            chatMessageRepository.save(userMessage);
            log.info("User message saved with ID: {} for conversation ID: {}", userMessage.getId(), conversationId);

            // 2. 获取历史消息构建上下文
            List<ChatMessage> historyMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            // 3. 构建OpenAI请求
            String requestJson = buildOpenAiRequest(historyMessages, conversation.getModel());
            log.info("Built OpenAI request for conversation ID: {}", conversationId);

            // 4. 调用OpenAI获取响应 - 直接传入model避免重复解析
            String responseJson = openAiClient.chatSync(requestJson, conversation.getModel());
            log.info("Received OpenAI response for conversation ID: {}", conversationId);

            // 5. 解析响应并保存AI消息
            String assistantReply = extractAssistantReply(responseJson);
            if (assistantReply != null && !assistantReply.isEmpty()) {
                ChatMessage assistantMessage = new ChatMessage();
                assistantMessage.setConversationId(conversationId);
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(assistantReply);
                assistantMessage.setCreatedAt(LocalDateTime.now());
                chatMessageRepository.save(assistantMessage);
                log.info("Assistant message saved with ID: {} for conversation ID: {}", assistantMessage.getId(), conversationId);
            }

            // 6. 构建并返回响应
            AgentResponse agentResponse = AgentResponse.builder()
                    .status("success")
                    .assistantReply(assistantReply)
                    .content(assistantReply)
                    .message(assistantReply)
                    .interactions(1)
                    .totalTokens(0)
                    .build();
            
            return agentResponse;

        } catch (Exception e) {
            log.error("Error processing message for conversation ID {}: {}", conversationId, e.getMessage(), e);
            
            // 返回错误响应
            return AgentResponse.error("抱歉，处理您的消息时遇到了错误，请稍后再试。");
        }
    }

    /**
     * 构建OpenAI请求JSON
     */
    private String buildOpenAiRequest(List<ChatMessage> historyMessages, String model) throws Exception {
        if(null==model || model.isEmpty()){
            model = defaultModel;
        }
        
        // 从OpenAiClient获取模型对应的配置参数
        double temperature = openAiClient.getTemperatureForModel(model);
        int maxTokens = openAiClient.getMaxTokensForModel(model);
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("stream", false);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个友善、有帮助的AI助手。");
        messages.add(systemMessage);
        
        // 添加历史消息
        for (ChatMessage msg : historyMessages) {
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }
        
        request.put("messages", messages);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        
        log.info("Building request for model: {}, temperature: {}, maxTokens: {}", model, temperature, maxTokens);
        
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 从OpenAI响应中提取AI回复内容
     */
    private String extractAssistantReply(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing OpenAI response: {}", e.getMessage(), e);
        }
        return "抱歉，无法解析AI回复，请稍后再试。";
    }

    // --- Helper methods for DTO mapping ---

    private ConversationDTO mapToConversationDTO(Conversation conversation) {
        if (conversation == null) return null;
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUserId());
        dto.setTitle(conversation.getTitle());
        dto.setService(conversation.getService());
        dto.setModel(conversation.getModel());
        dto.setCreatedAt(conversation.getCreatedAt().toString());
        dto.setUpdatedAt(conversation.getUpdatedAt().toString());
        dto.setSource(conversation.getSource());
        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage message) {
        if (message == null) return null;
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt().toString());
        return dto;
    }
}
