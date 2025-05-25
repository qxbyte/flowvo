package org.xue.app.chat.service;

import org.springframework.web.multipart.MultipartFile;
import org.xue.app.chat.dto.VisionRequestDTO;
import org.xue.app.chat.dto.VisionResponseDTO;

/**
 * 图像识别服务接口
 */
public interface VisionService {
    
    /**
     * 识别图像内容
     *
     * @param imageFile 图像文件
     * @param request 识别请求参数
     * @return 识别结果
     */
    VisionResponseDTO recognizeImage(MultipartFile imageFile, VisionRequestDTO request);
    
    /**
     * 验证图像文件是否支持识别
     *
     * @param imageFile 图像文件
     * @return 是否支持
     */
    boolean isImageSupported(MultipartFile imageFile);
} 