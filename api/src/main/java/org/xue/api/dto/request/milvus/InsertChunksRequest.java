package org.xue.api.dto.request.milvus;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTOs for request bodies
@Data
@AllArgsConstructor
public class InsertChunksRequest {
    private String docText;
    private String docId;

    public String getDocText() {
        return docText;
    }

    public void setDocText(String docText) {
        this.docText = docText;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
