# 

安装node.js

npm create vue@latest .

npm install axios marked highlight.js

npm config set registry https://registry.npmmirror.com


# 安装UI组件
npm install @heroicons/vue @headlessui/vue

### 引入Heroicons图标组件
npm install @heroicons/vue


# Qdrant安装
docker logout
docker login

docker pull qdrant/qdrant
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant

### 验证启动：
http://localhost:6333
### Qdrant UI
http://localhost:6333/dashboard

### 数据持久化到
docker run -p 6333:6333 -p 6334:6334 -v qdrant_data:/qdrant/storage qdrant/qdrant

### 查看docker挂载盘位置
docker volume inspect qdrant_data

### 安装嵌入模型  HuggingFace embedding-server(github:https://github.com/huggingface/text-embeddings-inference)
#### docker 安装
model=BAAI/bge-large-en-v1.5
docker run --gpus all -p 8080:80 -v $volume:/data --pull always ghcr.io/huggingface/text-embeddings-inference:1.7 --model-id $model

#### 测试：
curl 127.0.0.1:8080/embed \
-X POST \
-d '{"inputs":"What is Deep Learning?"}' \
-H 'Content-Type: application/json'