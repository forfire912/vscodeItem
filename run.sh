#!/bin/bash

# 编译并运行动力学决策网络系统

echo "======================================"
echo "编译项目..."
echo "======================================"

mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "编译失败!"
    exit 1
fi

echo ""
echo "======================================"
echo "启动系统..."
echo "======================================"
echo ""

java -jar target/ontology-dynamic-network-1.0.0.jar
