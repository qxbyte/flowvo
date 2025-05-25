package org.xue.app.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.chat.dto.VisionRequestDTO;
import org.xue.app.chat.dto.VisionResponseDTO;
import org.xue.app.chat.service.VisionService;
import org.xue.app.controller.BaseController;

import java.util.Map;

/**
 * 图像识别控制器
 */
@RestController
@RequestMapping("/api/vision")
@Slf4j
public class VisionController extends BaseController {
    
    @Autowired
    private VisionService visionService;
    
    /**
     * 图像识别接口
     *
     * @param imageFile 图像文件
     * @param message 用户消息（可选）
     * @param model 使用的模型（可选，默认gpt-4o-mini）
     * @param conversationId 对话ID（可选）
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeImage(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        
        try {
            log.info("收到图像识别请求: 文件={}, 大小={}bytes, 消息='{}'", 
                imageFile.getOriginalFilename(), imageFile.getSize(), message);
            
            // 验证文件
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body("图像文件不能为空");
            }
            
            // 快速验证图像文件
            if (!visionService.isImageSupported(imageFile)) {
                return ResponseEntity.badRequest().body("图像文件格式不支持或文件过大");
            }
            
            // 构建请求DTO
            VisionRequestDTO requestDTO = VisionRequestDTO.builder()
                .conversationId(conversationId)
                .message(message)
                .model(model)
                .fileName(imageFile.getOriginalFilename())
                .mimeType(imageFile.getContentType())
                .fileSize(imageFile.getSize())
                .build();
            
            // 调用服务进行识别
            VisionResponseDTO response = visionService.recognizeImage(imageFile, requestDTO);
            
            if (response.isSuccess()) {
                log.info("图像识别成功: {}", imageFile.getOriginalFilename());
                return ResponseEntity.ok(response);
            } else {
                log.warn("图像识别失败: {}", response.getError());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("图像识别接口异常", e);
            return ResponseEntity.internalServerError().body("图像识别服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 检查图像文件是否支持识别
     *
     * @param imageFile 图像文件
     * @return 是否支持
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkImageSupport(
            @RequestParam("image") MultipartFile imageFile) {
        
        try {
            boolean supported = visionService.isImageSupported(imageFile);
            return ResponseEntity.ok().body(Map.of(
                "supported", supported,
                "fileName", imageFile.getOriginalFilename(),
                "fileSize", imageFile.getSize(),
                "mimeType", imageFile.getContentType()
            ));
        } catch (Exception e) {
            log.error("检查图像文件支持性异常", e);
            return ResponseEntity.internalServerError().body("检查文件支持性失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取支持的图像格式信息
     *
     * @return 支持的格式列表
     */
    @GetMapping("/formats")
    public ResponseEntity<?> getSupportedFormats() {
        return ResponseEntity.ok().body(Map.of(
            "supportedTypes", new String[]{"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"},
            "maxFileSize", "10MB",
            "maxFileSizeBytes", 10 * 1024 * 1024
        ));
    }
} 