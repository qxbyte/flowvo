package org.xue.assistant.functioncall.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.assistant.chat.entity.ChatRecord;
import org.xue.assistant.chat.service.ChatService;
import org.xue.assistant.entity.User;
import org.xue.assistant.functioncall.service.FunctionCallService;
import org.xue.assistant.service.UserService;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/function-call")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FunctionCallController {

    private final FunctionCallService functionCallService;
    private final ChatService chatService;
    private final UserService userService;

    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("未授权的访问");
        }

        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    @PostMapping("/invoke")
    @CrossOrigin(origins = "*")
    public void invokeFunction(@RequestParam String question) {
        log.info("收到同步调用请求: {}", question);
        functionCallService.handleUserQuestion(question);
    }
    
    /**
     * 创建新的聊天会话或获取已有的AIPROCESS类型对话
     */
    @GetMapping("/create-chat")
    @CrossOrigin(origins = "*")
    public Map<String, String> createChat() {
        // 获取当前登录用户，如果未登录会抛出异常
        User user = getCurrentUser();
        log.info("收到创建聊天请求");
        
        // 获取或创建AIPROCESS类型的聊天记录
        String userId = user.getId().toString();
        String type = "AIPROCESS"; // 默认类型
        
        try {
            ChatRecord chatRecord = chatService.getOrCreateChatRecordByType(userId, type);
            if (chatRecord == null) {
                log.error("创建聊天记录失败，返回空记录");
                throw new RuntimeException("创建聊天记录失败");
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("id", chatRecord.getId());
            log.info("创建聊天成功，返回ID: {}", chatRecord.getId());
            return response;
        } catch (Exception e) {
            log.error("创建聊天记录时出错", e);
            throw new RuntimeException("创建聊天记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户的所有AIPROCESS类型对话记录
     */
    @GetMapping("/user-chats")
    @CrossOrigin(origins = "*")
    public List<Map<String, String>> getUserChats() {
        // 获取当前登录用户，如果未登录会抛出异常
        User user = getCurrentUser();
        log.info("获取用户AIPROCESS对话记录");
        
        // 获取用户的AIPROCESS类型聊天记录
        String userId = user.getId().toString();
        String type = "AIPROCESS"; // 默认类型
        
        try {
            List<ChatRecord> chatRecords = chatService.getChatRecordsByUserIdAndType(userId, type);
            
            // 转换为前端需要的格式
            return chatRecords.stream()
                .map(record -> {
                    Map<String, String> chat = new HashMap<>();
                    chat.put("id", record.getId());
                    chat.put("title", StringUtils.isNotBlank(record.getTitle()) ? record.getTitle() : "AI对话");
                    chat.put("createTime", record.getCreateTime().toString());
                    return chat;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户对话记录失败", e);
            throw new RuntimeException("获取用户对话记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定对话的历史消息
     */
    @GetMapping("/chat-history")
    @CrossOrigin(origins = "*")
    public List<Map<String, String>> getChatHistory(@RequestParam String chatId) {
        // 获取当前登录用户，如果未登录会抛出异常
        User user = getCurrentUser();
        log.info("获取对话历史消息: {}", chatId);
        
        try {
            // 验证对话是否属于当前用户
            String userId = user.getId().toString();
            ChatRecord chatRecord = chatService.getChatRecordByIdAndUserId(chatId, userId);
            
            if (chatRecord == null) {
                log.error("对话不存在或不属于当前用户");
                throw new RuntimeException("对话不存在或不属于当前用户");
            }
            
            // 从Messages表获取对话历史消息，而不是从CallMessage表
            return chatService.getMessagesByChatId(chatId).stream()
                .map(message -> {
                    Map<String, String> msg = new HashMap<>();
                    msg.put("role", message.getRole());
                    msg.put("content", message.getContent());
                    return msg;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取对话历史消息失败", e);
            throw new RuntimeException("获取对话历史消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 流式响应的函数调用接口
     */
    @GetMapping(value = "/invoke-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public Flux<String> invokeFunctionStream(
        @RequestParam String question, 
        @RequestParam(required = false) String chatId
    ) {
        log.info("收到流式调用请求: question={}, chatId={}", question, chatId);
        // 获取当前登录用户，如果未登录会抛出异常
        User user = getCurrentUser();
        // 获取或创建AIPROCESS类型的聊天记录
        String userId = user.getId().toString();
        String type = "AIPROCESS"; // 默认类型
        if (StringUtils.isEmpty(chatId)) {
            ChatRecord chatRecord = chatService.getOrCreateChatRecordByType(userId, type);
            if (chatRecord == null) {
                log.error("创建聊天记录失败，返回空记录");
                throw new RuntimeException("创建聊天记录失败");
            }
            chatId = chatRecord.getId();
        }

        return functionCallService.handleUserQuestionStream(question, chatId);
    }
}

