package org.xue.api.milvus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchChunksRequest {
    private String queryText;
    private int topK;
}
