package org.xue.app.chat.controller;

import jakarta.persistence.EntityNotFoundException;
import org.xue.app.chat.service.PixelChatService;
import org.xue.app.controller.BaseController;
import org.xue.app.dto.ChatMessageDTO;
import org.xue.app.dto.ChatRequestDTO;
import org.xue.app.dto.ConversationCreateDTO;
import org.xue.app.dto.ConversationDTO;
import org.xue.app.dto.ConversationUpdateDTO;
import org.xue.app.dto.FileUploadResponseDTO;
import org.xue.app.service.FileAttachmentService;
import org.xue.app.chat.service.VisionService;
import org.xue.app.chat.service.ModelConfigService;
import org.xue.app.chat.dto.VisionRequestDTO;
import org.xue.app.chat.dto.VisionResponseDTO;
import org.xue.agent.model.AgentResponse;
import org.xue.agent.client.core.McpClientTemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    
    @Autowired(required = false)  // 设置为可选，避免服务未实现时报错
    private FileAttachmentService fileAttachmentService;

    @Autowired(required = false)  // 设置为可选，避免服务未实现时报错
    private VisionService visionService;

    @Autowired(required = false)  // 设置为可选，避免服务未实现时报错
    private ModelConfigService modelConfigService;

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

    /**
     * 上传单个文件
     *
     * @param file 上传的文件
     * @param conversationId 对话ID（可选）
     * @return 文件上传响应
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
            }

            // 检查FileAttachmentService是否可用
            if (fileAttachmentService == null) {
                log.warn("FileAttachmentService not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "文件上传服务暂不可用"));
            }

            String userId = getCurrentUserId();
            if (userId == null) {
                log.warn("用户未登录，无法上传文件");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }

            FileUploadResponseDTO response = fileAttachmentService.uploadFile(file, conversationId, userId);
            log.info("文件上传成功: {}", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 上传的文件列表
     * @param conversationId 对话ID（可选）
     * @return 文件上传响应列表
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件列表不能为空"));
            }

            // 检查FileAttachmentService是否可用
            if (fileAttachmentService == null) {
                log.warn("FileAttachmentService not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "文件上传服务暂不可用"));
            }

            String userId = getCurrentUserId();
            if (userId == null) {
                log.warn("用户未登录，无法上传文件");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }

            List<FileUploadResponseDTO> responses = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    FileUploadResponseDTO response = fileAttachmentService.uploadFile(file, conversationId, userId);
                    responses.add(response);
                }
            }
            
            log.info("批量文件上传成功，共上传 {} 个文件", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "批量文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 图像识别接口 - 集成到聊天功能中
     *
     * @param imageFile 图像文件
     * @param message 用户消息（可选）
     * @param model 使用的模型（可选，默认gpt-4o-mini）
     * @param conversationId 对话ID（可选）
     * @return 识别结果
     */
    @PostMapping("/vision/recognize")
    public ResponseEntity<?> recognizeImageInChat(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        
        try {
            log.info("收到聊天中的图像识别请求: 文件={}, 大小={}bytes, 对话ID={}", 
                imageFile.getOriginalFilename(), imageFile.getSize(), conversationId);
            
            // 检查用户权限
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                log.warn("用户未登录，无法进行图像识别");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "用户未登录"));
            }
            
            // 验证文件
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "图像文件不能为空"));
            }
            
            // 检查VisionService是否可用
            if (visionService == null) {
                log.warn("VisionService not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "图像识别服务暂不可用"));
            }
            
            // 快速验证图像文件
            if (!visionService.isImageSupported(imageFile)) {
                return ResponseEntity.badRequest().body(Map.of("error", "图像文件格式不支持或文件过大"));
            }
            
            // 构建请求DTO
            VisionRequestDTO requestDTO = VisionRequestDTO.builder()
                .conversationId(conversationId)
                .message(message != null ? message : "请分析这张图片的内容。")
                .model(model != null ? model : "gpt-4o-mini")
                .fileName(imageFile.getOriginalFilename())
                .mimeType(imageFile.getContentType())
                .fileSize(imageFile.getSize())
                .build();
            
            // 调用服务进行识别
            VisionResponseDTO response = visionService.recognizeImage(imageFile, requestDTO);
            
            if (response.isSuccess()) {
                log.info("聊天中图像识别成功: {}", imageFile.getOriginalFilename());
                
                // 封装成聊天API格式的响应
                Map<String, Object> chatResponse = Map.of(
                    "assistantReply", response.getContent(),
                    "model", response.getModel(),
                    "imageInfo", response.getImageInfo(),
                    "success", true
                );
                
                return ResponseEntity.ok(chatResponse);
            } else {
                log.warn("聊天中图像识别失败: {}", response.getError());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", response.getError(),
                    "success", false
                ));
            }
            
        } catch (Exception e) {
            log.error("聊天中图像识别接口异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "图像识别服务异常: " + e.getMessage()));
        }
    }

    /**
     * 获取可用的AI模型列表
     *
     * @return 模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<?> getAvailableModels() {
        try {
            log.info("获取可用模型列表");
            
            if (modelConfigService == null) {
                log.warn("ModelConfigService not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "模型配置服务暂不可用"));
            }
            
            List<Map<String, Object>> models = modelConfigService.getFlatModels();
            log.info("返回 {} 个可用模型", models.size());
            
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取模型列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取支持Vision的模型列表
     *
     * @return Vision支持的模型列表
     */
    @GetMapping("/models/vision")
    public ResponseEntity<?> getVisionSupportedModels() {
        try {
            log.info("获取支持Vision的模型列表");
            
            if (modelConfigService == null) {
                log.warn("ModelConfigService not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "模型配置服务暂不可用"));
            }
            
            List<Map<String, Object>> visionModels = modelConfigService.getVisionSupportedModels();
            log.info("返回 {} 个支持Vision的模型", visionModels.size());
            
            return ResponseEntity.ok(visionModels);
        } catch (Exception e) {
            log.error("获取Vision模型列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取Vision模型列表失败: " + e.getMessage()));
        }
    }
}

