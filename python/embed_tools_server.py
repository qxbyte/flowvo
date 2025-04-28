from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from langchain.text_splitter import RecursiveCharacterTextSplitter
from typing import List

MODEL_NAME = "BAAI/bge-base-zh"
model = SentenceTransformer(MODEL_NAME)
app = FastAPI()

# 请求/响应数据模型
class TextRequest(BaseModel):
    text: str

class TextsRequest(BaseModel):
    texts: List[str]

class ChunksResponse(BaseModel):
    chunks: List[str]

class EmbeddingResponse(BaseModel):
    embedding: List[float]

class EmbeddingsResponse(BaseModel):
    embeddings: List[List[float]]

class ChunkEmbedding(BaseModel):
    chunk: str
    embedding: List[float]

class ChunkEmbeddingsResponse(BaseModel):
    chunk_embeddings: List[ChunkEmbedding]

# 1. 文本切分
@app.post("/split", response_model=ChunksResponse)
def split(req: TextRequest):
    splitter = RecursiveCharacterTextSplitter(chunk_size=200, chunk_overlap=30)
    chunks = splitter.split_text(req.text)
    return ChunksResponse(chunks=chunks)

# 2. 单条文本向量化
@app.post("/embed_one", response_model=EmbeddingResponse)
def embed_one(req: TextRequest):
    emb = model.encode(req.text).tolist()
    return EmbeddingResponse(embedding=emb)

# 3. 批量文本向量化
@app.post("/embed_batch", response_model=EmbeddingsResponse)
def embed_batch(req: TextsRequest):
    embs = model.encode(req.texts).tolist()
    return EmbeddingsResponse(embeddings=embs)

# 4. 文档内容切分并返回chunk+embedding
@app.post("/split_embed", response_model=ChunkEmbeddingsResponse)
def split_embed(req: TextRequest):
    splitter = RecursiveCharacterTextSplitter(chunk_size=200, chunk_overlap=30)
    chunks = splitter.split_text(req.text)
    embs = model.encode(chunks).tolist()
    chunk_embeddings = [ChunkEmbedding(chunk=c, embedding=e) for c, e in zip(chunks, embs)]
    return ChunkEmbeddingsResponse(chunk_embeddings=chunk_embeddings)