from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

# 选择你喜欢的模型（如中文BGE）
MODEL_NAME = "BAAI/bge-base-zh"  # 也可用 gte-base、m3e-base 等
model = SentenceTransformer(MODEL_NAME)

app = FastAPI()

class EmbeddingRequest(BaseModel):
    input: list[str]  # 支持批量输入

class EmbeddingResponse(BaseModel):
    embeddings: list[list[float]]

@app.post("/embed", response_model=EmbeddingResponse)
def embed(req: EmbeddingRequest):
    embeddings = model.encode(req.input).tolist()
    return EmbeddingResponse(embeddings=embeddings)