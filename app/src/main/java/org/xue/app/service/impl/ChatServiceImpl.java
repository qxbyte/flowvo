package org.xue.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.xue.agent.client.core.McpClientTemplate;
import org.xue.agent.config.AgentProperties;
import org.xue.agent.model.AgentResponse;
import org.xue.agent.model.llm.*;
import org.xue.agent.service.AgentService;
import org.xue.agent.service.LlmService;
import org.xue.app.dto.*;
import org.xue.app.entity.ChatMessage;
import org.xue.app.entity.Conversation;
import org.xue.app.repository.ChatMessageRepository;
import org.xue.app.repository.ConversationRepository;
import org.xue.app.service.ChatService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private McpClientTemplate mcpTemplate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AgentProperties agentProperties;
    
    @Autowired
    private ObjectMapper objectMapper;

    // 多轮对话消息上下文（可替换为Redis）
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 获取下一个消息序号
     */
    private int getNextSequence(String conversationId) {
        Integer maxSequence = chatMessageRepository.findMaxSequenceByConversationId(conversationId);
        return maxSequence == null ? 1 : maxSequence + 1;
    }
    
    /**
     * 对话实体转DTO
     */
    private ConversationDTO convertToDTO(Conversation conversation) {
        // 获取最后一条消息
        ChatMessageDTO lastMessage = getLastMessage(conversation.getId());
        String lastMessageText = lastMessage != null ? lastMessage.getContent() : "";
        
        return ConversationDTO.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .service(conversation.getService())
                .model(conversation.getModel())
                .source(conversation.getSource())
                .createdAt(conversation.getCreatedAt().format(FORMATTER))
                .lastMessage(lastMessageText)
                .build();
    }
    
    /**
     * 获取对话中的最后一条消息
     */
    private ChatMessageDTO getLastMessage(String conversationId) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<ChatMessage> lastMessages = chatMessageRepository.findLastMessageByConversationId(conversationId, pageRequest);
        
        if (lastMessages.hasContent()) {
            return convertToChatMessageDTO(lastMessages.getContent().get(0));
        }
        
        return null;
    }
    
    /**
     * 消息实体转DTO
     */
    private ChatMessageDTO convertToChatMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .role(message.getRole())
                .content(message.getContent())
                .toolCallId(message.getTool_call_id())
                .toolName(message.getToolName())
                .sequence(message.getSequence())
                .createdAt(message.getCreatedAt().format(FORMATTER))
                .build();
    }
    
    @Override
    @Transactional
    public ConversationDTO createConversation(ConversationCreateDTO createDTO) {
        // 创建对话
        Conversation conversation = new Conversation();
        conversation.setTitle(createDTO.getTitle());
        conversation.setService(createDTO.getService());
        conversation.setModel(createDTO.getModel());
        
        // 设置用户ID
        if (createDTO.getUserId() != null && !createDTO.getUserId().isEmpty()) {
            conversation.setUserId(createDTO.getUserId());
            log.info("设置对话所属用户ID: {}", createDTO.getUserId());
        } else {
            log.warn("创建对话时未提供用户ID");
        }
        
        // 设置来源，如果为空则默认为chat
        if (createDTO.getSource() != null && !createDTO.getSource().isEmpty()) {
            conversation.setSource(createDTO.getSource());
        } else {
            conversation.setSource("chat");
        }
        
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("创建对话成功，ID: {}, 用户ID: {}", savedConversation.getId(), savedConversation.getUserId());
        
        return convertToDTO(savedConversation);
    }

    @Override
    public ConversationDTO getConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在，ID: " + conversationId));
        
        return convertToDTO(conversation);
    }
    
    @Override
    @Transactional
    public ConversationDTO updateConversation(String conversationId, ConversationUpdateDTO updateDTO) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在，ID: " + conversationId));
        
        // 更新对话标题
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().isEmpty()) {
            conversation.setTitle(updateDTO.getTitle());
        }
        
        // 更新时间
        conversation.setUpdatedAt(LocalDateTime.now());
        
        Conversation updatedConversation = conversationRepository.save(conversation);
        return convertToDTO(updatedConversation);
    }
    
    @Override
    @Transactional
    public void deleteConversation(String conversationId) {
        // 先删除所有消息
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderBySequenceAsc(conversationId);
        chatMessageRepository.deleteAll(messages);
        
        // 再删除对话
        conversationRepository.deleteById(conversationId);
    }
    
    @Override
    public List<ChatMessageDTO> getMessages(String conversationId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderBySequenceAsc(conversationId);
        
        return messages.stream()
                .filter(message -> {
                    String role = message.getRole();
                    String content = message.getContent();
                    return (("user".equals(role) || "assistant".equals(role)) 
                            && content != null && !content.isEmpty());
                })
                .map(this::convertToChatMessageDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentResponse sendMessage(ChatRequestDTO requestDTO) {
        // 清空历史消息
        messageHistory.clear();
        
        // 获取或创建对话
        Conversation conversation;
        if (requestDTO.getConversationId() == null || requestDTO.getConversationId().isEmpty()) {
            // 创建新对话
            conversation = new Conversation();
            conversation.setTitle(requestDTO.getMessage().length() > 30 
                    ? requestDTO.getMessage().substring(0, 30) + "..." 
                    : requestDTO.getMessage());
            conversation.setModel(agentProperties.getDefaultModel());
            conversation = conversationRepository.save(conversation);
            log.info("创建新对话: {}", conversation.getId());
        } else {
            // 获取已有对话
            conversation = conversationRepository.findById(requestDTO.getConversationId())
                    .orElseThrow(() -> new RuntimeException("对话不存在，ID: " + requestDTO.getConversationId()));
            log.info("获取现有对话: {}", conversation.getId());
        }
        
        String conversationId = conversation.getId();
        
        try {
            // 1. 本次请求的消息列表
            List<ChatMessage> historyMessages = new ArrayList<>();
            
            // 2. 添加系统提示词（如果不存在）
            boolean hasSystemMessage = historyMessages.stream().anyMatch(msg -> "system".equals(msg.getRole()));
            if (!hasSystemMessage) {
                ChatMessage systemMessage = new ChatMessage();
                systemMessage.setConversationId(conversationId);
                systemMessage.setRole("system");
                systemMessage.setContent(agentProperties.getSystemPrompt());
                systemMessage.setSequence(getNextSequence(conversationId));
                chatMessageRepository.save(systemMessage);
                addMessage(systemMessage);
                log.info("添加系统提示词");
            }
            
            // 3. 添加用户消息
            ChatMessage userMessage = new ChatMessage();
            userMessage.setConversationId(conversationId);
            userMessage.setRole("user");
            userMessage.setContent(requestDTO.getMessage());
            userMessage.setSequence(getNextSequence(conversationId));
            chatMessageRepository.save(userMessage);
            addMessage(userMessage);
            log.info("添加用户消息: {}", requestDTO.getMessage());
            
            // 初始化统计变量
            int interactionCount = 0;
            int totalTokens = 0;
            String finalContent = null;
            
            // 循环处理工具调用，最多循环10次
            int maxInteractions = agentProperties.getMaxInteractions();
            
            while (interactionCount < maxInteractions) {
                interactionCount++;
                log.info("开始第 {} 次交互", interactionCount);
                
                // 构建LLM消息列表
                List<Message> messages = convertChatMessagesToLlmMessages(messageHistory);
                
                // 构建LLM请求
                LlmRequest llmRequest = LlmRequest.builder()
                        .model(agentProperties.getDefaultModel())
                        .messages(messages)
                        .tools(getTools())
                        .tool_choice("auto")
                        .temperature(requestDTO.getTemperature() != null ? 
                                requestDTO.getTemperature() : agentProperties.getTemperature())
                        .stream(false)
                        .build();
                
                // 记录请求日志
                log.info("发送请求到LLM:\n模型: {}\n温度: {}\n消息数量: {}", 
                        llmRequest.getModel(), 
                        llmRequest.getTemperature(),
                        llmRequest.getMessages().size());
                
                if (log.isDebugEnabled()) {
                    try {
                        log.debug("LLM请求详情: {}", objectMapper.writeValueAsString(llmRequest));
                    } catch (Exception e) {
                        log.debug("序列化LLM请求失败: {}", e.getMessage());
                    }
                }
                
                // 发送请求到LLM
                log.info("开始发送请求到LLM，时间: {}", LocalDateTime.now());
                long startTime = System.currentTimeMillis();
                LlmResponse llmResponse = llmService.callLlm(llmRequest);
                long endTime = System.currentTimeMillis();
                log.info("收到LLM响应，耗时: {}ms，时间: {}", (endTime - startTime), LocalDateTime.now());
                
                // 检查响应
                if (llmResponse == null) {
                    log.error("LLM响应为空");
                    // 不清空messageHistory，保留之前可能的回复
                    return AgentResponse.error("LLM响应为空");
                }
                
                // 累计token数
                if (llmResponse.getUsage() != null) {
                    totalTokens += llmResponse.getUsage().getTotal_tokens();
                }
                
                log.info("收到LLM响应:\n状态: 成功\n总Token数: {}\n选项数量: {}", 
                        llmResponse.getUsage() != null ? llmResponse.getUsage().getTotal_tokens() : "未知",
                        llmResponse.getChoices() != null ? llmResponse.getChoices().size() : 0);
                
                if (log.isDebugEnabled()) {
                    try {
                        log.debug("LLM响应详情: {}", objectMapper.writeValueAsString(llmResponse));
                    } catch (Exception e) {
                        log.debug("序列化LLM响应失败: {}", e.getMessage());
                    }
                }
                
                if (llmResponse.getChoices() == null || llmResponse.getChoices().isEmpty()) {
                    log.error("LLM响应无效：没有选项");
                    // 不清空messageHistory，保留之前可能的回复
                    return AgentResponse.error("LLM响应无效");
                }
                
                AssistantMessage assistantMessage = llmResponse.getChoices().get(0).getMessage();
                
                // 检查是否有内容和工具调用
                boolean hasContent = assistantMessage.getContent() != null && !assistantMessage.getContent().trim().isEmpty();
                boolean hasToolCalls = assistantMessage.getTool_calls() != null && !assistantMessage.getTool_calls().isEmpty();
                
                log.info("LLM回复状态: hasContent={}, hasToolCalls={}", hasContent, hasToolCalls);
                
                // 退出条件: 有内容且没有工具调用
                if (hasContent && !hasToolCalls) {
                    log.info("满足退出条件: 有内容且没有工具调用，结束循环");
                    finalContent = assistantMessage.getContent();
                    
                    // 保存最终的助手消息
                    ChatMessage finalAssistantMessage = new ChatMessage();
                    finalAssistantMessage.setConversationId(conversationId);
                    finalAssistantMessage.setRole("assistant");
                    finalAssistantMessage.setContent(finalContent);
                    finalAssistantMessage.setSequence(getNextSequence(conversationId));
                    chatMessageRepository.save(finalAssistantMessage);
                    addMessage(finalAssistantMessage);
                    
                    break;
                }
                
                // 保存助手消息（包含tool_calls）
                ChatMessage assistantChatMessage = new ChatMessage();
                assistantChatMessage.setConversationId(conversationId);
                assistantChatMessage.setRole("assistant");
                assistantChatMessage.setContent(assistantMessage.getContent() != null ?
                        assistantMessage.getContent() : "");
                
                // 序列化tool_calls为JSON字符串并保存
                if (hasToolCalls) {
                    try {
                        String toolCalls = objectMapper.writeValueAsString(assistantMessage.getTool_calls());
                        assistantChatMessage.setTool_calls(toolCalls);
                    } catch (Exception e) {
                        log.error("序列化tool_calls失败: {}", e.getMessage());
                    }
                }
                
                assistantChatMessage.setSequence(getNextSequence(conversationId));
                chatMessageRepository.save(assistantChatMessage);
                addMessage(assistantChatMessage);
                
                // 如果没有工具调用但也没有内容，继续循环
                if (!hasToolCalls) {
                    log.warn("没有工具调用也没有足够内容，继续循环");
                    continue;
                }
                
                // 处理工具调用
                log.info("LLM返回了 {} 个工具调用，开始处理", assistantMessage.getTool_calls().size());
                
                // 处理每个工具调用
                for (ToolCall toolCall : assistantMessage.getTool_calls()) {
                    String toolCallId = toolCall.getId();
                    FunctionCall functionCall = toolCall.getFunction();
                    
                    if (functionCall == null) {
                        log.warn("工具调用缺少function字段");
                        continue;
                    }
                    
                    String functionName = functionCall.getName();
                    String argumentsJson = functionCall.getArguments();
                    
                    log.info("处理工具调用: {} - 参数: {}", functionName, argumentsJson);
                    
                    // 根据函数名确定对应的服务
                    String serviceName = determineServiceName(functionName);
                    
                    if (serviceName == null || serviceName.isEmpty()) {
                        String errorMsg = "未能确定函数对应的服务: " + functionName;
                        log.error(errorMsg);
                        
                        // 保存错误消息作为工具结果
                        saveFunctionCallMessage(conversationId, toolCallId, functionName, 
                                "{\"error\":\"" + errorMsg + "\"}");
                        
                        // 添加错误结果到消息历史
                        ChatMessage errorToolMessage = new ChatMessage();
                        errorToolMessage.setConversationId(conversationId);
                        errorToolMessage.setRole("tool");
                        errorToolMessage.setContent("{\"error\":\"" + errorMsg + "\"}");
                        errorToolMessage.setTool_call_id(toolCallId);
                        errorToolMessage.setToolName(functionName);
                        addMessage(errorToolMessage);
                        continue;
                    }
                    
                    // 执行函数调用
                    String result = executeMcpFunction(serviceName, functionName, argumentsJson);
                    
                    // 保存函数调用结果
                    saveFunctionCallMessage(conversationId, toolCallId, functionName, result);
                    
                    // 添加工具结果到消息历史
                    ChatMessage toolMessage = new ChatMessage();
                    toolMessage.setConversationId(conversationId);
                    toolMessage.setRole("tool");
                    toolMessage.setContent(result);
                    toolMessage.setTool_call_id(toolCallId);
                    toolMessage.setToolName(functionName);
                    addMessage(toolMessage);
                }
            }
            
            // 处理最终结果
            if (finalContent != null && !finalContent.isEmpty()) {
                // 返回成功响应
                return AgentResponse.success(finalContent, interactionCount, totalTokens);
            } else {
                // 返回警告响应，但确保提供可用的内容
                String warningContent = "达到最大交互次数，这是目前的回复：\n\n";
                
                // 尝试找到最后一条助手消息作为回复内容
                Optional<ChatMessage> lastAssistantMessage = messageHistory.stream()
                        .filter(msg -> "assistant".equals(msg.getRole()) && msg.getContent() != null && !msg.getContent().isEmpty())
                        .reduce((first, second) -> second);
                
                if (lastAssistantMessage.isPresent()) {
                    warningContent += lastAssistantMessage.get().getContent();
                    // 将最后一条助手消息保存为最终消息
                    ChatMessage finalMessage = new ChatMessage();
                    finalMessage.setConversationId(conversationId);
                    finalMessage.setRole("assistant");
                    finalMessage.setContent(warningContent);
                    finalMessage.setSequence(getNextSequence(conversationId));
                    chatMessageRepository.save(finalMessage);
                    // 不需要清空消息历史，让前端显示这条消息
                    return AgentResponse.warning(warningContent, interactionCount, totalTokens);
                } else {
                    return AgentResponse.warning("未能获取最终回复", interactionCount, totalTokens);
                }
            }
        } catch (Exception e) {
            log.error("处理用户消息异常", e);
            
            // 尝试找到最后一条助手消息作为回复内容
            Optional<ChatMessage> lastAssistantMessage = messageHistory.stream()
                    .filter(msg -> "assistant".equals(msg.getRole()) && msg.getContent() != null && !msg.getContent().isEmpty())
                    .reduce((first, second) -> second);
            
            if (lastAssistantMessage.isPresent()) {
                String errorContent = "处理过程中出现错误，但这是目前的回复：\n\n" + lastAssistantMessage.get().getContent();
                // 保存最终的助手消息
                ChatMessage errorMessage = new ChatMessage();
                errorMessage.setConversationId(conversationId);
                errorMessage.setRole("assistant");
                errorMessage.setContent(errorContent);
                errorMessage.setSequence(getNextSequence(conversationId));
                chatMessageRepository.save(errorMessage);
                // 不清空消息历史，保留之前的回复
                return AgentResponse.error(errorContent);
            } else {
                // 清空消息历史
                messageHistory.clear();
                return AgentResponse.error("处理失败: " + e.getMessage());
            }
        }
    }
    
    private void addMessage(ChatMessage message) {
        // 添加消息时记录当前时间
        LocalDateTime now = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000);
        message.setCreatedAt(now);
        messageHistory.add(message);
    }

    
    /**
     * 保存函数调用消息到数据库
     */
    private void saveFunctionCallMessage(String conversationId, String toolCallId, String functionName, String toolResult) {
        ChatMessage toolMessage = new ChatMessage();
        toolMessage.setConversationId(conversationId);
        toolMessage.setRole("tool");
        toolMessage.setContent(toolResult);
        toolMessage.setTool_call_id(toolCallId);
        toolMessage.setToolName(functionName);
        toolMessage.setSequence(getNextSequence(conversationId));
        chatMessageRepository.save(toolMessage);
    }

    private List<Tool> getTools() {
        // 获取所有可用服务
        Map<String, Map<String, Object>> serversStatus = mcpTemplate.getServersStatus();

        List<Tool> tools = new ArrayList<>();
        // 遍历所有服务，获取所有可用工具
        for (Map.Entry<String, Map<String, Object>> entry : serversStatus.entrySet()) {
            String serviceName = entry.getKey();
            Map<String, Object> serviceStatus = entry.getValue();

            if (!(Boolean) serviceStatus.get("connected")) {
                continue;
            }
            try {
                List<Tool> serverTools = getToolsFromSchema(serviceName);
                tools.addAll(serverTools);
                log.info("从服务 {} 获取了 {} 个工具", serviceName, serverTools.size());
            } catch (Exception e) {
                log.error("获取服务 {} 的工具异常", serviceName, e);
            }
        }
        log.info("总共获取了 {} 个工具", tools.size());
        return tools;
    }

    /**
     * 从服务的schema获取工具列表
     *
     * @param serviceName 服务名称
     * @return 工具列表
     */
    private List<Tool> getToolsFromSchema(String serviceName) {
        List<Tool> tools = new ArrayList<>();
        
        try {
            // 检查服务是否可用
            if (!mcpTemplate.isServerAvailable(serviceName)) {
                log.warn("服务不可用: {}", serviceName);
                return tools;
            }
            
            // 获取服务的schema
            String schemaUrl = mcpTemplate.getSchemaUrl(serviceName, "function_calling");
            ResponseEntity<Map> response = restTemplate.getForEntity(schemaUrl, Map.class);
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("获取服务schema失败: {}", serviceName);
                return tools;
            }
            
            Map<String, Object> schemaData = response.getBody();
            
            // 检查schema中是否有functions
            if (schemaData.containsKey("functions")) {
                List<Map<String, Object>> functions = (List<Map<String, Object>>) schemaData.get("functions");
                
                // 转换为Tool对象
                for (Map<String, Object> function : functions) {
                    String name = (String) function.get("name");
                    String description = (String) function.get("description");
                    
                    // 获取参数信息
                    ToolParameter parameters = null;
                    if (function.containsKey("parameters")) {
                        try {
                            // 将parameters转换为ToolParameter对象
                            String paramsJson = objectMapper.writeValueAsString(function.get("parameters"));
                            parameters = objectMapper.readValue(paramsJson, ToolParameter.class);
                        } catch (Exception e) {
                            log.warn("解析函数 {} 的参数失败: {}", name, e.getMessage());
                        }
                    }
                    
                    // 创建Tool对象
                    Tool tool = Tool.functionTool(name, description, parameters);
                    tools.add(tool);
                }
            } else if (schemaData.containsKey("tools")) {
                // 如果schema直接包含tools
                try {
                    String toolsJson = objectMapper.writeValueAsString(schemaData.get("tools"));
                    List<Tool> schemaTools = objectMapper.readValue(toolsJson, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Tool.class));
                    tools.addAll(schemaTools);
                } catch (Exception e) {
                    log.error("解析工具列表失败: {}", e.getMessage());
                }
            }
            
            log.debug("从服务 {} 获取了 {} 个工具", serviceName, tools.size());
            
        } catch (Exception e) {
            log.error("获取服务 {} 工具异常", serviceName, e);
        }
        
        return tools;
    }
    
    /**
     * 根据函数名确定服务名称
     */
    private String determineServiceName(String functionName) {
        // 获取所有可用服务
        Map<String, Map<String, Object>> serversStatus = mcpTemplate.getServersStatus();
        
        // 遍历所有服务，查找包含该函数的服务
        for (Map.Entry<String, Map<String, Object>> entry : serversStatus.entrySet()) {
            String serviceName = entry.getKey();
            Map<String, Object> serviceStatus = entry.getValue();
            
            if (!(Boolean) serviceStatus.get("connected")) {
                continue;
            }
            
            try {
                // 获取服务的schema
                String schemaUrl = mcpTemplate.getSchemaUrl(serviceName, "function_calling");
                ResponseEntity<Map> response = restTemplate.getForEntity(schemaUrl, Map.class);
                
                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    continue;
                }
                
                Map<String, Object> schemaData = response.getBody();
                if (schemaData.containsKey("functions")) {
                    List<Map<String, Object>> functions = (List<Map<String, Object>>) schemaData.get("functions");
                    
                    // 检查服务是否包含指定的函数
                    for (Map<String, Object> function : functions) {
                        String name = (String) function.get("name");
                        if (functionName.equals(name)) {
                            return serviceName;
                        }
                    }
                } else if (schemaData.containsKey("tools")) {
                    try {
                        String toolsJson = objectMapper.writeValueAsString(schemaData.get("tools"));
                        List<Map<String, Object>> tools = objectMapper.readValue(toolsJson, 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        
                        for (Map<String, Object> tool : tools) {
                            if ("function".equals(tool.get("type"))) {
                                Map<String, Object> function = (Map<String, Object>) tool.get("function");
                                String name = (String) function.get("name");
                                if (functionName.equals(name)) {
                                    return serviceName;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析工具列表失败: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("获取服务schema异常: " + serviceName, e);
            }
        }
        
        return null;
    }
    
    /**
     * 执行MCP函数
     */
    private String executeMcpFunction(String serviceName, String functionName, String arguments) {
        try {
            log.info("执行MCP函数: {}.{} - 参数: {}", serviceName, functionName, arguments);
            
            // 检查服务是否存在
            if (!mcpTemplate.isServerAvailable(serviceName)) {
                return "{\"error\":\"服务不存在或未连接\"}";
            }
            
            // 使用McpClientTemplate获取RPC URL
            String rpcUrl = mcpTemplate.getRpcUrl(serviceName);
            
            // 解析参数
            Map<String, Object> params = objectMapper.readValue(arguments, Map.class);
            
            // 构建RPC请求
            Map<String, Object> rpcParams = new HashMap<>();
            rpcParams.put("jsonrpc", "2.0");
            rpcParams.put("method", functionName);
            rpcParams.put("params", params);
            rpcParams.put("id", System.currentTimeMillis());
            
            // 发送RPC请求
            ResponseEntity<String> response = restTemplate.postForEntity(rpcUrl, rpcParams, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "{\"error\":\"调用服务失败: " + response.getStatusCode() + "\"}";
            }
        } catch (JsonProcessingException e) {
            log.error("解析参数异常", e);
            return "{\"error\":\"参数解析失败: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("执行MCP函数异常", e);
            return "{\"error\":\"执行函数失败: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 将内部ChatMessage列表转换为LLM的Message列表
     */
    private List<Message> convertChatMessagesToLlmMessages(List<ChatMessage> chatMessages) {
        List<Message> messages = new ArrayList<>();
        
        for (ChatMessage msg : chatMessages) {
            switch (msg.getRole()) {
                case "system":
                    messages.add(Message.systemMessage(msg.getContent()));
                    break;
                case "user":
                    messages.add(Message.userMessage(msg.getContent()));
                    break;
                case "assistant":
                    if (msg.getTool_calls() != null && !msg.getTool_calls().isEmpty()) {
                        try {
                            // 将字符串转回为JsonNode
                            JsonNode toolCallsNode = objectMapper.readTree(msg.getTool_calls());
                            
                            // 检查是否是有效的工具调用数组
                            if (toolCallsNode.isArray()) {
                                messages.add(Message.assistantMessage(msg.getContent(), toolCallsNode));
                            } else {
                                log.warn("tool_calls不是有效的数组: {}", msg.getTool_calls());
                                messages.add(Message.assistantMessage(msg.getContent()));
                            }
                        } catch (Exception e) {
                            log.warn("解析tool_calls失败，只传递content: {}", e.getMessage());
                            messages.add(Message.assistantMessage(msg.getContent()));
                        }
                    } else {
                        messages.add(Message.assistantMessage(msg.getContent()));
                    }
                    break;
                case "tool":
                    messages.add(Message.toolMessage(msg.getTool_call_id(), msg.getToolName(), msg.getContent()));
                    break;
            }
        }
        
        return messages;
    }

    /**
     * 获取所有对话列表
     */
    @Override
    public List<ConversationDTO> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID获取对话列表
     * 
     * @param userId 用户ID
     * @return 对话列表
     */
    public List<ConversationDTO> getConversationsByUserId(String userId) {
        List<Conversation> conversations;
        
        if (userId == null || userId.isEmpty()) {
            log.warn("获取对话列表时未提供用户ID，返回所有对话");
            conversations = conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            log.info("获取用户{}的对话列表", userId);
            conversations = conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据来源获取对话列表
     * 
     * @param source 对话来源，如果为null则获取所有对话
     * @return 对话列表
     */
    public List<ConversationDTO> getConversationsBySource(String source) {
        List<Conversation> conversations;
        
        if (source == null) {
            conversations = conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            conversations = conversationRepository.findBySourceOrderByCreatedAtDesc(source);
        }
        
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据来源和用户ID获取对话列表
     * 
     * @param source 对话来源
     * @param userId 用户ID
     * @return 对话列表
     */
    public List<ConversationDTO> getConversationsBySourceAndUserId(String source, String userId) {
        List<Conversation> conversations;
        
        if (source == null && (userId == null || userId.isEmpty())) {
            log.warn("获取对话列表时未提供来源和用户ID，返回所有对话");
            conversations = conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (source == null) {
            log.info("获取用户{}的对话列表", userId);
            conversations = conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else if (userId == null || userId.isEmpty()) {
            log.info("获取来源为{}的对话列表", source);
            conversations = conversationRepository.findBySourceOrderByCreatedAtDesc(source);
        } else {
            log.info("获取来源为{}、用户ID为{}的对话列表", source, userId);
            conversations = conversationRepository.findBySourceAndUserIdOrderByCreatedAtDesc(source, userId);
        }
        
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
} 