"""
FlowVO 嵌入工具服务器
提供文本嵌入、切分等功能的高性能API服务

作者: FlowVO Team
版本: 2.0.0
创建时间: 2025-06-02
"""

import os
import time
import logging
from typing import List, Optional, Dict, Any
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, validator
from sentence_transformers import SentenceTransformer
from langchain.text_splitter import RecursiveCharacterTextSplitter
import numpy as np

# ======================================
# 配置管理
# ======================================

class Config:
    """服务配置类"""
    
    # 本地模型路径配置
    BASE_DIR: str = os.path.dirname(os.path.abspath(__file__))  # python目录的绝对路径
    PROJECT_ROOT: str = os.path.dirname(BASE_DIR)  # 项目根目录
    LOCAL_MODELS_DIR: str = os.path.join(PROJECT_ROOT, "models")
    
    # 模型配置 - 优先使用本地模型
    LOCAL_MODEL_PATH: str = os.path.join(LOCAL_MODELS_DIR, "all-mpnet-base-v2")
    
    # 远程模型配置（本地不存在时使用）
    PRIMARY_MODEL: str = "BAAI/bge-large-en-v1.5"  # 1024维度
    FALLBACK_MODEL: str = "sentence-transformers/all-mpnet-base-v2"  # 768维度
    LOCAL_FALLBACK_MODEL: str = "all-mpnet-base-v2"  # 本地备用模型名
    
    # 目标维度配置
    TARGET_DIMENSION: int = 1536  # 目标维度，与OpenAI保持一致
    
    # 文本切分配置
    DEFAULT_CHUNK_SIZE: int = 512
    DEFAULT_CHUNK_OVERLAP: int = 50
    MAX_CHUNK_SIZE: int = 2048
    MIN_CHUNK_SIZE: int = 100
    
    # 服务配置
    MAX_BATCH_SIZE: int = 100
    MAX_TEXT_LENGTH: int = 8192
    ENABLE_CORS: bool = True
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FORMAT: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    
    @classmethod
    def get_debug_info(cls) -> dict:
        """获取配置调试信息"""
        return {
            "base_dir": cls.BASE_DIR,
            "project_root": cls.PROJECT_ROOT,
            "local_models_dir": cls.LOCAL_MODELS_DIR,
            "local_model_path": cls.LOCAL_MODEL_PATH,
            "local_model_exists": os.path.exists(cls.LOCAL_MODEL_PATH),
            "current_working_dir": os.getcwd()
        }

# ======================================
# 日志配置
# ======================================

# 确保日志目录存在
log_dir = os.path.join(Config.PROJECT_ROOT, "logs")
os.makedirs(log_dir, exist_ok=True)

log_file_path = os.path.join(log_dir, "embedding_service.log")

logging.basicConfig(
    level=getattr(logging, Config.LOG_LEVEL),
    format=Config.LOG_FORMAT,
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler(log_file_path, mode="a", encoding="utf-8")
    ]
)

logger = logging.getLogger(__name__)

# ======================================
# 全局变量
# ======================================

model: Optional[SentenceTransformer] = None
model_info: Dict[str, Any] = {}

# ======================================
# 模型管理
# ======================================

