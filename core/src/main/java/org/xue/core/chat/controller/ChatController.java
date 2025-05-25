package org.xue.core.chat.controller;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.api.milvus.dto.SearchChunksRequest;
import org.xue.core.chat.entity.ChatRecord;
import org.xue.core.chat.entity.Messages;
import org.xue.core.client.feign.MilvusFeign;
import org.xue.core.entity.User;
import org.xue.core.chat.service.AIService;
import org.xue.core.chat.service.ChatService;
import org.xue.core.service.UserService;
import org.xue.core.milvus.service.ChunkMilvusService;
import reactor.core.publisher.Flux;
import org.xue.core.config.PromptsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    private final AIService aiService;

    private final UserService userService;

    private final ChunkMilvusService milvusService;

    private final MilvusFeign milvusFeign;
    
    private final PromptsService promptsService;

    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("未授权的访问");
        }
        
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    // 获取所有聊天记录
    @GetMapping("/records")
    public List<Map<String, Object>> getChatList() {
        User user = getCurrentUser();
        String type = "CHAT"; // 默认类型
        List<ChatRecord> records = chatService.getChatRecordsByUserIdAndType(user.getId().toString(), type);
        return records.stream()
            .map(record -> {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("title", record.getTitle());
                recordMap.put("createTime", record.getCreateTime());
                return recordMap;
            })
            .collect(Collectors.toList());
    }

    // 获取某个聊天记录的消息
    @GetMapping("/{id}")
    public List<Messages> getMessagesByChatId(@PathVariable String id) {
        User user = getCurrentUser();
        
        // 验证聊天记录是否属于当前用户
        ChatRecord record = chatService.getChatRecordById(id);
        if (record == null || !user.getId().toString().equals(record.getUserId())) {
            throw new SecurityException("无权访问该聊天记录");
        }
        
        return chatService.getMessagesByChatId(id);
    }

    // 创建新对话
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createNewChat() {
        User user = getCurrentUser();

        String type = "CHAT";
        // 设置用户ID
        ChatRecord newRecord = chatService.createNewChatRecordwithType(user.getId().toString(), type);

        Map<String, String> response = new HashMap<>();
        response.put("id", newRecord.getId());
        return ResponseEntity.ok(response);
    }

    // 发送消息并生成回复
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(
        @RequestParam("chatId") String chatId,  // 修改为 String 类型
        @RequestParam("message") String message
    ) {
        // 保存用户消息
        chatService.saveMessage(chatId, "user", message); // 保存用户消息到数据库

        // 获取 AI 回复
        String aiReply = aiService.getAiReply(message);

        // 保存 AI 回复
        chatService.saveMessage(chatId, "AI", aiReply); // 保存 AI 回复到数据库

        // 返回 AI 回复
        Map<String, String> response = new HashMap<>();
        response.put("response", aiReply);

        return ResponseEntity.ok(response);
    }
    // 使用流式返回生成的 AI 回复
    @PostMapping("/sendStream")
    public Flux<String> generateStream(
        @RequestParam("message") String message,
        @RequestParam("chatId") String chatId  // 添加chatId参数
    ) {
        log.info("收到流式请求: {}, chatId: {}", message, chatId);
        
        try {
            // 验证用户是否有权限访问此聊天
            User user = getCurrentUser();
            
            // 验证聊天记录是否属于当前用户
            ChatRecord record = chatService.getChatRecordById(chatId);
            if (record == null || !user.getId().toString().equals(record.getUserId())) {
                return Flux.error(new SecurityException("无权访问该聊天记录"));
            }
            
            // 先保存用户消息
            chatService.saveMessage(chatId, "user", message);
    
            // ==== 1. 检索向量库知识 ====
            List<String> retrievedChunks = new ArrayList<>();
            try {
                // 尝试从向量库检索相关知识
                log.info("开始调用Milvus向量库，查询内容：'{}'，预期返回{}条结果", 
                         message.substring(0, Math.min(30, message.length())) + "...", 2);
                retrievedChunks = milvusFeign.searchChunks(new SearchChunksRequest(message, 2));
                log.info("向量检索成功，获取到{}条相关资料", retrievedChunks.size());
            } catch (feign.FeignException.BadGateway e) {
                log.error("向量库服务不可用(502错误): {}, 异常类型: {}", e.getMessage(), e.getClass().getName());
                // 添加友好提示
                retrievedChunks.add("很抱歉，知识库检索服务暂时不可用(502错误)，但我会尽力回答您的问题。");
            } catch (feign.FeignException e) {
                log.error("向量检索Feign异常: 状态码={}, 消息={}", e.status(), e.getMessage());
                // 添加友好提示
                retrievedChunks.add("知识库检索服务返回错误(状态码:" + e.status() + ")，将使用基础知识回答您的问题。");
            } catch (Exception e) {
                log.error("向量检索失败: {}, 异常类型: {}", e.getMessage(), e.getClass().getName(), e);
                // 添加友好提示
                retrievedChunks.add("向量检索服务遇到问题，无法提供知识库参考，但我会尽力回答您的问题。");
            }
            
            // ==== 2. 拼接prompt ====
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("【以下是相关资料，可参考作答】\n");
            for (String chunk : retrievedChunks) {
                promptBuilder.append(chunk).append("\n");
            }
            promptBuilder.append("【用户提问】\n").append(message);
    
            String finalPromptText = promptsService.getChatKnowledgeBasePrompt(
                String.join("\n", retrievedChunks), 
                message
            );
    
            // 创建 StringBuilder 来收集完整的 AI 响应
            StringBuilder fullResponse = new StringBuilder();
            
            Prompt prompt = new Prompt(new UserMessage(finalPromptText));
            
            // 移除timeout和retry，允许流自然结束
            return aiService.getChatStream(prompt)
                .map(text -> {
                    // 确保文本不为空
                    if (text == null) return "";
                    
                    // 将文本添加到完整响应
                    fullResponse.append(text);
                    
                    // 确保返回的文本不包含data:前缀
                    if (text.startsWith("data:")) {
                        text = text.substring(5).trim();
                    }
                    
                    return text;
                })
                .doOnComplete(() -> {
                    // 流结束时保存 AI 回复到数据库
                    String response = fullResponse.toString();
                    if (response.length() > 0) {
                        try {
                            chatService.saveMessage(chatId, "assistant", response);
                            log.info("对话完成，消息已保存，长度: {}", response.length());
                        } catch (Exception e) {
                            log.error("保存AI回复失败: {}", e.getMessage(), e);
                        }
                    }
                });
        } catch (SecurityException e) {
            log.error("访问无权限的聊天记录: {}", e.getMessage());
            return Flux.error(new SecurityException(e.getMessage()));
        } catch (Exception e) {
            log.error("处理流式请求时发生错误: {}", e.getMessage(), e);
            return Flux.error(e);
        }
    }



    @GetMapping("/sendStream-test")
    public Flux<String> generateStreamTest(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return aiService.getChatStream(prompt); // 使用流式处理生成回复
    }

    // 删除聊天记录
    // 重命名聊天记录
    @PostMapping("/{id}/rename")
    public ResponseEntity<Void> renameChatRecord(
        @PathVariable String id,
        @RequestBody Map<String, String> payload
    ) {
        String newTitle = payload.get("title");
        chatService.renameChatRecord(id, newTitle);
        return ResponseEntity.ok().build();
    }

    // 删除聊天记录
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRecord(@PathVariable String id) {
        chatService.deleteChatRecord(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveMessage(@RequestBody Map<String, String> payload) {
        String chatId = payload.get("chatId");
        String role = payload.get("role");
        String content = payload.get("content");
        
        chatService.saveMessage(chatId, role, content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/saveMessages")
    public ResponseEntity<Void> saveMessages(@RequestBody Map<String, Object> payload) {
        String chatId = (String) payload.get("chatId");
        List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");
        
        for (Map<String, String> message : messages) {
            chatService.saveMessage(
                chatId,
                message.get("role"),
                message.get("content")
            );
        }
        
        return ResponseEntity.ok().build();
    }
}
