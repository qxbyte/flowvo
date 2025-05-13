package org.xue.core.functioncall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xue.core.chat.entity.ChatRecord;
import org.xue.core.chat.service.ChatService;
import org.xue.core.functioncall.client.OpenAiClient;
import org.xue.core.functioncall.dto.model.ChatMessage;
import org.xue.core.functioncall.dto.model.Tool;
import org.xue.core.functioncall.entity.CallMessage;
import org.xue.core.functioncall.executor.FunctionDispatcher;
import org.xue.core.functioncall.repository.CallMessageRepository;
import org.xue.core.functioncall.service.FunctionCallService;
import org.xue.core.functioncall.util.FunctionDefinitionRegistry;
import org.xue.core.functioncall.util.ModelRequestBuilder;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
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

    private final OpenAiClient openAiClient;

    private final CallMessageRepository callMessageRepository;

    private final FunctionDispatcher functionDispatcher;

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
//                String functionResult = executeFunction(functionName, argumentsJson);
                String functionResult = functionDispatcher.dispatchFromJson(toolCall);

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

    @Override
    public Flux<String> handleUserQuestionStream(String question, String chatId) {
        log.info("开始处理流式请求: question={}, chatId={}", question, chatId);
        // 如果提供了chatId，则设置当前会话ID
        if (chatId != null && !chatId.isEmpty()) {
            this.currentChatId = chatId;
        } else {
            try {
                // 获取或创建AIPROCESS类型的聊天记录
                ChatRecord chatRecord = chatService.getOrCreateChatRecordByType(DEFAULT_USER_ID, AI_PROCESS_TYPE);
                
                // 检查chatRecord是否为空
                if (chatRecord == null) {
                    log.error("无法创建或获取聊天记录");
                    return Flux.error(new RuntimeException("无法创建聊天记录"));
                }
                
                this.currentChatId = chatRecord.getId();
                log.info("未提供chatId，使用AIPROCESS对话: {}", this.currentChatId);
            } catch (Exception e) {
                log.error("获取或创建聊天记录出错", e);
                return Flux.error(e);
            }
        }
        
        if (this.currentChatId == null || this.currentChatId.isEmpty()) {
            log.error("chatId为空，无法处理请求");
            return Flux.error(new RuntimeException("聊天ID无效"));
        }
        
        // 保存用户消息到数据库
        try {
            chatService.saveMessage(this.currentChatId, "user", question);
            log.info("已保存用户消息到数据库: chatId={}", this.currentChatId);
        } catch (Exception e) {
            log.error("保存用户消息失败", e);
            // 继续处理，不中断流程
        }
        
        // 清空之前的消息历史，防止混淆
        this.messageHistory.clear();
        
        // 添加系统消息和用户输入
        addMessage(new ChatMessage("system", "你是一个助手，如果需要调用函数，请使用 function_call 返回 JSON。"));
        addMessage(new ChatMessage("user", question));

        // 构建流式响应
        return Flux.<String>create(sink -> {
            try {
                // 构建请求 JSON
                String requestJson = buildRequestJson("auto");
                log.info("发送请求到OpenAI: {}", requestJson);
                
                // 添加重试机制处理 SETTINGS preface 错误
                int maxRetries = 2;
                int retryCount = 0;

                while (retryCount <= maxRetries) {
                    try {
                        String result = openAiClient.chatSync(requestJson);
                        log.info("收到OpenAI响应: {}", result);
                        
                        // 处理AI响应
                        JsonNode root = objectMapper.readTree(result);
                        JsonNode message = root.path("choices").get(0).path("message");
                        JsonNode toolCalls = message.get("tool_calls");

                        if (toolCalls != null && toolCalls.isArray()) {
                            // 有函数调用
                            ChatMessage assistantMsg = new ChatMessage();
                            assistantMsg.setRole("assistant");
                            assistantMsg.setContent("");
                            assistantMsg.setTool_calls(toolCalls);
                            addMessage(assistantMsg);
                            
                            // 处理所有函数调用
                            for (JsonNode toolCall : toolCalls) {
                                String toolCallId = toolCall.path("id").asText();
                                String functionName = toolCall.path("function").path("name").asText();
                                String argumentsJson = toolCall.path("function").path("arguments").asText();

                                log.info("触发函数调用: {}, 参数: {}", functionName, argumentsJson);

                                // 调用本地函数
                                String functionResult = functionDispatcher.dispatchFromJson(toolCall);

                                // 构造 tool 类型的回复并添加到 messageHistory
                                ChatMessage toolReply = new ChatMessage();
                                toolReply.setRole("tool");
                                toolReply.setTool_call_id(toolCallId);
                                toolReply.setName(functionName);
                                toolReply.setContent(functionResult);
                                addMessage(toolReply);
                            }

                            // 构造新的请求，让 AI 继续基于函数结果回复
                            int functionMaxRetries = 2;
                            int functionRetryCount = 0;
                            Exception functionLastException = null;
                            
                            while (functionRetryCount <= functionMaxRetries) {
                                try {
                                    String newRequest = buildRequestJson("none");
                                    String newResponse = openAiClient.chatSync(newRequest);
                                    String finalReply = objectMapper.readTree(newResponse)
                                            .path("choices").get(0).path("message").path("content").asText();

                                    log.info("AI 最终回复: {}", finalReply);
                                    ChatMessage finalMessage = new ChatMessage("assistant", finalReply);
                                    addMessage(finalMessage);
                                    
                                    // 将完整回复保存到数据库
                                    try {
                                        chatService.saveMessage(this.currentChatId, "assistant", finalReply);
                                    } catch (Exception e) {
                                        log.error("保存AI回复到数据库失败", e);
                                    }
                                    
                                    // 发送纯文本内容
                                    sink.next(finalReply);
                                    log.info("已发送AI回复");
                                    break; // 成功则跳出循环
                                } catch (IOException e) {
                                    functionLastException = e;
                                    functionRetryCount++;
                                    
                                    // 特别处理HTTP/2和SETTINGS错误
                                    if (e.getMessage() != null && 
                                        (e.getMessage().contains("SETTINGS") || 
                                         e.getMessage().contains("Connection reset") ||
                                         e.getMessage().contains("timeout"))) {
                                        log.warn("函数调用阶段检测到HTTP/2协议错误，尝试重试 ({}/{})", 
                                                functionRetryCount, functionMaxRetries);
                                        
                                        if (functionRetryCount <= functionMaxRetries) {
                                            // 指数退避策略
                                            int sleepTime = functionRetryCount * 2000; // 2秒, 4秒
                                            log.info("{}毫秒后重试函数调用请求", sleepTime);
                                            Thread.sleep(sleepTime);
                                            continue;
                                        }
                                    }
                                    
                                    if (functionRetryCount > functionMaxRetries) {
                                        throw e; // 重试次数用尽，抛出异常
                                    }
                                    
                                    // 其他错误也重试
                                    log.warn("函数调用阶段发生IO异常，尝试重试 ({}/{}): {}", 
                                            functionRetryCount, functionMaxRetries, e.getMessage());
                                    Thread.sleep(functionRetryCount * 1000);
                                } catch (Exception e) {
                                    functionLastException = e;
                                    functionRetryCount++;
                                    
                                    if (functionRetryCount > functionMaxRetries) {
                                        throw e; // 重试次数用尽，抛出异常
                                    }
                                    
                                    log.warn("函数调用阶段发生异常，尝试重试 ({}/{}): {}", 
                                            functionRetryCount, functionMaxRetries, e.getMessage());
                                    Thread.sleep(functionRetryCount * 1000);
                                }
                            }
                            
                            // 如果所有重试都失败了
                            if (functionRetryCount > functionMaxRetries && functionLastException != null) {
                                throw functionLastException;
                            }
                        } else {
                            // 没有函数调用，直接返回回复
                            String content = message.path("content").asText();
                            log.info("AI 回复: {}", content);
                            ChatMessage assistantReply = new ChatMessage("assistant", content);
                            addMessage(assistantReply);
                            
                            // 将完整回复保存到数据库
                            try {
                                chatService.saveMessage(this.currentChatId, "assistant", content);
                            } catch (Exception e) {
                                log.error("保存AI回复到数据库失败", e);
                            }
                            
                            // 发送纯文本内容
                            sink.next(content);
                            log.info("已发送AI回复");
                        }
                        
                        // 如果成功处理，跳出重试循环
                        break;
                        
                    } catch (NoRouteToHostException e) {
                        log.error("无法连接到OpenAI服务", e);
                        String errorMsg = "无法连接到AI服务，请检查网络连接或代理设置。";
                        sink.next(errorMsg);
                        // 保存错误消息
                        chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                        break;
                    } catch (ConnectException e) {
                        log.error("连接OpenAI服务失败", e);
                        String errorMsg = "连接AI服务失败，可能是网络问题或服务暂时不可用。";
                        sink.next(errorMsg);
                        // 保存错误消息
                        chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                        break;

                    } catch (SocketTimeoutException e) {
                        log.error("连接OpenAI服务超时", e);
                        String errorMsg = "连接AI服务超时，请稍后重试。";
                        sink.next(errorMsg);
                        // 保存错误消息
                        chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                        break;
                    } catch (IOException e) {
                        retryCount++;

                        // 特别处理HTTP/2和SETTINGS错误
                        if (e.getMessage() != null &&
                            (e.getMessage().contains("SETTINGS") ||
                             e.getMessage().contains("Connection reset") ||
                             e.getMessage().contains("timeout"))) {
                            log.warn("检测到HTTP/2协议错误或连接重置，尝试重试 ({}/{})", retryCount, maxRetries);

                            if (retryCount <= maxRetries) {
                                // 指数退避策略
                                int sleepTime = retryCount * 3000; // 3秒, 6秒, 9秒...
                                log.info("{}毫秒后重试请求", sleepTime);
                                Thread.sleep(sleepTime);
                                continue;
                            }
                        }

                        if (retryCount > maxRetries) {
                            log.error("重试次数已用完，处理IO异常:", e);
                            String errorMsg = "连接AI服务失败，请稍后重试。";
                            sink.next(errorMsg);
                            chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                            break;
                        }

                        // 其他IO错误
                        log.warn("请求发生IO异常，尝试重试 ({}/{}): {}", retryCount, maxRetries, e.getMessage());
                        Thread.sleep(retryCount * 1500);
                    } catch (Exception e) {
                        retryCount++;
                        
                        if (retryCount > maxRetries) {
                            log.error("重试次数已用完，处理异常:", e);
                            String errorMsg = "处理您的请求时出错，请稍后重试。";
                            sink.next(errorMsg);
                            chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                            break;
                        }
                        
                        log.warn("处理请求时发生异常，尝试重试 ({}/{}): {}", retryCount, maxRetries, e.getMessage());
                        Thread.sleep(retryCount * 1500);
                    }
                }
                
                // 发送结束标记
                sink.next("[DONE]");
                log.info("已发送结束标记");
                sink.complete();
            } catch (Exception e) {
                log.error("流式处理总体出错", e);
                // 尝试向客户端发送错误消息，然后完成流
                try {
                    String errorMsg = "服务器处理请求出错，请稍后重试。";
                    sink.next(errorMsg);
                    sink.next("[DONE]");
                    sink.complete();
                    
                    // 保存错误消息到数据库
                    chatService.saveMessage(this.currentChatId, "assistant", errorMsg);
                } catch (Exception ignored) {
                    // 如果连这个也失败了，那就只能error了
                    sink.error(e);
                }
            }
        });
    }
}