def load_embedding_model() -> SentenceTransformer:
    """加载嵌入模型，优先使用本地模型，支持自动降级和维度调整"""
    global model_info
    
    # 1. 首先尝试加载本地模型
    if os.path.exists(Config.LOCAL_MODEL_PATH) and os.path.isdir(Config.LOCAL_MODEL_PATH):
        try:
            logger.info(f"发现本地模型，正在加载: {Config.LOCAL_MODEL_PATH}")
            model = SentenceTransformer(Config.LOCAL_MODEL_PATH, local_files_only=True)
            
            # 测试模型并获取维度信息
            test_embedding = model.encode(["测试文本"])
            original_dim = len(test_embedding[0])
            
            model_info = {
                "model_name": Config.LOCAL_FALLBACK_MODEL,
                "model_path": Config.LOCAL_MODEL_PATH,
                "original_dimension": original_dim,
                "target_dimension": Config.TARGET_DIMENSION,
                "load_time": time.time(),
                "status": "local_loaded",
                "load_type": "local_offline"
            }
            
            logger.info(f"本地模型加载成功: {Config.LOCAL_MODEL_PATH}, 原始维度: {original_dim}")
            return model
            
        except Exception as e:
            logger.warning(f"本地模型加载失败: {e}")
            logger.info("将尝试从远程下载模型...")
    else:
        logger.info(f"本地模型路径不存在: {Config.LOCAL_MODEL_PATH}")
        logger.info("将尝试从远程下载模型...")
    
    # 2. 尝试加载主要远程模型
    try:
        logger.info(f"正在从远程加载主模型: {Config.PRIMARY_MODEL}")
        model = SentenceTransformer(Config.PRIMARY_MODEL)
        
        # 测试模型并获取维度信息
        test_embedding = model.encode(["测试文本"])
        original_dim = len(test_embedding[0])
        
        model_info = {
            "model_name": Config.PRIMARY_MODEL,
            "model_path": "remote",
            "original_dimension": original_dim,
            "target_dimension": Config.TARGET_DIMENSION,
            "load_time": time.time(),
            "status": "remote_loaded",
            "load_type": "remote_download"
        }
        
        logger.info(f"远程主模型加载成功: {Config.PRIMARY_MODEL}, 原始维度: {original_dim}")
        return model
        
    except Exception as e:
        logger.error(f"远程主模型加载失败: {e}")
        logger.info(f"尝试加载远程备用模型: {Config.FALLBACK_MODEL}")
        
        # 3. 尝试加载远程备用模型
        try:
            model = SentenceTransformer(Config.FALLBACK_MODEL)
            test_embedding = model.encode(["测试文本"])
            original_dim = len(test_embedding[0])
            
            model_info = {
                "model_name": Config.FALLBACK_MODEL,
                "model_path": "remote",
                "original_dimension": original_dim,
                "target_dimension": Config.TARGET_DIMENSION,
                "load_time": time.time(),
                "status": "fallback_loaded",
                "load_type": "remote_download",
                "fallback_reason": str(e)
            }
            
            logger.info(f"远程备用模型加载成功: {Config.FALLBACK_MODEL}, 原始维度: {original_dim}")
            return model
            
        except Exception as fallback_error:
            logger.error(f"所有模型加载失败")
            logger.error(f"本地模型: {Config.LOCAL_MODEL_PATH} - 不存在或加载失败")
            logger.error(f"远程主模型: {Config.PRIMARY_MODEL} - {e}")
            logger.error(f"远程备用模型: {Config.FALLBACK_MODEL} - {fallback_error}")
            
            raise RuntimeError(
                f"所有模型加载失败:\n"
                f"- 本地模型: {Config.LOCAL_MODEL_PATH} (不存在或无法加载)\n"
                f"- 远程主模型: {Config.PRIMARY_MODEL} ({e})\n"
                f"- 远程备用模型: {Config.FALLBACK_MODEL} ({fallback_error})\n"
                f"请检查网络连接或确保本地模型文件完整"
            )

def check_local_model_available() -> bool:
    """检查本地模型是否可用"""
    if not os.path.exists(Config.LOCAL_MODEL_PATH):
        return False
    
    # 检查关键文件是否存在 - 支持新的safetensors格式
    config_file = os.path.join(Config.LOCAL_MODEL_PATH, 'config.json')
    if not os.path.exists(config_file):
        logger.warning(f"本地模型缺少配置文件: {config_file}")
        return False
    
    # 检查模型权重文件 - 优先检查safetensors，其次pytorch_model.bin
    safetensors_file = os.path.join(Config.LOCAL_MODEL_PATH, 'model.safetensors')
    pytorch_file = os.path.join(Config.LOCAL_MODEL_PATH, 'pytorch_model.bin')
    
    if os.path.exists(safetensors_file):
        # 检查文件大小，确保不是空文件
        if os.path.getsize(safetensors_file) > 1024:  # 至少1KB
            return True
        else:
            logger.warning(f"model.safetensors文件太小: {os.path.getsize(safetensors_file)}字节")
    
    if os.path.exists(pytorch_file):
        # 检查文件大小，确保不是空文件
        if os.path.getsize(pytorch_file) > 1024:  # 至少1KB
            return True
        else:
            logger.warning(f"pytorch_model.bin文件太小: {os.path.getsize(pytorch_file)}字节")
    
    logger.warning(f"本地模型缺少有效的权重文件: {Config.LOCAL_MODEL_PATH}")
    return False

