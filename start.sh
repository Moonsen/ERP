#!/bin/bash

echo "正在启动跨境仓库发货管理系统..."

# 启动后端
echo "正在启动后端服务器 (Port 3000)..."
cd server
npm install
npm start &
BACKEND_PID=$!

# 启动前端
echo "正在启动前端客户端..."
cd ../client
npm install
npm run dev &
FRONTEND_PID=$!

echo "系统启动中！"
echo "后端地址: http://localhost:3000"
echo "前端地址: http://localhost:5173 (或控制台输出的地址)"
echo ""
echo "提示: 如果 npm 报错，请确保已安装 Node.js 环境。"

# 保持脚本运行，以便用户看到输出或方便关闭
wait $BACKEND_PID $FRONTEND_PID
