package org.xue.milvus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.api.dto.request.milvus.InsertChunksRequest;
import org.xue.api.dto.request.milvus.SearchChunksRequest;
import org.xue.milvus.service.ChunkMilvusService;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * REST 控制器为 OpenFeign 客户端暴露 ChunkMilvusService 方法。
 */
@RestController
@RequestMapping("/api/milvus")
@RequiredArgsConstructor
public class ChunkMilvusController {

    private final ChunkMilvusService chunkMilvusService;

    /**
     * 确保集合存在，如果不存在，则创建 。
     */
    @PostMapping("/collections/create")
    public ResponseEntity<Void> createCollection() {
        chunkMilvusService.createCollectionIfNotExists();
        return ResponseEntity.ok().build();
    }

    /**
     * 插入文档块和嵌入。
     * @param request 包含文档文本和文档 ID
     */
    @PostMapping("/chunks/insert")
    public ResponseEntity<Void> insertChunks(@RequestBody InsertChunksRequest request) {
        chunkMilvusService.insertChunks(request.getDocText(), request.getDocId());
        return ResponseEntity.ok().build();
    }

    /**
     * 按查询文本搜索相似文本块。
     * @param request 包含查询文本和 topK
     * @return 匹配 chunk 列表
     */
    @PostMapping("/chunks/search")
    public ResponseEntity<List<String>> searchChunks(@RequestBody SearchChunksRequest request) {
        List<String> results = chunkMilvusService.searchSimilarChunks(
            request.getQueryText(), request.getTopK());
        return ResponseEntity.ok(results);
    }

    /**
     * 按文档 ID 删除块。
     * @param id 要删除的文档 ID
     */
    @DeleteMapping("/chunks/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        chunkMilvusService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
