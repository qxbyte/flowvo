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
import java.util.Map;

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

