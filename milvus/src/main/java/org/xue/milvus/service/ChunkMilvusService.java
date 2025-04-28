package org.xue.milvus.service;

import io.milvus.client.*;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import org.springframework.stereotype.Service;
import org.xue.milvus.embed.EmbeddingClient;

import java.util.*;

@Service
public class ChunkMilvusService {

    private final MilvusClient milvusClient;
    // 注入EmbeddingClient
    private final EmbeddingClient embeddingClient;

    // 配置 collection 名称和字段
    private static final String COLLECTION = "doc_chunk";
    private static final String PK_FIELD = "id";
    private static final String VECTOR_FIELD = "embedding";
    private static final String TEXT_FIELD = "chunk";

    // 向量维度，需与 embedding 大小一致
    private final int VECTOR_DIM = 768; // 例如 bge-base-zh

    // Index type
    private static final IndexType INDEX_TYPE = IndexType.AUTOINDEX;
    private static final MetricType METRIC_TYPE = MetricType.COSINE;

    public ChunkMilvusService(MilvusClient milvusClient, EmbeddingClient embeddingClient) {
        this.milvusClient = milvusClient;
        this.embeddingClient = embeddingClient;
    }

    /**
     * 建表（只需一次）
     */
    public void createCollectionIfNotExists() {

        R<Boolean> exist = milvusClient.hasCollection(
            HasCollectionParam.newBuilder().withCollectionName(COLLECTION).build()
        );
        if (exist.getData() != Boolean.TRUE) {
            FieldType pk = FieldType.newBuilder().withName(PK_FIELD).withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(true).build(); //设置自增 IDwithAutoID(true)
            FieldType txt = FieldType.newBuilder().withName(TEXT_FIELD).withDataType(DataType.VarChar).withMaxLength(1024).build();
            FieldType vec = FieldType.newBuilder().withName(VECTOR_FIELD).withDataType(DataType.FloatVector).withDimension(VECTOR_DIM).build(); //设置向量维度 withDimension(VECTOR_DIM)

            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION)
                    .withDescription("Doc Chunks Embedding")
                    .withShardsNum(2)
                    .addFieldType(pk)
                    .addFieldType(txt)
                    .addFieldType(vec)
                    .build();
            milvusClient.createCollection(createParam);

            // Create index
            milvusClient.createIndex(
              CreateIndexParam.newBuilder()
                .withCollectionName("doc_chunk")
                .withIndexName("embedding_index")
                .withFieldName("embedding")
                .withIndexType(INDEX_TYPE)
                .withMetricType(METRIC_TYPE)
                .withExtraParam("{}")
                .withSyncMode(Boolean.FALSE)
                .build()
            );
        }
    }

    /**
     * 写入一批文本分块及embedding
     */
    public void insertChunks(String docText) {

        //建表
        createCollectionIfNotExists();

        List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(docText);

        //List<Long> ids = new ArrayList<>(); // 这里用自增ID，也可自定义
        List<String> chunks = new ArrayList<>();
        List<List<Float>> vectors = new ArrayList<>();

        for (EmbeddingClient.ChunkEmbedding ce : chunkEmbeddings) {
            chunks.add(ce.getChunk());
            // Milvus要求 float32 类型
            List<Float> emb = ce.getEmbedding();
            vectors.add(emb);
        }

        List<InsertParam.Field> fields = Arrays.asList(
            new InsertParam.Field(TEXT_FIELD, chunks),
            new InsertParam.Field(VECTOR_FIELD, vectors)
        );
        InsertParam param = InsertParam.newBuilder()
                .withCollectionName(COLLECTION)
                .withFields(fields)
                .build();

        milvusClient.insert(param);
        // 加载内存
        milvusClient.loadCollection(
            LoadCollectionParam.newBuilder().withCollectionName(COLLECTION).build()
        );
    }

    /**
     * 检索：根据query文本找到最相近的chunk（topK）
     */
    public List<String> searchSimilarChunks(String queryText, int topK) {

        List<Float> queryEmbedding = embeddingClient.embedOne(queryText);
        List<Float> vector = new ArrayList<>();
        for (Float d : queryEmbedding) vector.add(d.floatValue());

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(COLLECTION)
                .withMetricType(MetricType.COSINE) // 余弦相似
//                .withMetricType(MetricType.IP) // 点积
                .withOutFields(Arrays.asList(TEXT_FIELD))
                .withVectors(Collections.singletonList(vector))
                .withTopK(topK)
                .withVectorFieldName(VECTOR_FIELD)
                .withParams("{\"nprobe\":10}")
                .build();

        R<SearchResults> resp = milvusClient.search(searchParam);
        SearchResultsWrapper wrapper = new SearchResultsWrapper(resp.getData().getResults());
        List<String> hits = new ArrayList<>();
        List<?> textList = wrapper.getFieldData(TEXT_FIELD, 0);
        for (Object obj : textList) {
            hits.add(obj != null ? obj.toString() : "");
        }
        return hits;
    }

    /**
     * 删除（按主键/条件，可扩展）
     */
    public void deleteById(long id) {
        milvusClient.delete(DeleteParam.newBuilder()
                .withCollectionName(COLLECTION)
                .withExpr(PK_FIELD + " == " + id)
                .build());
    }

    // 可扩展批量删除、过滤删除等方法

    // 关闭 Milvus 连接
    public void close() {
        milvusClient.close();
    }




}