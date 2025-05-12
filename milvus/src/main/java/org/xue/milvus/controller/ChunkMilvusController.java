package org.xue.milvus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.api.dto.request.milvus.InsertChunksRequest;
import org.xue.api.dto.request.milvus.SearchChunksRequest;
import org.xue.milvus.service.ChunkMilvusService;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * REST controller to expose ChunkMilvusService methods for OpenFeign clients.
 */
@RestController
@RequestMapping("/api/milvus")
@RequiredArgsConstructor
public class ChunkMilvusController {

    private final ChunkMilvusService chunkMilvusService;

    /**
     * Ensure the collection exists, create if not.
     */
    @PostMapping("/collections/create")
    public ResponseEntity<Void> createCollection() {
        chunkMilvusService.createCollectionIfNotExists();
        return ResponseEntity.ok().build();
    }

    /**
     * Insert document chunks and embeddings.
     * @param request contains document text and document id
     */
    @PostMapping("/chunks/insert")
    public ResponseEntity<Void> insertChunks(@RequestBody InsertChunksRequest request) {
        chunkMilvusService.insertChunks(request.getDocText(), request.getDocId());
        return ResponseEntity.ok().build();
    }

    /**
     * Search similar chunks by query text.
     * @param request contains query text and topK
     * @return list of matching chunks
     */
    @PostMapping("/chunks/search")
    public ResponseEntity<List<String>> searchChunks(@RequestBody SearchChunksRequest request) {
        List<String> results = chunkMilvusService.searchSimilarChunks(
            request.getQueryText(), request.getTopK());
        return ResponseEntity.ok(results);
    }

    /**
     * Delete chunks by document id.
     * @param id the document id to delete
     */
    @DeleteMapping("/chunks/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        chunkMilvusService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
