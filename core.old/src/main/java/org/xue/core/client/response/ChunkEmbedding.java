package org.xue.core.client.response;

import lombok.Data;

import java.util.List;

@Data
public class ChunkEmbedding {
    public String chunk;
    public List<Float> embedding;
    public ChunkEmbedding(String chunk, List<Float> embedding) {
        this.chunk = chunk;
        this.embedding = embedding;
    }
    @Override
    public String toString() {
        return "ChunkEmbedding{" + "chunk='" + chunk + '\'' + ", embedding(size)=" + embedding.size() + '}';
    }
}
