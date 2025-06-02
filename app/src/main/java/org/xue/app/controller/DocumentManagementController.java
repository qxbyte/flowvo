package org.xue.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.SearchResult;
import org.xue.app.entity.Document;
import org.xue.app.service.DocumentManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器
 * 提供文档上传、管理和搜索的前端API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
public class DocumentManagementController {

    private final DocumentManagementService documentManagementService;

    @Autowired
    public DocumentManagementController(DocumentManagementService documentManagementService) {
        this.documentManagementService = documentManagementService;
    }

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "tags", required = false) String[] tags,
            @RequestParam(value = "description", required = false) String description) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 记录接收到的参数
            log.debug("接收到文档上传请求 - 文件: {}, 用户: {}, 标签: {}, 描述: {}", 
                     file.getOriginalFilename(), userId, 
                     tags != null ? String.join(",", tags) : "无", description);

            // 检查文件类型
            if (!documentManagementService.isFileSupported(file.getOriginalFilename(), file.getContentType())) {
                response.put("success", false);
                response.put("message", "不支持的文件类型");
                response.put("supportedTypes", documentManagementService.getSupportedFileTypes());
                return ResponseEntity.badRequest().body(response);
            }

            // 处理文档
            Document document = documentManagementService.uploadDocument(file, userId, tags, description);

            response.put("success", true);
            response.put("message", "文档上传成功");
            response.put("document", document);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("文档上传失败", e);
            response.put("success", false);
            response.put("message", "文档上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = documentManagementService.deleteDocument(documentId, userId);

            if (success) {
                response.put("success", true);
                response.put("message", "文档删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文档删除失败，可能不存在或无权限");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("删除文档失败", e);
            response.put("success", false);
            response.put("message", "删除文档失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新文档信息
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> updateDocument(
            @PathVariable String documentId,
            @RequestParam String userId,
            @RequestBody Document updatedDocument) {

        Map<String, Object> response = new HashMap<>();

        try {
            Document document = documentManagementService.updateDocument(documentId, userId, updatedDocument);

            if (document != null) {
                response.put("success", true);
                response.put("message", "文档更新成功");
                response.put("document", document);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文档更新失败，可能不存在或无权限");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("更新文档失败", e);
            response.put("success", false);
            response.put("message", "更新文档失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> getDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Document document = documentManagementService.getDocument(documentId, userId);

            if (document != null) {
                response.put("success", true);
                response.put("document", document);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文档不存在或无权限访问");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("获取文档失败", e);
            response.put("success", false);
            response.put("message", "获取文档失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户的所有文档
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDocuments(@PathVariable String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Document> documents = documentManagementService.getUserDocuments(userId);

            response.put("success", true);
            response.put("documents", documents);
            response.put("count", documents.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户文档列表失败", e);
            response.put("success", false);
            response.put("message", "获取文档列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 向量搜索文档
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @RequestParam String query,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0.5") Double threshold) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (query == null || query.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索查询不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            List<SearchResult> results = documentManagementService.searchDocuments(query, userId, limit, threshold);

            response.put("success", true);
            response.put("query", query);
            response.put("results", results);
            response.put("count", results.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("文档搜索失败", e);
            response.put("success", false);
            response.put("message", "文档搜索失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重新处理文档
     */
    @PostMapping("/{documentId}/reprocess")
    public ResponseEntity<Map<String, Object>> reprocessDocument(
            @PathVariable String documentId,
            @RequestParam String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Document document = documentManagementService.reprocessDocument(documentId, userId);

            if (document != null) {
                response.put("success", true);
                response.put("message", "文档重新处理成功");
                response.put("document", document);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文档重新处理失败，可能不存在或无权限");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("重新处理文档失败", e);
            response.put("success", false);
            response.put("message", "重新处理文档失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取支持的文件类型
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<String> supportedTypes = documentManagementService.getSupportedFileTypes();

            response.put("success", true);
            response.put("supportedTypes", supportedTypes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取支持的文件类型失败", e);
            response.put("success", false);
            response.put("message", "获取支持的文件类型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 