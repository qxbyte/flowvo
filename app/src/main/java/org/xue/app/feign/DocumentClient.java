package org.xue.app.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.config.FeignConfig;
import org.xue.app.dto.DocumentSearchRequest;
import org.xue.app.dto.DocumentUploadRequest;
import org.xue.app.dto.SearchResult;
import org.xue.app.entity.Document;

import java.util.List;

/**
 * 文档管理Feign客户端
 * 调用agents模块的文档管理服务
 */
@FeignClient(name = "agents", url = "${agents.service.url:http://localhost:8081}", configuration = FeignConfig.class)
public interface DocumentClient {

    /**
     * 上传文档（JSON格式）- 已解析的文本内容
     */
    @PostMapping("/api/documents/upload")
    ResponseEntity<Document> uploadDocument(@RequestBody DocumentUploadRequest request);

    /**
     * 上传文件（multipart格式）- 原始文件，让agents自己解析
     */
    @PostMapping(value = "/api/documents/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Document> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) List<String> tags);

    /**
     * 获取支持的文件类型
     */
    @GetMapping("/api/documents/supported-types")
    ResponseEntity<List<String>> getSupportedTypes();

    /**
     * 删除文档
     */
    @DeleteMapping("/api/documents/{documentId}")
    ResponseEntity<Void> deleteDocument(
            @PathVariable("documentId") String documentId,
            @RequestParam("userId") String userId);

    /**
     * 更新文档
     */
    @PutMapping("/api/documents/{documentId}")
    ResponseEntity<Document> updateDocument(
            @PathVariable("documentId") String documentId,
            @RequestParam("userId") String userId,
            @RequestBody Document updatedDocument);

    /**
     * 获取单个文档
     */
    @GetMapping("/api/documents/{documentId}")
    ResponseEntity<Document> getDocument(
            @PathVariable("documentId") String documentId,
            @RequestParam("userId") String userId);

    /**
     * 获取用户的所有文档
     */
    @GetMapping("/api/documents/user/{userId}")
    ResponseEntity<List<Document>> getUserDocuments(@PathVariable("userId") String userId);

    /**
     * 向量搜索文档
     */
    @PostMapping("/api/documents/search")
    ResponseEntity<List<SearchResult>> searchDocuments(@RequestBody DocumentSearchRequest request);

    /**
     * 重新处理文档
     */
    @PostMapping("/api/documents/{documentId}/reprocess")
    ResponseEntity<Document> reprocessDocument(
            @PathVariable("documentId") String documentId,
            @RequestParam("userId") String userId);
} 