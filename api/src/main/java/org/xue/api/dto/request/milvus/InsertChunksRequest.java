package org.xue.api.dto.request.milvus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTOs for request bodies
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsertChunksRequest {
    private String docText;
    private String docId;
}
