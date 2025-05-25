#!/bin/bash

echo "===== FlowVO UI 启动脚本 ====="

# 检查Node.js版本
echo "检查Node.js版本..."
node_version=$(node -v)
echo "当前Node.js版本: $node_version"

# 检查npm版本
echo "检查npm版本..."
npm_version=$(npm -v)
echo "当前npm版本: $npm_version"

# 安装依赖
echo "正在安装依赖..."
npm install

# 检查后端API连接
echo "检查后端API连接..."
curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/api/orders?page=1

if [ $? -ne 0 ]; then
  echo "警告: 后端API服务似乎未启动，请确保后端服务已运行在端口8084"
  echo "您仍然可以启动前端，但某些功能可能无法正常工作"
  echo "按任意键继续，或按Ctrl+C退出"
  read -n 1
else
  echo "后端API连接正常"
fi

# 启动开发服务器
echo "启动开发服务器..."
npm run dev 