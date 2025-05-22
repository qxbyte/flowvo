package org.xue.app.milvus.service;

import io.milvus.client.MilvusClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xue.app.milvus.embed.EmbeddingClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
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
    private static final String DOC_ID = "doc_id";

    // 向量维度，需与 embedding 大小一致
    private final int VECTOR_DIM = 768; // 例如 bge-base-zh

    // Index type
    private static final IndexType VECTOR_INDEX_TYPE = IndexType.AUTOINDEX;
    private static final IndexType SCALAR_INDEX_TYPE = IndexType.TRIE;
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
            FieldType docId = FieldType.newBuilder().withName(DOC_ID).withDataType(DataType.VarChar).withMaxLength(100).build(); //用于删除文档向量数据

            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION)
                    .withDescription("Doc Chunks Embedding")
                    .withShardsNum(2)
                    .addFieldType(pk)
                    .addFieldType(txt)
                    .addFieldType(vec)
                    .addFieldType(docId)
                    .build();
            milvusClient.createCollection(createParam);

            // Create index
            milvusClient.createIndex(
              CreateIndexParam.newBuilder()
                .withCollectionName("doc_chunk")
                .withIndexName("embedding_index")
                .withFieldName("embedding")
                .withIndexType(VECTOR_INDEX_TYPE)
                .withMetricType(METRIC_TYPE)
                .withExtraParam("{}")
                .withSyncMode(Boolean.FALSE)
                .build()
            );

            // Create index
            milvusClient.createIndex(
              CreateIndexParam.newBuilder()
                .withCollectionName("doc_chunk")
                .withIndexName("doc_id_index")
                .withFieldName("chunk")
                .withIndexType(SCALAR_INDEX_TYPE)
                .withSyncMode(Boolean.FALSE)
                .build()
            );

            close();
        }
    }

    /**
     * 写入一批文本分块及embedding
     */
    public void insertChunks(String docText, String docId) {

        //建表
        createCollectionIfNotExists();

        List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(docText);

        //List<Long> ids = new ArrayList<>(); // 这里用自增ID，也可自定义
        List<String> chunks = new ArrayList<>();
        List<List<Float>> vectors = new ArrayList<>();
        List<String> docIds = new ArrayList<>();

        for (EmbeddingClient.ChunkEmbedding ce : chunkEmbeddings) {
            chunks.add(ce.getChunk());
            // Milvus要求 float32 类型
            List<Float> emb = ce.getEmbedding();
            vectors.add(emb);
            docIds.add(docId);
        }

        List<InsertParam.Field> fields = Arrays.asList(
            new InsertParam.Field(TEXT_FIELD, chunks),
            new InsertParam.Field(VECTOR_FIELD, vectors),
            new InsertParam.Field(DOC_ID, docIds)
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

        close();
    }

    /**
     * 检索：根据query文本找到最相近的chunk（topK）
     */
    public List<String> searchSimilarChunks(String queryText, int topK) {

        List<Float> queryEmbedding = embeddingClient.embedOne(queryText);
        log.info(queryEmbedding.toString());
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

        List<String> hits = new ArrayList<>();
        R<SearchResults> resp = milvusClient.search(searchParam);
        if(resp == null || resp.getData() == null) return hits;
        SearchResultsWrapper wrapper = new SearchResultsWrapper(resp.getData().getResults());
        List<?> textList = wrapper.getFieldData(TEXT_FIELD, 0);
        for (Object obj : textList) {
            hits.add(obj != null ? obj.toString() : "");
        }
        close();
        return hits;
    }

    /**
     * 删除（按主键/条件，可扩展）
     */
    public void deleteById(String id) {
        milvusClient.delete(DeleteParam.newBuilder()
                .withCollectionName(COLLECTION)
                .withExpr(DOC_ID + " == '" + id + "'")
                .build());
    }

    // 可扩展批量删除、过滤删除等方法

    // 关闭 Milvus 连接
    public void close() {
        milvusClient.close();
    }




}