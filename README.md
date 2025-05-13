# 安装node.js

npm create vue@latest .

npm install axios marked highlight.js

npm config set registry https://registry.npmmirror.com


# 安装UI组件
npm install @heroicons/vue @headlessui/vue

### 引入Heroicons图标组件
npm install @heroicons/vue

---
[embed_tools_server.py](../python/embed_tools_server.py)

# 引入langchain切分模型

### 安装必要的python库
1. pip install langchain
2. pip install langchain sentence-transformers fastapi uvicorn

### 启动虚拟环境Mac/Linux
source embedding_env/bin/activate
### 切到服务目录
cd embedding_service
### 启动服务
uvicorn embed_tools_server:app --host 0.0.0.0 --port 8000

### 启动后查看api
http://localhost:8000/docs

---
# 安装Milvus
### 下载docker-compose配置文件
wget https://github.com/milvus-io/milvus/releases/download/v2.5.10/milvus-standalone-docker-compose.yml -O docker-compose.yml
### 启动
sudo docker compose up -d

### 查看运行情况
docker-compose ps
      Name                     Command                  State                            Ports
--------------------------------------------------------------------------------------------------------------------
milvus-etcd         etcd -advertise-client-url ...   Up             2379/tcp, 2380/tcp
milvus-minio        /usr/bin/docker-entrypoint ...   Up (healthy)   9000/tcp
milvus-standalone   /tini -- milvus run standalone   Up             0.0.0.0:19530->19530/tcp, 0.0.0.0:9091->9091/tcp

### 停止和删除 Milvus
sudo docker compose down
sudo rm -rf volumes

### Milvus Insight（官方可视化工具）停止更新
#### 拉取镜像
docker pull milvusdb/milvus-insight:latest
#### 一键启动
docker run -p 3000:3000 -e MILVUS_URL={milvus server IP}:19530 zilliz/attu:v2.5
docker run --rm -p 3000:3000 milvusdb/milvus-insight:latest
启动 docker start 容器ID

### 社区可视化工具 attu
docker run -d --name attu -p 3000:3000 zilliz/attu



# ✅ 步骤流程（详细描述）
### 用户发送消息

前端发送用户输入的问题，例如：“请帮我查一下今天上海的天气”。

### 服务端封装消息（第一轮请求）

#### 构造 ChatCompletion 请求体：

包含用户消息。

附加当前支持的 functions 和 function descriptions。

明确提示：只回答是否需要调用 function call（是 或 否），不要包含其他解释内容。

### 大模型回复是否需要 function call

#### 模型返回：

"content": "是" 或 "content": "否"。

无需包含任何额外文本。

### 后端解析“是/否”响应

#### 如果是 "否"：

直接把用户问题再次发送给模型（不附加任何 function 信息），

获取回答，流式返回给前端。

#### 如果是 "是"：

###### 后端再次构造请求体：

包含用户原始消息。

附加所有 functions 的完整 JSON 格式。

明确提示模型只返回 function_call 字段中的调用信息（JSON）。

### 模型返回 Function Call JSON

#### 示例返回：

json
`{
  "function_call": {
    "name": "getWeather",
    "arguments": {
      "city": "上海"
    }
  }
}`
### 后端执行对应方法

后端使用反射（或统一注册的 handlerMap）找到对应方法。

执行调用，获取返回值。

### 返回 Function 结果

将函数返回值作为回答内容，流式发送到前端页面。

# 🔄 对话流逻辑图（文本图）

                        用户输入问题
                            ↓
    服务端封装问题 + 发送完整 functions JSON + 原始消息给模型                    
                            ↓                                   
                模型返回 function_call JSON          
                            ↓                                   
                  后端解析并通过反射调用方法             
                            ↓
                函数执行结果作为回答流式返回前端

---
# MCP server
1、实现数据库MCP server (HTTP+JSON‑RPC 调用)