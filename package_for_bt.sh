#!/bin/bash

# 确保脚本在出错时停止
set -e

echo "开始准备宝塔部署包..."

# 1. 构建前端
echo "正在构建前端代码..."
cd client
npm install
npm run build
cd ..

# 2. 创建临时部署目录
echo "整理部署文件..."
rm -rf deploy_temp
mkdir -p deploy_temp/server
mkdir -p deploy_temp/client/dist

# 3. 复制后端文件 (排除 node_modules 和本地数据库)
cp -R server/* deploy_temp/server/
rm -rf deploy_temp/server/node_modules
rm -f deploy_temp/server/warehouse.db

# 4. 复制前端构建产物
cp -R client/dist/* deploy_temp/client/dist/

# 5. 压缩打包
echo "正在生成压缩包..."
rm -f deploy_package.zip
cd deploy_temp
zip -r ../deploy_package.zip .
cd ..

# 6. 清理
rm -rf deploy_temp

echo "============================================"
echo "打包完成！"
echo "部署包位置: /Users/mhuang/Desktop/文稿/软件/erp/deploy_package.zip"
echo "============================================"
echo "您可以直接将 deploy_package.zip 上传到宝塔面板并解压。"
