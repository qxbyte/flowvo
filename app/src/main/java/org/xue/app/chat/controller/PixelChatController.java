package org.xue.app.chat.controller;

import jakarta.persistence.EntityNotFoundException;
import org.xue.app.chat.service.PixelChatService;
import org.xue.app.controller.BaseController;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;
import org.xue.agent.model.AgentResponse;
import org.xue.agent.client.core.McpClientTemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // For potential simple updates if needed, though sticking to DTO
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pixel_chat")
@CrossOrigin(origins = "*") // As specified, similar to ChatController
@Slf4j
public class PixelChatController extends BaseController {

    private final PixelChatService pixelChatService;
    private final McpClientTemplate mcpClientTemplate;

    @Autowired
    public PixelChatController(PixelChatService pixelChatService, McpClientTemplate mcpClientTemplate) {
        this.pixelChatService = pixelChatService;
        this.mcpClientTemplate = mcpClientTemplate;
    }

    // --- Endpoint Methods ---

    @PostMapping("/conversations")
    public ResponseEntity<?> createPixelConversation(@RequestBody ConversationCreateDTO createDTO) {
        log.info("Received request to create PixelChat conversation with title: {}", createDTO.getTitle());

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法创建对话");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            // 设置当前用户ID和source
            createDTO.setUserId(currentUserId);
            createDTO.setSource("chat");
            
            ConversationDTO conversationDTO = pixelChatService.createPixelConversation(createDTO);
            log.info("PixelChat conversation created successfully with ID: {}", conversationDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(conversationDTO);
        } catch (Exception e) {
            log.error("Error creating PixelChat conversation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create conversation: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getAllPixelConversations() {
        log.info("Received request to get all PixelChat conversations");

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法获取对话列表");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            List<ConversationDTO> conversations = pixelChatService.getAllPixelConversations(currentUserId);
            log.info("Returning {} PixelChat conversations", conversations.size());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error fetching all PixelChat conversations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch conversations: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> getPixelConversation(@PathVariable String id) {
        log.info("Received request to get PixelChat conversation with ID: {}", id);

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法获取对话详情");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            ConversationDTO conversationDTO = pixelChatService.getPixelConversation(id, currentUserId);
            log.info("Returning PixelChat conversation with ID: {}", id);
            return ResponseEntity.ok(conversationDTO);
        } catch (EntityNotFoundException e) {
            log.warn("PixelChat conversation not found with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching PixelChat conversation with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch conversation: " + e.getMessage()));
        }
    }

    @PutMapping("/conversations/{id}/title")
    public ResponseEntity<?> updatePixelConversationTitle(
            @PathVariable String id,
            @RequestBody ConversationUpdateDTO updateDTO) {
        log.info("Received request to update title for PixelChat conversation ID: {}", id);

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法更新对话标题");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            ConversationDTO conversationDTO = pixelChatService.updatePixelConversationTitle(id, updateDTO, currentUserId);
            log.info("PixelChat conversation title updated for ID: {}", id);
            return ResponseEntity.ok(conversationDTO);
        } catch (EntityNotFoundException e) {
            log.warn("PixelChat conversation not found for title update with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating title for PixelChat conversation ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update conversation title: " + e.getMessage()));
        }
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<?> deletePixelConversation(@PathVariable String id) {
        log.info("Received request to delete PixelChat conversation with ID: {}", id);

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法删除对话");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            pixelChatService.deletePixelConversation(id, currentUserId);
            log.info("PixelChat conversation deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("PixelChat conversation not found for deletion with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting PixelChat conversation with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to delete conversation: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getPixelMessages(@PathVariable String id) {
        log.info("Received request to get messages for PixelChat conversation ID: {}", id);

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法获取消息列表");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            List<ChatMessageDTO> messages = pixelChatService.getPixelMessages(id, currentUserId);
            log.info("Returning {} messages for PixelChat conversation ID: {}", messages.size(), id);
            return ResponseEntity.ok(messages);
        } catch (EntityNotFoundException e) {
            log.warn("PixelChat conversation not found for fetching messages, ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching messages for PixelChat conversation ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch messages: " + e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendPixelMessage(@RequestBody ChatRequestDTO requestDTO) {
        log.info("Received request to send message to PixelChat conversation ID: {}", requestDTO.getConversationId());

        try {
            // 获取当前登录用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法发送消息");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            // 设置用户ID到请求中
            requestDTO.setUserId(currentUserId);
            
            AgentResponse agentResponse = pixelChatService.sendPixelMessage(requestDTO);
            log.info("Message sent successfully to PixelChat conversation ID: {}", requestDTO.getConversationId());
            return ResponseEntity.ok(agentResponse);
        } catch (EntityNotFoundException e) {
            log.warn("PixelChat conversation not found for sending message, ID {}: {}", requestDTO.getConversationId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error sending message to PixelChat conversation ID {}: {}", requestDTO.getConversationId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/agents")
    public ResponseEntity<?> getAvailableAgents() {
        log.info("Received request to get available MCP agents");

        try {
            // 获取MCP服务状态
            Map<String, Map<String, Object>> serversStatus = mcpClientTemplate.getServersStatus();
            
            // 过滤出连接的服务并提取服务名称
            List<Map<String, Object>> agents = serversStatus.entrySet().stream()
                    .filter(entry -> {
                        Map<String, Object> status = entry.getValue();
                        return Boolean.TRUE.equals(status.get("connected"));
                    })
                    .map(entry -> {
                        Map<String, Object> agent = Map.of(
                                "name", entry.getKey(),
                                "displayName", entry.getKey().replace("mcp-", "").toUpperCase(),
                                "status", "connected"
                        );
                        return agent;
                    })
                    .collect(Collectors.toList());
            
            log.info("Returning {} available agents", agents.size());
            return ResponseEntity.ok(agents);
            
        } catch (Exception e) {
            log.error("Error fetching available agents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch agents: " + e.getMessage()));
        }
    }
}