def adjust_embedding_dimension(embeddings: np.ndarray) -> np.ndarray:
    """调整嵌入向量维度到目标维度1536"""
    original_dim = embeddings.shape[1]
    target_dim = Config.TARGET_DIMENSION
    
    if original_dim == target_dim:
        return embeddings
    
    if original_dim < target_dim:
        # 如果原始维度小于目标维度，使用零填充
        padding = np.zeros((embeddings.shape[0], target_dim - original_dim))
        adjusted = np.concatenate([embeddings, padding], axis=1)
        logger.debug(f"维度扩展: {original_dim} -> {target_dim}")
    else:
        # 如果原始维度大于目标维度，使用主成分分析降维或简单截断
        adjusted = embeddings[:, :target_dim]
        logger.debug(f"维度截断: {original_dim} -> {target_dim}")
    
    return adjusted

# ======================================
# 应用生命周期管理
# ======================================

@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    global model
    
    # 启动时加载模型
    logger.info("FlowVO嵌入服务启动中...")
    
    # 输出路径调试信息
    debug_info = Config.get_debug_info()
    logger.info(f"路径配置信息: {debug_info}")
    
    try:
        model = load_embedding_model()
        logger.info("嵌入服务启动完成")
    except Exception as e:
        logger.error(f"嵌入服务启动失败: {e}")
        raise
    
    yield
    
    # 关闭时清理资源
    logger.info("FlowVO嵌入服务关闭中...")
    model = None
    logger.info("嵌入服务已关闭")

# ======================================
# FastAPI 应用初始化
# ======================================

app = FastAPI(
    title="FlowVO Embedding Tools Server",
    description="FlowVO平台的文本嵌入和切分服务 - 高性能AI文本处理",
    version="2.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS中间件
if Config.ENABLE_CORS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

# ======================================
# 数据模型
# ======================================

class TextRequest(BaseModel):
    """单个文本请求"""
    text: str = Field(..., min_length=1, max_length=Config.MAX_TEXT_LENGTH, description="要处理的文本")
    
    @validator('text')
    def validate_text(cls, v):
        if not v.strip():
            raise ValueError('文本不能为空')
        return v.strip()

class TextsRequest(BaseModel):
    """批量文本请求"""
    texts: List[str] = Field(..., min_items=1, max_items=Config.MAX_BATCH_SIZE, description="要处理的文本列表")
    
    @validator('texts')
    def validate_texts(cls, v):
        if not all(text.strip() for text in v):
            raise ValueError('文本列表中不能包含空文本')
        return [text.strip() for text in v]

class SplitRequest(BaseModel):
    """文本切分请求"""
    text: str = Field(..., min_length=1, max_length=Config.MAX_TEXT_LENGTH, description="要切分的文本")
    chunk_size: Optional[int] = Field(Config.DEFAULT_CHUNK_SIZE, ge=Config.MIN_CHUNK_SIZE, le=Config.MAX_CHUNK_SIZE, description="分块大小")
    chunk_overlap: Optional[int] = Field(Config.DEFAULT_CHUNK_OVERLAP, ge=0, description="分块重叠大小")
    
    @validator('chunk_overlap')
    def validate_overlap(cls, v, values):
        chunk_size = values.get('chunk_size', Config.DEFAULT_CHUNK_SIZE)
        if v >= chunk_size:
            raise ValueError('重叠大小不能大于等于分块大小')
        return v

class ChunksResponse(BaseModel):
    """切分结果响应"""
    chunks: List[str] = Field(..., description="切分后的文本块")
    count: int = Field(..., description="切分块数量")
    total_length: int = Field(..., description="原文本总长度")

class EmbeddingResponse(BaseModel):
    """单个嵌入向量响应"""
    embedding: List[float] = Field(..., description="嵌入向量")
    dimension: int = Field(..., description="向量维度")
    model_info: Dict[str, Any] = Field(..., description="模型信息")

class EmbeddingsResponse(BaseModel):
    """批量嵌入向量响应"""
    embeddings: List[List[float]] = Field(..., description="嵌入向量列表")
    count: int = Field(..., description="向量数量")
    dimension: int = Field(..., description="向量维度")
    model_info: Dict[str, Any] = Field(..., description="模型信息")

class ChunkEmbedding(BaseModel):
    """分块嵌入结果"""
    chunk: str = Field(..., description="文本块")
    embedding: List[float] = Field(..., description="对应的嵌入向量")
    index: int = Field(..., description="块索引")

class ChunkEmbeddingsResponse(BaseModel):
    """分块嵌入响应"""
    chunk_embeddings: List[ChunkEmbedding] = Field(..., description="分块嵌入结果列表")
    count: int = Field(..., description="分块数量")
    dimension: int = Field(..., description="向量维度")
    total_length: int = Field(..., description="原文本总长度")
    model_info: Dict[str, Any] = Field(..., description="模型信息")

class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str = Field(..., description="服务状态")
    service: str = Field(..., description="服务名称")
    version: str = Field(..., description="服务版本")
    model_info: Dict[str, Any] = Field(..., description="模型信息")
    timestamp: int = Field(..., description="时间戳")
    uptime: float = Field(..., description="运行时间(秒)")
    message: str = Field(..., description="状态消息")

# ======================================
# 工具函数
# ======================================

def get_text_splitter(chunk_size: int = None, chunk_overlap: int = None) -> RecursiveCharacterTextSplitter:
    """获取文本切分器"""
    return RecursiveCharacterTextSplitter(
        chunk_size=chunk_size or Config.DEFAULT_CHUNK_SIZE,
        chunk_overlap=chunk_overlap or Config.DEFAULT_CHUNK_OVERLAP,
        length_function=len,
        separators=["\n\n", "\n", "。", "！", "？", "；", ".", "!", "?", ";", " "]
    )

def encode_texts(texts: List[str]) -> List[List[float]]:
    """编码文本为嵌入向量"""
    if not model:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="嵌入模型未加载"
        )
    
    try:
        # 获取原始嵌入
        embeddings = model.encode(texts, convert_to_numpy=True)
        
        # 调整维度到1536
        adjusted_embeddings = adjust_embedding_dimension(embeddings)
        
        # 转换为列表格式
        return adjusted_embeddings.tolist()
        
    except Exception as e:
        logger.error(f"文本编码失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"文本编码失败: {str(e)}"
        )

