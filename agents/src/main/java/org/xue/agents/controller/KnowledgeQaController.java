package org.xue.agents.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.agents.dto.*;
import org.xue.agents.entity.DocumentCategory;
import org.xue.agents.entity.KnowledgeQaRecord;
import org.xue.agents.entity.PopularQuestion;
import org.xue.agents.service.KnowledgeQaService;
import reactor.core.publisher.Flux;
import org.xue.agents.exception.BusinessException;

import java.util.List;

/**
 * 知识库问答控制器
 */
@RestController
@RequestMapping("/api/knowledge-qa")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeQaController {

    private final KnowledgeQaService knowledgeQaService;
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.debug("获取当前用户ID - Authentication: {}", authentication);
        
        if (authentication == null) {
            log.error("SecurityContext中没有Authentication对象");
            throw new RuntimeException("用户未登录：没有认证信息");
        }
        
        if (!authentication.isAuthenticated()) {
            log.error("用户未通过认证: {}", authentication);
            throw new RuntimeException("用户未登录：认证失败");
        }
        
        Object principal = authentication.getPrincipal();
        if ("anonymousUser".equals(principal)) {
            log.error("用户为匿名用户");
            throw new RuntimeException("用户未登录：匿名用户");
        }
        
        // 在新的SecurityConfig中，principal是userId，credentials是userName
        String userId = authentication.getName(); // 这个返回principal的字符串表示
        if (userId == null || userId.trim().isEmpty()) {
            log.error("用户ID为空 - Principal: {}, Credentials: {}", principal, authentication.getCredentials());
            throw new RuntimeException("用户未登录：用户ID为空");
        }
        
        log.debug("成功获取用户ID: {}, Principal: {}, Credentials: {}", userId, principal, authentication.getCredentials());
        return userId;
    }
    
    /**
     * 知识库问答（同步）
     */
    @PostMapping("/ask")
    public ResponseEntity<KnowledgeQaResponse> askQuestion(@RequestBody KnowledgeQaRequest request) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("收到用户问答请求: {} (用户: {})", request.getQuestion(), currentUserId);
            
            // 强制设置为当前登录用户的ID，忽略请求中的userId
            request.setUserId(currentUserId);
            
            KnowledgeQaResponse response = knowledgeQaService.askQuestion(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("知识库问答处理失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 知识库问答（流式）
     */
    @PostMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askQuestionStream(@RequestBody KnowledgeQaRequest request, HttpServletRequest httpRequest) {
        // 手动验证认证信息（因为SSE端点跳过了Spring Security）
        String userName = httpRequest.getHeader("X-User-Name");
        String userId = httpRequest.getHeader("X-User-Id");
        String tokenValid = httpRequest.getHeader("X-Token-Valid");
        
        if (userName == null || userId == null || !"true".equals(tokenValid)) {
            log.warn("流式传输请求缺少有效认证信息: userName={}, userId={}, tokenValid={}", userName, userId, tokenValid);
            return Flux.error(new RuntimeException("认证失败"));
        }
        
        log.info("收到用户流式问答请求: {} (用户: {})", request.getQuestion(), userId);
        
        // 强制设置为当前登录用户的ID
        request.setUserId(userId);
        
        return knowledgeQaService.askQuestionStream(request, userId)
                .map(chunk -> "data: " + chunk + "\n\n")  // 转换为SSE格式
                .doOnError(error -> log.error("流式问答处理失败", error));
    }
    
    /**
     * 获取最近提问
     */
    @GetMapping("/recent-questions")
    public ResponseEntity<List<KnowledgeQaRecord>> getRecentQuestions(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户最近提问: 用户={}, 限制={}, 分类={}", currentUserId, limit, category);
            
            List<KnowledgeQaRecord> recentQuestions = knowledgeQaService.getUserRecentQuestions(currentUserId, limit, category);
            return ResponseEntity.ok(recentQuestions);
        } catch (Exception e) {
            log.error("获取最近提问失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取热门问题（仅当前用户）
     */
    @GetMapping("/hot-questions")
    public ResponseEntity<List<PopularQuestion>> getHotQuestions(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户热门问题: 用户={}, 限制={}, 分类={}", currentUserId, limit, category);
            
            List<PopularQuestion> hotQuestions = knowledgeQaService.getUserHotQuestions(currentUserId, limit, category);
            return ResponseEntity.ok(hotQuestions);
        } catch (Exception e) {
            log.error("获取热门问题失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取知识库分类统计信息
     */
    @GetMapping("/knowledge-base-statistics")
    public ResponseEntity<List<CategoryStatistics>> getKnowledgeBaseStatistics() {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户知识库统计: 用户={}", currentUserId);
            
            List<CategoryStatistics> statistics = knowledgeQaService.getUserKnowledgeBaseStatistics(currentUserId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取知识库统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取分类下的文档列表（仅当前用户）
     */
    @GetMapping("/categories/{categoryId}/documents")
    public ResponseEntity<CategoryStatistics> getCategoryDocuments(@PathVariable String categoryId) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户分类文档: 用户={}, 分类={}", currentUserId, categoryId);
            
            CategoryStatistics categoryDocuments = knowledgeQaService.getUserCategoryDocuments(currentUserId, categoryId);
            return ResponseEntity.ok(categoryDocuments);
        } catch (Exception e) {
            log.error("获取分类文档失败: {}", categoryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取所有文档分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<DocumentCategory>> getAllCategories() {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户分类列表: 用户={}", currentUserId);
            
            List<DocumentCategory> categories = knowledgeQaService.getUserCategories(currentUserId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取单个分类详情
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<DocumentCategory> getCategoryById(@PathVariable String categoryId) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户分类详情: 用户={}, 分类={}", currentUserId, categoryId);
            
            DocumentCategory category = knowledgeQaService.getUserCategoryById(currentUserId, categoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("获取分类详情失败: {}", categoryId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 创建分类
     */
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryRequest request) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("用户创建分类: 用户={}, 分类名={}", currentUserId, request.getName());
            
            DocumentCategory category = knowledgeQaService.createUserCategory(currentUserId, request);
            return ResponseEntity.ok(category);
        } catch (BusinessException e) {
            // 业务逻辑错误，使用WARN级别，返回400
            log.warn("创建分类被拒绝: {} - {}", request.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 系统错误，使用ERROR级别，返回500
            log.error("创建分类时发生系统错误: {}", request.getName(), e);
            return ResponseEntity.internalServerError().body("系统错误，请稍后重试");
        }
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable String categoryId, 
            @RequestBody UpdateCategoryRequest request) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("用户更新分类: 用户={}, 分类={} -> {}", currentUserId, categoryId, request.getName());
            
            DocumentCategory category = knowledgeQaService.updateUserCategory(currentUserId, categoryId, request);
            return ResponseEntity.ok(category);
        } catch (BusinessException e) {
            // 业务逻辑错误，使用WARN级别，返回400
            log.warn("更新分类被拒绝: {} - {}", categoryId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 系统错误，使用ERROR级别，返回500
            log.error("更新分类时发生系统错误: {}", categoryId, e);
            return ResponseEntity.internalServerError().body("系统错误，请稍后重试");
        }
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable String categoryId) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("用户删除分类: 用户={}, 分类={}", currentUserId, categoryId);
            
            knowledgeQaService.deleteUserCategory(currentUserId, categoryId);
            return ResponseEntity.ok("分类删除成功");
        } catch (BusinessException e) {
            // 业务逻辑错误，使用WARN级别，返回400
            log.warn("删除分类被拒绝: {} - {}", categoryId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 系统错误，使用ERROR级别，返回500
            log.error("删除分类时发生系统错误: {}", categoryId, e);
            return ResponseEntity.internalServerError().body("系统错误，请稍后重试");
        }
    }
    
    /**
     * 用户反馈
     */
    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(
            @RequestParam String recordId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("收到用户反馈: 用户={}, 记录ID={}, 评分={}, 评论={}", currentUserId, recordId, rating, comment);
            
            knowledgeQaService.submitFeedback(recordId, rating, comment, currentUserId);
            return ResponseEntity.ok("反馈提交成功");
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return ResponseEntity.internalServerError().body("提交反馈失败");
        }
    }
} 