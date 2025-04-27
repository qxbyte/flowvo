package org.xue.chat.service;

import java.util.List;

public interface QdrantService {
    // 保存单条文本及其元数据
    void saveText(String text, String docId, int chunkIndex);

    // 检索相似文本
    List<String> searchSimilar(String query, int topK);

    // 按条件删除
    void deleteByDocId(String docId);
}
