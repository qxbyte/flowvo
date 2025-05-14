// 注意：
// 1）如果 core 与 milvus 服务部署在不同服务器，建议只依赖一个共享的 API 模块（仅包含 DTO／请求／响应类）。
// 2）Feign 用于跨进程／跨服务器的 HTTP 调用，无需本地实现代码。
package org.xue.core.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.xue.api.milvus.dto.InsertChunksRequest;
import org.xue.api.milvus.dto.SearchChunksRequest;
import org.xue.core.client.fallback.MilvusClientFallback;
import org.xue.core.config.FeignConfig;

import java.util.List;

/**
 * Milvus 服务的 Feign 客户端接口
 */
@FeignClient(
    name = "milvus",
    path = "/api/milvus",
    configuration = FeignConfig.class,
    fallback = MilvusClientFallback.class)
public interface MilvusFeign {

    /**
     * 创建或验证集合是否存在
     */
    @PostMapping("/collections/create")
    void createCollection();

    /**
     * 向 Milvus 插入文档分块及其 Embedding
     * @param request 包含文档文本（docText）和文档 ID（docId）
     */
    @PostMapping("/chunks/insert")
    void insertChunks(@RequestBody InsertChunksRequest request);

    /**
     * 根据查询文本检索最相似的分块
     * @param request 包含查询文本（queryText）和返回结果数量（topK）
     * @return 匹配到的分块列表
     */
    @PostMapping("/chunks/search")
    List<String> searchChunks(@RequestBody SearchChunksRequest request);

    /**
     * 根据文档 ID 删除对应分块
     * @param id 文档 ID
     */
    @DeleteMapping("/chunks/{id}")
    void deleteById(@PathVariable("id") String id);
}
