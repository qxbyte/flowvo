# 安装Milvus
### 下载docker-compose配置文件
wget https://github.com/milvus-io/milvus/releases/download/v2.5.12/milvus-standalone-docker-compose.yml -O docker-compose.yml
或者
curl -L https://github.com/milvus-io/milvus/releases/download/v2.5.12/milvus-standalone-docker-compose.yml -o docker-compose.yml
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