# ======================================
# API 路由
# ======================================

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """健康检查接口"""
    try:
        if not model:
            raise Exception("模型未加载")
        
        # 测试模型功能
        test_embeddings = encode_texts(["健康检查测试文本"])
        
        uptime = time.time() - model_info.get('load_time', time.time())
        
        return HealthResponse(
            status="UP",
            service="flowvo-embedding-service",
            version="2.0.0",
            model_info=model_info,
            timestamp=int(time.time() * 1000),
            uptime=uptime,
            message="服务运行正常，模型工作正常"
        )
        
    except Exception as e:
        logger.error(f"健康检查失败: {e}")
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "status": "DOWN",
                "service": "flowvo-embedding-service",
                "version": "2.0.0",
                "error": str(e),
                "timestamp": int(time.time() * 1000),
                "message": "服务异常"
            }
        )

@app.get("/")
async def root():
    """根路径，返回服务信息"""
    local_model_available = check_local_model_available()
    
    return {
        "service": "FlowVO Embedding Tools Server",
        "version": "2.0.0",
        "description": "高性能文本嵌入和切分服务",
        "model_info": model_info,
        "local_model_status": {
            "available": local_model_available,
            "path": Config.LOCAL_MODEL_PATH,
            "model_name": Config.LOCAL_FALLBACK_MODEL
        },
        "features": [
            "文本向量化 (1536维度)",
            "批量文本处理",
            "智能文本切分",
            "文档切分嵌入",
            "健康检查监控",
            "本地模型支持 (离线运行)"
        ],
        "endpoints": {
            "health": "/health - 健康检查",
            "docs": "/docs - API文档",
            "split": "/split - 文本切分",
            "embed_one": "/embed_one - 单文本嵌入",
            "embed_batch": "/embed_batch - 批量文本嵌入",
            "split_embed": "/split_embed - 切分并嵌入"
        },
        "config": {
            "target_dimension": Config.TARGET_DIMENSION,
            "max_batch_size": Config.MAX_BATCH_SIZE,
            "max_text_length": Config.MAX_TEXT_LENGTH,
            "default_chunk_size": Config.DEFAULT_CHUNK_SIZE,
            "local_models_dir": Config.LOCAL_MODELS_DIR,
            "model_load_priority": [
                "1. 本地模型 (优先)",
                "2. 远程主模型",
                "3. 远程备用模型"
            ]
        },
        "model_loading_strategy": {
            "priority": "local_first",
            "local_path": Config.LOCAL_MODEL_PATH,
            "remote_primary": Config.PRIMARY_MODEL,
            "remote_fallback": Config.FALLBACK_MODEL,
            "offline_mode": local_model_available
        }
    }

