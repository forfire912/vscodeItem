#!/bin/bash

# 快速演示脚本

echo "======================================"
echo "编译项目..."
echo "======================================"

mvn clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo "编译失败!"
    exit 1
fi

echo ""
echo "======================================"
echo "运行演示场景..."
echo "======================================"
echo ""

# 使用Maven运行
mvn exec:java -Dexec.mainClass="com.softwarefactory.ontology.Main" -q
