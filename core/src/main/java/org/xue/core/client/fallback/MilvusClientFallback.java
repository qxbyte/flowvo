package org.xue.core.client.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xue.api.dto.request.milvus.InsertChunksRequest;
import org.xue.api.dto.request.milvus.SearchChunksRequest;
import org.xue.core.client.feign.MilvusFeign;

import java.util.ArrayList;
import java.util.List;

/**
 * Milvus服务调用失败时的降级处理
 */
@Component
public class MilvusClientFallback implements MilvusFeign {
    private static final Logger log = LoggerFactory.getLogger(MilvusClientFallback.class);

    @Override
    public void createCollection() {
        log.error("【降级】调用Milvus创建集合失败");
    }

    @Override
    public void insertChunks(InsertChunksRequest request) {
        log.error("【降级】调用Milvus插入分块失败");
    }

    @Override
    public List<String> searchChunks(SearchChunksRequest request) {
        log.error("【降级】调用Milvus搜索分块失败，返回空结果");
        List<String> fallbackResults = new ArrayList<>();
        fallbackResults.add("抱歉，知识库服务暂时不可用，将使用基础知识为您回答问题。");
        return fallbackResults;
    }

    @Override
    public void deleteById(String id) {
        log.error("【降级】调用Milvus删除分块失败，ID：{}", id);
    }
}
