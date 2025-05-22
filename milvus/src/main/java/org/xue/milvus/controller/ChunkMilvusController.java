package org.xue.mcp_client.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.api.milvus.dto.InsertChunksRequest;
import org.xue.api.milvus.dto.SearchChunksRequest;
import org.xue.mcp_client.service.ChunkMilvusService;

import java.util.List;

/**
 * REST 控制器为 OpenFeign 客户端暴露 ChunkMilvusService 方法。
 */
@RestController
@RequestMapping("/api/milvus")
@AllArgsConstructor
public class ChunkMilvusController {

    private static final Logger log = LoggerFactory.getLogger(ChunkMilvusController.class);
    
    private final ChunkMilvusService chunkMilvusService;

    /**
     * 健康检查端点
     */
    @GetMapping("/health/check")
    public String healthCheck() {
        log.info("收到健康检查请求");
        return "Milvus服务正常运行";
    }

    /**
     * 创建集合
     */
    @PostMapping("/collections/create")
    public void createCollection() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("收到创建集合请求，认证信息：{}", auth != null ? auth.getName() : "未认证");
        chunkMilvusService.createCollectionIfNotExists();
    }

    /**
     * 插入数据
     */
    @PostMapping("/chunks/insert")
    public void insertChunks(@RequestBody InsertChunksRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("收到插入分块请求，docId={}，文本长度={}，认证信息：{}", 
                request.getDocId(), 
                request.getDocText() != null ? request.getDocText().length() : 0,
                auth != null ? auth.getName() : "未认证");
        chunkMilvusService.insertChunks(request.getDocText(), request.getDocId());
    }

    /**
     * 搜索相似数据
     */
    @PostMapping("/chunks/search")
    public List<String> searchChunks(@RequestBody SearchChunksRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("收到搜索分块请求，查询文本={}，topK={}，认证信息：{}", 
                request.getQueryText() != null ? request.getQueryText().substring(0, Math.min(30, request.getQueryText().length())) + "..." : "null",
                request.getTopK(),
                auth != null ? auth.getName() : "未认证");
        
        try {
            List<String> results = chunkMilvusService.searchSimilarChunks(request.getQueryText(), request.getTopK());
            log.info("搜索成功，返回{}条结果", results.size());
            return results;
        } catch (Exception e) {
            log.error("搜索分块时发生错误", e);
            throw e;
        }
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/chunks/{id}")
    public void deleteById(@PathVariable("id") String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("收到删除分块请求，id={}，认证信息：{}", id, auth != null ? auth.getName() : "未认证");
        chunkMilvusService.deleteById(id);
    }
}
