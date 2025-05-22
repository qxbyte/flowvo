package org.xue.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.agent.model.AgentResponse;
import org.xue.app.dto.*;
import org.xue.app.service.ChatService;
import org.xue.app.service.impl.ChatServiceImpl;

import java.util.List;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ChatService chatService;
    
    /**
     * 检查认证信息
     * @param authHeader 认证头
     * @return 如果认证有效返回true，否则返回false
     */
    private boolean checkAuthentication(String authHeader) {
        log.info("收到认证请求, header: {}", authHeader);
        
        // 开发环境中简化认证，任何包含test-token的都通过
        if (authHeader == null) {
            log.info("认证头为空，在开发环境中默认允许访问");
            return true; // 开发环境允许空认证头
        }
        
        if (authHeader.contains("test-token")) {
            log.info("发现test-token，认证成功");
            return true;
        }
        
        log.warn("认证失败: {}", authHeader);
        return false;
    }
    
    /**
     * 统一处理认证
     * @param authHeader 认证头
     * @return 如果认证失败返回401响应，否则返回null
     */
    private <T> ResponseEntity<T> authenticate(String authHeader) {
        if (!checkAuthentication(authHeader)) {
            log.warn("认证失败，返回401: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("认证成功，继续处理请求");
        return null;
    }
    
    /**
     * 创建对话
     */
    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ConversationCreateDTO createDTO) {
        
        log.info("收到创建对话请求: {}, 认证头: {}", createDTO, authHeader);
        ResponseEntity<ConversationDTO> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        ConversationDTO conversation = chatService.createConversation(createDTO);
        log.info("对话创建成功: {}", conversation.getId());
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationDTO> getConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        
        log.info("收到获取对话请求: {}, 认证头: {}", id, authHeader);
        ResponseEntity<ConversationDTO> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        ConversationDTO conversation = chatService.getConversation(id);
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * 更新对话（重命名）
     */
    @PutMapping("/conversations/{id}")
    public ResponseEntity<ConversationDTO> updateConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id, 
            @RequestBody ConversationUpdateDTO updateDTO) {
        
        log.info("收到更新对话请求: {}, 数据: {}, 认证头: {}", id, updateDTO, authHeader);
        ResponseEntity<ConversationDTO> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        ConversationDTO updatedConversation = chatService.updateConversation(id, updateDTO);
        log.info("对话更新成功: {}", id);
        return ResponseEntity.ok(updatedConversation);
    }
    
    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        
        log.info("收到删除对话请求: {}, 认证头: {}", id, authHeader);
        ResponseEntity<Void> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        chatService.deleteConversation(id);
        log.info("对话删除成功: {}", id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取对话中的消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        
        log.info("收到获取消息列表请求: {}, 认证头: {}", id, authHeader);
        ResponseEntity<List<ChatMessageDTO>> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        List<ChatMessageDTO> messages = chatService.getMessages(id);
        log.info("成功获取消息列表: {}, 消息数量: {}", id, messages.size());
        return ResponseEntity.ok(messages);
    }
    
    /**
     * 获取所有对话
     */
    @GetMapping("/conversations")
    public ResponseEntity<ConversationListDTO> getConversations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "source", required = false) String source) {
        
        log.info("收到获取对话请求, 来源: {}, 认证头: {}", source, authHeader);
        ResponseEntity<ConversationListDTO> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        // 根据来源获取对话
        List<ConversationDTO> conversations;
        if (source != null && !source.isEmpty()) {
            conversations = chatService.getConversationsBySource(source);
            log.info("按来源({})获取对话, 对话数量: {}", source, conversations.size());
        } else {
            conversations = chatService.getAllConversations();
            log.info("获取所有对话, 对话数量: {}", conversations.size());
        }
        
        // 构建响应
        ConversationListDTO response = new ConversationListDTO();
        response.setItems(conversations);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送消息并获取回复
     */
    @PostMapping("/send")
    public ResponseEntity<AgentResponse> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ChatRequestDTO requestDTO) {
        
        log.info("收到发送消息请求: {}, 认证头: {}", requestDTO, authHeader);
        ResponseEntity<AgentResponse> authResult = authenticate(authHeader);
        if (authResult != null) return authResult;
        
        // 参数校验
        if (requestDTO.getMessage() == null || requestDTO.getMessage().trim().isEmpty()) {
            log.warn("发送消息失败: 用户问题为空");
            return ResponseEntity.badRequest().body(AgentResponse.error("用户问题不能为空"));
        }
        
        AgentResponse response = chatService.sendMessage(requestDTO);
        log.info("消息发送成功，状态: {}", response.getStatus());
        return ResponseEntity.ok(response);
    }
} 