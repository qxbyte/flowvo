package org.xue.assistant.functioncall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xue.assistant.chat.service.ChatService;
import org.xue.assistant.chat.entity.ChatRecord;
import org.xue.assistant.functioncall.client.OpenAiClient;
import org.xue.assistant.functioncall.dto.model.ChatMessage;
import org.xue.assistant.functioncall.dto.model.Tool;
import org.xue.assistant.functioncall.entity.CallMessage;
import org.xue.assistant.functioncall.repository.CallMessageRepository;
import org.xue.assistant.functioncall.service.FunctionCallService;
import org.xue.assistant.functioncall.util.FunctionDefinitionRegistry;
import org.xue.assistant.functioncall.util.ModelRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 2.0 模式：
 * 请求里只有 tools + tool_choice，不 带 functions/function_call。
 * messages 里用 "role":"tool" 来传函数结果，前提是紧跟在带 tool_calls 的 assistant 消息之后。
 *
 * 1.0 模式：
 * 请求里只有 functions + function_call，不带 tools/tool_choice。
 * messages 里若要回传函数结果，要用 "role":"function"（注意旧版是 function）。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FunctionCallServiceImpl implements FunctionCallService {

    @Value("${ai.openai.chat.options.model}")
    private String model;

    @Autowired
    private final OpenAiClient openAiClient;
    
    @Autowired
    private final CallMessageRepository callMessageRepository;
    
    @Autowired
    private final ChatService chatService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 多轮对话消息上下文（可替换为Redis）
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    
    private String currentChatId;
    
    // 默认用户ID，实际系统应该从认证信息中获取
    private static final String DEFAULT_USER_ID = "5";
    // AI处理类型对话标识
    private static final String AI_PROCESS_TYPE = "AIPROCESS";

    @Override
    public void handleUserQuestion(String question) {

        // 获取或创建AIPROCESS类型的聊天记录
        ChatRecord chatRecord = chatService.getOrCreateChatRecordByType(DEFAULT_USER_ID, AI_PROCESS_TYPE);
        currentChatId = UUID.randomUUID().toString();
        // 如果是新会话，生成新的chatId
        if (null!=chatRecord&& StringUtils.isNotEmpty(chatRecord.getId().trim())) {
            currentChatId = chatRecord.getId();
        }
        
        // 添加用户输入
        addMessage(new ChatMessage("system", "你是一个助手，如果需要调用函数，请使用 function_call 返回 JSON。"));
        addMessage(new ChatMessage("user", question));

        // 构建请求 JSON
        String requestJson = buildRequestJson("auto");

        try {
            String result = openAiClient.chatSync(requestJson);
            handleAiResponse(result);
            // 保存消息到数据库
            persistChatHistory();
        } catch (Exception e) {
            log.error("模型调用失败", e);
        }
    }

    private void addMessage(ChatMessage message) {
        // 添加消息时记录当前时间
        LocalDateTime now = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000);
        message.setCreatedAt(now);
        messageHistory.add(message);
    }

    private String buildRequestJson(String value) {

        // 如果有 Function schema，可加入
        List<Tool> allFunctions = FunctionDefinitionRegistry.getAll();

        try {
            return ModelRequestBuilder.builder().model(model).stream(false).temperature(0.7).messages(messageHistory).tools(allFunctions).toolChoice(value).build().toJson();// 使用tool_choice
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAiResponse(String result) throws Exception {

        JsonNode root = objectMapper.readTree(result);
        JsonNode message = root.path("choices").get(0).path("message");
        JsonNode toolCalls = message.get("tool_calls");

        if (toolCalls != null && toolCalls.isArray()) {
            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setRole("assistant");
            assistantMsg.setContent("");
            assistantMsg.setTool_calls(toolCalls);  // 这里你需要支持 JsonNode 或序列化为字符串
            addMessage(assistantMsg);
            // 遍历每一个函数调用
            for (JsonNode toolCall : toolCalls) {
                String toolCallId = toolCall.path("id").asText();
                String functionName = toolCall.path("function").path("name").asText();
                String argumentsJson = toolCall.path("function").path("arguments").asText();

                log.info("触发函数调用: {}, 参数: {}", functionName, argumentsJson);

                // 调用本地函数
                String functionResult = executeFunction(functionName, argumentsJson);

                // 构造 tool 类型的回复并添加到 messageHistory
                ChatMessage toolReply = new ChatMessage();
                toolReply.setRole("tool");
                toolReply.setTool_call_id(toolCallId); // 这是关键，必须加上！
                toolReply.setName(functionName);
                toolReply.setContent(functionResult);
                addMessage(toolReply);
            }

            // 构造新的请求，让 AI 继续基于函数结果回复
            String newRequest = buildRequestJson("none");
            String newResponse = openAiClient.chatSync(newRequest);
            String finalReply = objectMapper.readTree(newResponse)
                    .path("choices").get(0).path("message").path("content").asText();

            log.info("AI 最终回复: {}", finalReply);
            ChatMessage finalMessage = new ChatMessage("assistant", finalReply);
            addMessage(finalMessage);
        } else {
            // 没有函数调用，直接加回复
            String content = message.path("content").asText();
            log.info("AI 回复: {}", content);
            ChatMessage assistantReply = new ChatMessage("assistant", content);
            addMessage(assistantReply);
        }
    }

    private String executeFunction(String functionName, String argumentsJson) {
        // TODO: 替换为你自己的本地方法映射逻辑
        // 临时演示用
        return switch (functionName) {
            case "getWeather" -> "{\"city\":\"上海\",\"weather\":\"晴天\",\"temperature\":\"26°C\"}";//模拟业务处理返回JSON
            default -> "{\"error\":\"未实现函数: " + functionName + "\"}";
        };
    }

    @Override
    public List<CallMessage> getChatHistory(String chatId) {
        return callMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
    
    @Override
    public void loadChatHistory(String chatId) {
        List<CallMessage> messages = getChatHistory(chatId);
        if (messages != null && !messages.isEmpty()) {
            // 设置当前聊天ID
            this.currentChatId = chatId;
            
            // 清空当前消息历史
            this.messageHistory.clear();
            
            // 将数据库中的消息转换为ChatMessage并添加到历史中
            messages.forEach(msg -> {
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setRole(msg.getRole());
                chatMsg.setContent(msg.getContent());
                chatMsg.setName(msg.getName());
                chatMsg.setTool_call_id(msg.getToolCallId());
                chatMsg.setTool_calls(msg.getToolCalls());
                chatMsg.setCreatedAt(msg.getCreatedAt());
                messageHistory.add(chatMsg);
            });
            
            log.info("已加载 {} 条历史消息，chatId: {}", messages.size(), chatId);
        } else {
            log.warn("未找到chatId为{}的聊天记录", chatId);
        }
    }
    
    @Override
    public void persistChatHistory() {
        if (messageHistory != null && !messageHistory.isEmpty()) {
            // 1. 将消息保存到 call_message 表
            List<CallMessage> entities = messageHistory.stream().map(msg -> {
                CallMessage e = new CallMessage();
                e.setChatId(currentChatId);
                e.setRole(msg.getRole());
                e.setContent(msg.getContent());
                e.setName(msg.getName());
                e.setToolCallId(msg.getTool_call_id());
                e.setToolCalls(msg.getTool_calls());
                e.setCreatedAt(msg.getCreatedAt());
                return e;
            }).collect(Collectors.toList());

            callMessageRepository.saveAll(entities);
            log.info("保存了 {} 条消息到call_message表, chatId: {}", entities.size(), currentChatId);
            
            // 2. 同步保存到 chat_record 和 messages 表
            saveToChatAndMessages();
        }
    }
    
    /**
     * 将对话同步保存到chat_record和messages表
     */
    private void saveToChatAndMessages() {
        // 检查是否有需要同步的消息
        List<ChatMessage> messagesToSync = messageHistory.stream()
                .filter(msg -> ("user".equals(msg.getRole()) || "assistant".equals(msg.getRole())) 
                        && msg.getContent() != null && !msg.getContent().trim().isEmpty())
                .collect(Collectors.toList());
        
        if (messagesToSync.isEmpty()) {
            log.info("没有需要同步到chat_record的消息");
            return;
        }

        // 将过滤后的消息保存到messages表
        for (ChatMessage msg : messagesToSync) {
            chatService.saveMessage(currentChatId, msg.getRole(), msg.getContent(), msg.getCreatedAt());
        }
        
        log.info("已同步 {} 条消息到chat_record(id={})和messages表", messagesToSync.size(), currentChatId);
    }
}
