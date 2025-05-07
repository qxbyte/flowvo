package org.xue.assistant.functioncall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xue.assistant.chat.service.ChatService;
import org.xue.assistant.functioncall.client.OpenAiClient;
import org.xue.assistant.functioncall.dto.model.ChatMessage;
import org.xue.assistant.functioncall.dto.model.Tool;
import org.xue.assistant.functioncall.service.FunctionCallService;
import org.xue.assistant.functioncall.util.FunctionDefinitionRegistry;
import org.xue.assistant.functioncall.util.ModelRequestBuilder;

import java.util.ArrayList;
import java.util.List;

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

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Autowired
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatService chatService;


    // 多轮对话消息上下文（可替换为数据库或 Redis）
    private final List<ChatMessage> messageHistory = new ArrayList<>();

    @Override
    public void handleUserQuestion(String question) {
        // 添加用户输入
        messageHistory.add(new ChatMessage("system", "你是一个助手，如果需要调用函数，请使用 function_call 返回 JSON。"));
        messageHistory.add(new ChatMessage("user", question));

//        String redisKey = "chat:history:" + chatId;
//        redisTemplate.opsForList().rightPush(redisKey, new ChatMessage("system", "你是一个助手，如果需要调用函数，请使用 function_call 返回 JSON。，请务必要按照以下格式返回：{\"function_call\": {\"name\": \"getWeather\",\"arguments\": {\"city\": \"上海\"}}}"));

        // 构建请求 JSON
        String requestJson = buildRequestJson("auto");

        try {
            String result = openAiClient.chatSync(requestJson);
            handleAiResponse(result);
        } catch (Exception e) {
            log.error("模型调用失败", e);
        }
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
            messageHistory.add(assistantMsg);
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
                messageHistory.add(toolReply);
            }

            // 构造新的请求，让 AI 继续基于函数结果回复
            String newRequest = buildRequestJson("none");
            String newResponse = openAiClient.chatSync(newRequest);
            String finalReply = objectMapper.readTree(newResponse)
                    .path("choices").get(0).path("message").path("content").asText();

            log.info("AI 最终回复: {}", finalReply);
            messageHistory.add(new ChatMessage("assistant", finalReply));
        } else {
            // 没有函数调用，直接加回复
            String content = message.path("content").asText();
            log.info("AI 回复: {}", content);
            messageHistory.add(new ChatMessage("assistant", content));
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

//    private CallMessageRepository callMessageRepository;
//
//    public void persistChatHistory(Long chatId) {
//        String redisKey = "chat:history:" + chatId;
//        List<ChatMessage> history = redisTemplate.opsForList().range(redisKey, 0, -1);
//
//        if (history != null && !history.isEmpty()) {
//            List<CallMessage> entities = history.stream().map(msg -> {
//                CallMessage e = new CallMessage();
//                e.setChatId(chatId);
//                e.setRole(msg.getRole());
//                e.setContent(msg.getContent());
//                e.setName(msg.getName());
//                e.setToolCallId(msg.getToolCallId());
//                if (msg.getToolCalls() != null) {
//                    e.setToolCalls(msg.getToolCalls());
//                }
//                e.setCreatedAt(LocalDateTime.now()); // 或者你记录 msg 的时间
//                return e;
//            }).collect(Collectors.toList());
//
//            callMessageRepository.saveAll(entities);  // 假设你用 JPA/MyBatis-plus
//            redisTemplate.delete(redisKey); // 删除缓存
//        }
//    }

}