@app.post("/split", response_model=ChunksResponse)
async def split_text(request: SplitRequest):
    """
    文本切分接口
    将长文本智能切分为较小的文本块
    """
    try:
        splitter = get_text_splitter(request.chunk_size, request.chunk_overlap)
        chunks = splitter.split_text(request.text)
        
        logger.info(f"文本切分完成: {len(chunks)}块, 原长度: {len(request.text)}")
        
        return ChunksResponse(
            chunks=chunks,
            count=len(chunks),
            total_length=len(request.text)
        )
        
    except Exception as e:
        logger.error(f"文本切分失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"文本切分失败: {str(e)}"
        )

@app.post("/embed_one", response_model=EmbeddingResponse)
async def embed_single_text(request: TextRequest):
    """
    单文本嵌入接口
    将单个文本转换为1536维嵌入向量
    """
    try:
        embeddings = encode_texts([request.text])
        embedding = embeddings[0]
        
        logger.info(f"单文本嵌入完成: 文本长度={len(request.text)}, 向量维度={len(embedding)}")
        
        return EmbeddingResponse(
            embedding=embedding,
            dimension=len(embedding),
            model_info=model_info
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"单文本嵌入失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"单文本嵌入失败: {str(e)}"
        )

@app.post("/embed_batch", response_model=EmbeddingsResponse)
async def embed_batch_texts(request: TextsRequest):
    """
    批量文本嵌入接口
    批量处理多个文本的嵌入向量生成
    """
    try:
        embeddings = encode_texts(request.texts)
        
        logger.info(f"批量文本嵌入完成: {len(request.texts)}个文本, 向量维度={len(embeddings[0]) if embeddings else 0}")
        
        return EmbeddingsResponse(
            embeddings=embeddings,
            count=len(embeddings),
            dimension=len(embeddings[0]) if embeddings else 0,
            model_info=model_info
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"批量文本嵌入失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"批量文本嵌入失败: {str(e)}"
        )

@app.post("/split_embed", response_model=ChunkEmbeddingsResponse)
async def split_and_embed(request: SplitRequest):
    """
    文档切分并嵌入接口
    将长文本切分后生成每个块的嵌入向量
    """
    try:
        # 文本切分
        splitter = get_text_splitter(request.chunk_size, request.chunk_overlap)
        chunks = splitter.split_text(request.text)
        
        if not chunks:
            raise ValueError("文本切分后没有生成任何块")
        
        # 批量嵌入
        embeddings = encode_texts(chunks)
        
        # 构建结果
        chunk_embeddings = [
            ChunkEmbedding(
                chunk=chunk,
                embedding=embedding,
                index=i
            )
            for i, (chunk, embedding) in enumerate(zip(chunks, embeddings))
        ]
        
        logger.info(f"文档切分嵌入完成: {len(chunks)}块, 原长度={len(request.text)}, 向量维度={len(embeddings[0]) if embeddings else 0}")
        
        return ChunkEmbeddingsResponse(
            chunk_embeddings=chunk_embeddings,
            count=len(chunk_embeddings),
            dimension=len(embeddings[0]) if embeddings else 0,
            total_length=len(request.text),
            model_info=model_info
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"文档切分嵌入失败: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"文档切分嵌入失败: {str(e)}"
        )

# ======================================
# 异常处理
# ======================================

@app.exception_handler(ValueError)
async def value_error_handler(request, exc):
    """值错误处理"""
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"detail": f"参数错误: {str(exc)}"}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """通用异常处理"""
    logger.error(f"未处理的异常: {exc}")
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"detail": "服务器内部错误"}
    )

if __name__ == "__main__":
    import uvicorn
    
    logger.info("启动FlowVO嵌入服务...")
    uvicorn.run(
        "embed_tools_server:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    )