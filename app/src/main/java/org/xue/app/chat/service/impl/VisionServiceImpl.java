package org.xue.app.chat.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.chat.client.OpenAIVisionClient;
import org.xue.app.chat.dto.VisionRequestDTO;
import org.xue.app.chat.dto.VisionResponseDTO;
import org.xue.app.chat.service.ImageProcessingService;
import org.xue.app.chat.service.VisionService;

/**
 * 图像识别服务实现类
 */
@Service
@Slf4j
public class VisionServiceImpl implements VisionService {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private OpenAIVisionClient openAIVisionClient;
    
    @Override
    public VisionResponseDTO recognizeImage(MultipartFile imageFile, VisionRequestDTO request) {
        log.info("开始处理图像识别请求: {}", imageFile.getOriginalFilename());
        
        try {
            // 1. 验证图像文件
            if (!imageProcessingService.isValidImageFile(imageFile)) {
                return VisionResponseDTO.builder()
                    .success(false)
                    .error("图像文件格式不支持或文件过大")
                    .build();
            }
            
            // 2. 提取图像信息
            VisionResponseDTO.ImageInfo imageInfo = imageProcessingService.extractImageInfo(imageFile);
            
            // 3. 调用 OpenAI Vision API
            String model = request.getModel() != null ? request.getModel() : "gpt-4o-mini";
            String userMessage = request.getMessage() != null ? request.getMessage() : "请描述这张图片的内容。";
            
            String recognitionResult = openAIVisionClient.recognizeImage(imageFile, userMessage, model);
            
            // 4. 构建响应
            return VisionResponseDTO.builder()
                .success(true)
                .content(recognitionResult)
                .model(model)
                .imageInfo(imageInfo)
                .build();
                
        } catch (Exception e) {
            log.error("图像识别处理失败", e);
            return VisionResponseDTO.builder()
                .success(false)
                .error("图像识别处理失败: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public boolean isImageSupported(MultipartFile imageFile) {
        return imageProcessingService.isValidImageFile(imageFile);
    }
} 