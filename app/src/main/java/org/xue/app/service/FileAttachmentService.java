package org.xue.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.FileUploadResponseDTO;
import org.xue.app.entity.FileAttachment;
import org.xue.app.repository.FileAttachmentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件附件服务类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;

    // 从配置文件读取文件上传路径，默认为 uploads 目录
    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    // 从配置文件读取文件访问URL前缀
    @Value("${app.upload.url-prefix:/files}")
    private String urlPrefix;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param conversationId 对话ID（可选）
     * @param userId 用户ID
     * @return 文件上传响应
     */
    public FileUploadResponseDTO uploadFile(MultipartFile file, String conversationId, String userId) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 生成唯一的文件ID
        String fileId = UUID.randomUUID().toString();
        
        // 获取原始文件名和扩展名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // 生成存储文件名
        String storedFileName = fileId + fileExtension;
        
        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 保存文件到本地
        Path filePath = uploadDir.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 创建文件附件实体
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(fileId);
        fileAttachment.setFileName(originalFileName);
        fileAttachment.setFileSize(file.getSize());
        fileAttachment.setFileType(file.getContentType());
        fileAttachment.setFilePath(filePath.toString());
        fileAttachment.setFileUrl(urlPrefix + "/" + storedFileName);
        fileAttachment.setConversationId(conversationId);
        fileAttachment.setUserId(userId);
        
        // 保存到数据库
        FileAttachment savedAttachment = fileAttachmentRepository.save(fileAttachment);
        
        log.info("文件上传成功: {} -> {}", originalFileName, filePath);
        
        // 返回响应DTO
        return FileUploadResponseDTO.builder()
                .id(savedAttachment.getId())
                .fileName(savedAttachment.getFileName())
                .fileSize(savedAttachment.getFileSize())
                .fileType(savedAttachment.getFileType())
                .filePath(savedAttachment.getFilePath())
                .fileUrl(savedAttachment.getFileUrl())
                .build();
    }
} 