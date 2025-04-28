🟢 一、环境准备
1. 安装 Python
   推荐 Python 3.9 ~ 3.11（3.12也可以）
Windows 一定要记得安装时勾选 “Add Python to PATH”

检查安装：
python --version
pip --version

2. 建议安装虚拟环境（可选但推荐）
   让环境干净，不污染全局。
python -m venv embedding_env
# 激活（Windows）
embedding_env\Scripts\activate
# 或 Mac/Linux
source embedding_env/bin/activate

3. 安装必要 Python 库 

pip install -U pip

pip install fastapi uvicorn sentence-transformers

# 推荐使用国内源加速（可选）
pip install -i https://pypi.tuna.tsinghua.edu.cn/simple fastapi uvicorn sentence-transformers

🟢 二、编写 Python Embedding 服务
建议新建一个目录：
mkdir embedding_service && cd embedding_service

[embed_server.py](../python/embed_server.py)
新建 embed_server.py，内容如下：
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

🟢 三、启动 HTTP 服务
uvicorn embed_server:app --host 0.0.0.0 --port 8000
uvicorn embed_tools_server:app --host 0.0.0.0 --port 8000
看到：
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
代表启动成功！

无法启动可以设置终端代理：
curl https://huggingface.co
无法连接huggingface时
export http_proxy=http://127.0.0.1:7890

export https_proxy=http://127.0.0.1:7890

停止服务：pkill -f "uvicorn embed_server:app"

🟢 四、测试接口（用 curl/Postman/Java）
1. curl 命令行测试
   curl -X POST http://localhost:8000/embed \
   -H "Content-Type: application/json" \
   -d "{\"input\": [\"你好世界\", \"hello world\"]}"
   会返回类似
{
"embeddings": [
[0.123, 0.456, ...],   // 第1个文本
[0.789, 0.654, ...]    // 第2个文本
]
}
2. 
2. Postman 测试
   URL: http://localhost:8000/embed

POST, JSON body:
{
"input": ["你是谁", "hello"]
}
点击 Send，看到 embeddings 字段

3. Java 调用代码片段
   RestTemplate restTemplate = new RestTemplate();
   String url = "http://localhost:8000/embed";
   Map<String, Object> req = Map.of("input", List.of("你好世界", "hello world"));
   Map result = restTemplate.postForObject(url, req, Map.class);
   List<List<Double>> embeddings = (List<List<Double>>) result.get("embeddings");

🟢 五、常见问题与技巧
   首次启动会自动下载模型（较慢，耐心等待一次）

想用英文或多语言？

把 MODEL_NAME 改成 "thenlper/gte-base" 或 "sentence-transformers/all-MiniLM-L6-v2"


可直接部署在服务器/云/局域网，Java 可跨机调用

如遇“端口被占用”，改端口即可 --port 8080 等

🟢 六、进阶优化建议
支持GPU:

安装 torch + cuda，自动用GPU

高并发/服务化:

用 gunicorn/uvicorn worker 多进程/多线程

可直接封装成 Docker 镜像

安全/认证:

增加接口密钥/白名单等安全防护

🟢 七、可选：一键 Docker 部署本地 Python Embedding 服务