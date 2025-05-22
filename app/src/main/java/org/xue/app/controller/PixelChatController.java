package org.xue.app.controller;

import jakarta.persistence.EntityNotFoundException;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;
import org.xue.app.service.PixelChatService;
import org.xue.agent.model.AgentResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // For potential simple updates if needed, though sticking to DTO

@RestController
@RequestMapping("/api/pixel_chat")
@CrossOrigin(origins = "*") // As specified, similar to ChatController
@Slf4j
public class PixelChatController {

    private final PixelChatService pixelChatService;
    private static final String VALID_TOKEN = "test-token"; // Authentication token

    @Autowired
    public PixelChatController(PixelChatService pixelChatService) {
        this.pixelChatService = pixelChatService;
    }

    // --- Authentication Helper Methods ---
    private boolean checkAuthentication(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ") && VALID_TOKEN.equals(authHeader.substring(7));
    }

    private ResponseEntity<?> authenticate(String authHeader) {
        if (!checkAuthentication(authHeader)) {
            log.warn("Authentication failed for token: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized: Invalid or missing token"));
        }
        return null; // Indicates authentication success
    }

    // --- Endpoint Methods ---

    @PostMapping("/conversations")
    public ResponseEntity<?> createPixelConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ConversationCreateDTO createDTO) {
        log.info("Received request to create PixelChat conversation with title: {}", createDTO.getTitle());
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            ConversationDTO conversationDTO = pixelChatService.createPixelConversation(createDTO);
            log.info("PixelChat conversation created successfully with ID: {}", conversationDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(conversationDTO);
        } catch (Exception e) {
            log.error("Error creating PixelChat conversation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create conversation: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getAllPixelConversations(@RequestHeader("Authorization") String authHeader) {
        log.info("Received request to get all PixelChat conversations");
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            List<ConversationDTO> conversations = pixelChatService.getAllPixelConversations();
            log.info("Returning {} PixelChat conversations", conversations.size());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error fetching all PixelChat conversations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch conversations: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> getPixelConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        log.info("Received request to get PixelChat conversation with ID: {}", id);
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            ConversationDTO conversationDTO = pixelChatService.getPixelConversation(id);
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
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody ConversationUpdateDTO updateDTO) {
        log.info("Received request to update title for PixelChat conversation ID: {}", id);
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            ConversationDTO conversationDTO = pixelChatService.updatePixelConversationTitle(id, updateDTO);
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
    public ResponseEntity<?> deletePixelConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        log.info("Received request to delete PixelChat conversation with ID: {}", id);
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            pixelChatService.deletePixelConversation(id);
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
    public ResponseEntity<?> getPixelMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        log.info("Received request to get messages for PixelChat conversation ID: {}", id);
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
            List<ChatMessageDTO> messages = pixelChatService.getPixelMessages(id);
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
    public ResponseEntity<?> sendPixelMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatRequestDTO requestDTO) {
        log.info("Received request to send message to PixelChat conversation ID: {}", requestDTO.getConversationId());
        ResponseEntity<?> authResponse = authenticate(authHeader);
        if (authResponse != null) return authResponse;

        try {
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
}

