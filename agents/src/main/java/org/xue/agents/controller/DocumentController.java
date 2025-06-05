package org.xue.agents.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.agents.dto.DocumentSearchRequest;
import org.xue.agents.dto.DocumentUploadRequest;
import org.xue.agents.dto.DocumentWithCategoryDTO;
import org.xue.agents.dto.SearchResult;
import org.xue.agents.entity.Document;
import org.xue.agents.parse.DocumentParserService;
import org.xue.agents.service.DocumentService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文档管理控制器
 * 提供文档上传、删除、修改和向量检索等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentParserService documentParserService;

    @Autowired
    public DocumentController(DocumentService documentService, DocumentParserService documentParserService) {
        this.documentService = documentService;
        this.documentParserService = documentParserService;
    }

    /**
     * 上传文档（JSON格式）
     */
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestBody DocumentUploadRequest request) {
        try {
            Document document = documentService.uploadDocument(request);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 上传文件（multipart格式）- 新增接口
     * 接收文件上传，自动解析内容，然后转为DocumentUploadRequest处理
     */
    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String[] tags,
            @RequestParam(value = "category", required = false) String category) {
        
        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            String fileName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            
            log.info("接收到文件上传请求: fileName={}, mimeType={}, size={}, category={}", fileName, mimeType, file.getSize(), category);
            
            // 2. 检查文件类型是否支持
            if (!documentParserService.isSupported(fileName, mimeType)) {
                return ResponseEntity.badRequest().body("不支持的文件类型: " + fileName);
            }
            
            // 3. 解析文件内容
            String content;
            try {
                content = documentParserService.parseDocument(file.getInputStream(), fileName, mimeType);
                if (content == null || content.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("文件内容解析失败或为空");
                }
            } catch (IOException e) {
                log.error("文件解析失败: {}", fileName, e);
                return ResponseEntity.badRequest().body("文件解析失败: " + e.getMessage());
            }
            
            // 4. 构建DocumentUploadRequest
            DocumentUploadRequest request = DocumentUploadRequest.builder()
                    .name(fileName)
                    .content(content)
                    .size(file.getSize())
                    .type(getFileExtension(fileName))
                    .userId(userId)
                    .description(description)
                    .tags(tags != null ? Arrays.asList(tags) : List.of())
                    .category(category)  // 添加分类参数
                    .filePath("uploads/" + UUID.randomUUID() + "/" + fileName) // 虚拟路径
                    .build();
            
            // 5. 调用现有的文档处理逻辑
            Document document = documentService.uploadDocument(request);
            
            return ResponseEntity.ok(document);
            
        } catch (Exception e) {
            log.error("文件上传处理失败: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的文件类型
     */
    @GetMapping("/supported-types")
    public ResponseEntity<List<String>> getSupportedTypes() {
        try {
            List<String> types = documentParserService.getSupportedTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            log.error("获取支持的文件类型失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 提取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {
        try {
            boolean deleted = documentService.deleteDocument(documentId, userId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("文档删除失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新文档
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable String documentId,
            @RequestParam String userId,
            @RequestBody Document updatedDocument) {
        try {
            Document document = documentService.updateDocument(documentId, updatedDocument, userId);
            if (document != null) {
                return ResponseEntity.ok(document);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("文档更新失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取单个文档
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<Document> getDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {
        try {
            Document document = documentService.getDocument(documentId, userId);
            if (document != null) {
                return ResponseEntity.ok(document);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户的所有文档
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable String userId) {
        try {
            List<Document> documents = documentService.getUserDocuments(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("获取用户文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户的所有文档（包含分类信息）
     */
    @GetMapping("/user/{userId}/with-category")
    public ResponseEntity<List<DocumentWithCategoryDTO>> getUserDocumentsWithCategory(@PathVariable String userId) {
        try {
            List<DocumentWithCategoryDTO> documents = documentService.getUserDocumentsWithCategory(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("获取用户文档（含分类信息）失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 向量搜索文档
     */
    @PostMapping("/search")
    public ResponseEntity<List<SearchResult>> searchDocuments(@RequestBody DocumentSearchRequest request) {
        try {
            List<SearchResult> results = documentService.searchDocuments(request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("文档搜索失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 重新处理文档
     */
    @PostMapping("/{documentId}/reprocess")
    public ResponseEntity<Document> reprocessDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {
        try {
            Document document = documentService.reprocessDocument(documentId, userId);
            if (document != null) {
                return ResponseEntity.ok(document);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("重新处理文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 重新处理文档（包含新文件上传）
     */
    @PostMapping("/{documentId}/reprocess-with-file")
    public ResponseEntity<Document> reprocessDocumentWithFile(
            @PathVariable String documentId,
            @RequestParam String userId,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("开始重新处理文档: {} (用户: {}, 新文件: {})", documentId, userId, file.getOriginalFilename());
            
            if (file.isEmpty()) {
                log.warn("上传的文件为空");
                return ResponseEntity.badRequest().build();
            }
            
            Document document = documentService.reprocessDocumentWithFile(documentId, userId, file);
            if (document != null) {
                return ResponseEntity.ok(document);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("重新处理文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 