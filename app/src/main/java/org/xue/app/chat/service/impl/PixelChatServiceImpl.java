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
import org.xue.app.agents.model.AgentResponse;
import org.xue.app.service.PromptsService;
import org.xue.app.service.DocumentParserService;

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
import java.io.ByteArrayInputStream;

@Service
@Slf4j
public class PixelChatServiceImpl implements PixelChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final PromptsService promptsService;
    private final DocumentParserService documentParserService;

    @Autowired
    public PixelChatServiceImpl(ConversationRepository conversationRepository,
                                ChatMessageRepository chatMessageRepository,
                                OpenAiClient openAiClient,
                                ObjectMapper objectMapper,
                                PromptsService promptsService,
                                DocumentParserService documentParserService) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
        this.promptsService = promptsService;
        this.documentParserService = documentParserService;
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
            // 1. 处理并解析附件内容
            String processedAttachments = processAndParseAttachments(requestDTO.getAttachments());
            
            // 2. 保存用户消息（只保存用户原始输入）
            ChatMessage userMessage = new ChatMessage();
            userMessage.setConversationId(conversationId);
            userMessage.setRole("user");
            userMessage.setContent(requestDTO.getMessage()); // 直接保存用户原始输入
            userMessage.setAttachments(processedAttachments);
            userMessage.setCreatedAt(LocalDateTime.now());
            chatMessageRepository.save(userMessage);
            log.info("User message saved with ID: {} for conversation ID: {}", userMessage.getId(), conversationId);

            // 3. 获取历史消息构建上下文
            List<ChatMessage> historyMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            // 4. 构建包含附件信息的完整消息内容（用于发送给AI）
            String messageWithAttachments = buildMessageWithAttachments(requestDTO.getMessage(), processedAttachments);
            
            // 5. 构建OpenAI请求（使用包含附件信息的消息）
            String requestJson = buildOpenAiRequestWithAttachments(historyMessages, conversation.getModel(), messageWithAttachments);
            log.info("Built OpenAI request for conversation ID: {}", conversationId);

            // 6. 调用OpenAI获取响应
            String responseJson = openAiClient.chatSync(requestJson, conversation.getModel());
            log.info("Received OpenAI response for conversation ID: {}", conversationId);

            // 7. 解析响应并保存AI消息
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

            // 8. 构建并返回响应
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
     * 处理并解析附件内容
     * 将base64内容解析为实际文档内容，并存储解析结果
     */
    private String processAndParseAttachments(String attachmentsJson) {
        if (attachmentsJson == null || attachmentsJson.trim().isEmpty()) {
            return null;
        }

        try {
            JsonNode attachments = objectMapper.readTree(attachmentsJson);
            if (!attachments.isArray() || attachments.size() == 0) {
                return null;
            }

            // 创建新的附件数组，包含解析后的内容
            List<Map<String, Object>> processedAttachments = new ArrayList<>();
            
            for (JsonNode attachment : attachments) {
                Map<String, Object> processedAttachment = new HashMap<>();
                
                // 复制基本信息
                if (attachment.has("id")) processedAttachment.put("id", attachment.get("id").asText());
                if (attachment.has("fileName")) processedAttachment.put("fileName", attachment.get("fileName").asText());
                if (attachment.has("fileSize")) processedAttachment.put("fileSize", attachment.get("fileSize").asLong());
                if (attachment.has("fileType")) processedAttachment.put("fileType", attachment.get("fileType").asText());
                if (attachment.has("filePath")) processedAttachment.put("filePath", attachment.get("filePath").asText());
                if (attachment.has("fileUrl")) processedAttachment.put("fileUrl", attachment.get("fileUrl").asText());
                
                String fileName = attachment.has("fileName") ? attachment.get("fileName").asText() : "unknown";
                String fileType = attachment.has("fileType") ? attachment.get("fileType").asText() : "";
                String base64Content = attachment.has("base64Content") ? attachment.get("base64Content").asText() : null;
                String originalContent = attachment.has("fileContent") ? attachment.get("fileContent").asText() : null;
                
                // 尝试解析文档内容
                String parsedContent = null;
                if (base64Content != null && !base64Content.trim().isEmpty() && 
                    documentParserService.isSupported(fileName, fileType)) {
                    
                    try {
                        // 解码base64并解析文档
                        String base64Data = base64Content.contains(",") ? base64Content.split(",")[1] : base64Content;
                        byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data);
                        
                        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                            parsedContent = documentParserService.parseDocument(inputStream, fileName, fileType);
                            log.info("成功解析文档: {}, 内容长度: {}", fileName, parsedContent != null ? parsedContent.length() : 0);
                        }
                    } catch (Exception e) {
                        log.error("解析文档 {} 失败: {}", fileName, e.getMessage());
                        parsedContent = null;
                    }
                }
                
                // 设置文件内容
                if (parsedContent != null && !parsedContent.trim().isEmpty()) {
                    // 使用解析后的内容
                    processedAttachment.put("fileContent", parsedContent);
                    processedAttachment.put("parsed", true);
                    // 为了节省空间，不保存base64Content到数据库
                } else if (originalContent != null && !originalContent.trim().isEmpty()) {
                    // 使用原始内容
                    processedAttachment.put("fileContent", originalContent);
                    processedAttachment.put("parsed", false);
                } else {
                    // 保留文件信息但无内容
                    processedAttachment.put("fileContent", null);
                    processedAttachment.put("parsed", false);
                }
                
                // 对于图片文件，保留base64用于显示
                if (fileType.startsWith("image/") && base64Content != null) {
                    processedAttachment.put("base64Content", base64Content);
                }
                
                processedAttachments.add(processedAttachment);
            }
            
            return objectMapper.writeValueAsString(processedAttachments);
            
        } catch (Exception e) {
            log.error("处理附件时出错: {}", e.getMessage(), e);
            return attachmentsJson; // 返回原始内容作为fallback
        }
    }

    /**
     * 构建包含附件信息的完整消息内容
     */
    private String buildMessageWithAttachments(String originalMessage, String attachmentsJson) {
        if (attachmentsJson == null || attachmentsJson.trim().isEmpty()) {
            return originalMessage;
        }

        try {
            JsonNode attachments = objectMapper.readTree(attachmentsJson);
            if (!attachments.isArray() || attachments.size() == 0) {
                return originalMessage;
            }

            StringBuilder messageBuilder = new StringBuilder();
            if (originalMessage != null && !originalMessage.trim().isEmpty()) {
                messageBuilder.append(originalMessage).append("\n\n");
            }
            
            messageBuilder.append("附件信息：\n");
            
            for (JsonNode attachment : attachments) {
                String fileName = attachment.has("fileName") ? attachment.get("fileName").asText() : "未知文件";
                String fileType = attachment.has("fileType") ? attachment.get("fileType").asText() : "";
                long fileSize = attachment.has("fileSize") ? attachment.get("fileSize").asLong() : 0;
                String fileContent = attachment.has("fileContent") ? attachment.get("fileContent").asText() : null;
                boolean isParsed = attachment.has("parsed") ? attachment.get("parsed").asBoolean() : false;
                String base64Content = attachment.has("base64Content") ? attachment.get("base64Content").asText() : null;
                
                // 根据文件类型构建不同的信息
                if (fileType.startsWith("image/")) {
                    messageBuilder.append("- 图片文件: ").append(fileName)
                            .append(" (").append(formatFileSize(fileSize)).append(")\n");
                    if (base64Content != null) {
                        messageBuilder.append("  [图片内容已提供，可能包含可识别的文本或对象]\n");
                    }
                } else if (fileContent != null && !fileContent.trim().isEmpty()) {
                    // 有解析后的文档内容
                    messageBuilder.append("- ").append(getDocumentTypeName(fileName)).append(": ").append(fileName)
                            .append(" (").append(formatFileSize(fileSize)).append(")\n");
                    
                    if (isParsed && isExcelFile(fileName) && fileContent.startsWith("{")) {
                        // Excel的JSON格式数据
                        messageBuilder.append("  Excel数据 (JSON格式):\n```json\n")
                                .append(fileContent.length() > 3000 ? fileContent.substring(0, 3000) + "..." : fileContent)
                                .append("\n```\n");
                    } else {
                        // 其他文档的文本内容
                        messageBuilder.append("  文档内容:\n```\n")
                                .append(fileContent.length() > 2000 ? fileContent.substring(0, 2000) + "..." : fileContent)
                                .append("\n```\n");
                    }
                } else {
                    // 没有可用的文档内容
                    messageBuilder.append("- 文件: ").append(fileName)
                            .append(" (").append(formatFileSize(fileSize)).append(")\n");
                    messageBuilder.append("  [文件已上传，但无法读取内容或解析失败]\n");
                }
            }
            
            return messageBuilder.toString();
            
        } catch (Exception e) {
            log.warn("Failed to parse attachments JSON: {}", e.getMessage());
            return originalMessage;
        }
    }

    /**
     * 构建包含附件信息的OpenAI请求
     */
    private String buildOpenAiRequestWithAttachments(List<ChatMessage> historyMessages, String model, String currentMessageWithAttachments) throws Exception {
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
        
        // 添加系统消息（针对文件处理进行优化）
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", promptsService.getPixelChatSystemPrompt());
        messages.add(systemMessage);
        
        // 添加历史消息
        for (ChatMessage msg : historyMessages) {
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            
            // 如果是用户消息且有附件，构建包含附件信息的内容
            if ("user".equals(msg.getRole()) && msg.getAttachments() != null && !msg.getAttachments().trim().isEmpty()) {
                String contentWithAttachments = buildMessageWithAttachments(msg.getContent(), msg.getAttachments());
                message.put("content", contentWithAttachments);
            } else {
                message.put("content", msg.getContent());
            }
            
            messages.add(message);
        }
        
        request.put("messages", messages);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        
        log.info("Building request for model: {}, temperature: {}, maxTokens: {}", model, temperature, maxTokens);
        
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
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
        
        // 处理attachments字段：如果是JSON字符串且不为空，保持原样；否则设为null
        String attachments = message.getAttachments();
        if (attachments != null && !attachments.trim().isEmpty()) {
            try {
                // 验证是否为有效的JSON
                JsonNode jsonNode = objectMapper.readTree(attachments);
                if (jsonNode.isArray()) {
                    dto.setAttachments(attachments);
                } else {
                    dto.setAttachments(null);
                }
            } catch (Exception e) {
                log.warn("Invalid attachments JSON format for message {}: {}", message.getId(), e.getMessage());
                dto.setAttachments(null);
            }
        } else {
            dto.setAttachments(null);
        }
        
        dto.setCreatedAt(message.getCreatedAt().toString());
        return dto;
    }

    /**
     * 获取文档类型名称
     */
    private String getDocumentTypeName(String fileName) {
        if (fileName == null) return "文档";
        String ext = fileName.toLowerCase();
        if (ext.endsWith(".pdf")) return "PDF文档";
        if (ext.endsWith(".doc") || ext.endsWith(".docx")) return "Word文档";
        if (ext.endsWith(".xls") || ext.endsWith(".xlsx")) return "Excel表格";
        if (ext.endsWith(".ppt") || ext.endsWith(".pptx")) return "PowerPoint演示";
        return "Office文档";
    }

    /**
     * 判断是否为PDF文档
     */
    private boolean isPdfDocument(String fileName) {
        if (fileName == null) return false;
        return fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * 判断是否为Excel文件
     */
    private boolean isExcelFile(String fileName) {
        if (fileName == null) return false;
        String ext = fileName.toLowerCase();
        return ext.endsWith(".xls") || ext.endsWith(".xlsx");
    }

    /**
     * 检测文本是否包含乱码
     */
    private boolean hasGibberish(String text) {
        if (text == null || text.length() == 0) return false;
        
        // 检测控制字符比例
        long controlChars = text.chars().filter(c -> c < 32 && c != 9 && c != 10 && c != 13).count();
        double controlCharRatio = (double) controlChars / text.length();
        
        // 如果控制字符比例过高，认为是乱码
        if (controlCharRatio > 0.1) return true;
        
        // 检测是否包含大量特殊替换字符
        long replacementChars = text.chars().filter(c -> c == 0xFFFD).count();
        if (replacementChars > text.length() * 0.05) return true;
        
        return false;
    }

    /**
     * 判断是否为Office文档
     */
    private boolean isOfficeDocument(String fileName) {
        if (fileName == null) return false;
        String ext = fileName.toLowerCase();
        return ext.endsWith(".doc") || ext.endsWith(".docx") || 
               ext.endsWith(".xls") || ext.endsWith(".xlsx") || 
               ext.endsWith(".ppt") || ext.endsWith(".pptx") || 
               ext.endsWith(".pdf");
    }
}
