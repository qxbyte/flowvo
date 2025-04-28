[embed_tools_server.py](../python/embed_tools_server.py)

# 引入langchain切分模型
### 安装必要的python库
1. pip install langchain
2. pip install langchain sentence-transformers fastapi uvicorn

### 启动服务
uvicorn embed_tools_server:app --host 0.0.0.0 --port 8000

### 启动后查看api
http://localhost:8000/docs