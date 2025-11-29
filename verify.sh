#!/bin/bash

echo "======================================"
echo "项目验证脚本"
echo "======================================"
echo ""

# 检查Java版本
echo "1. 检查Java环境..."
java -version
if [ $? -ne 0 ]; then
    echo "❌ Java未安装或版本不正确"
    exit 1
fi
echo "✅ Java环境正常"
echo ""

# 检查Maven
echo "2. 检查Maven环境..."
mvn -version
if [ $? -ne 0 ]; then
    echo "❌ Maven未安装"
    exit 1
fi
echo "✅ Maven环境正常"
echo ""

# 检查源文件
echo "3. 检查源文件..."
JAVA_FILES=$(find src -name "*.java" | wc -l)
echo "   Java源文件数量: $JAVA_FILES"
if [ $JAVA_FILES -lt 10 ]; then
    echo "❌ 源文件不完整"
    exit 1
fi
echo "✅ 源文件完整"
echo ""

# 检查配置文件
echo "4. 检查配置文件..."
if [ ! -f "config/ontology.yml" ]; then
    echo "❌ 缺少ontology.yml"
    exit 1
fi
if [ ! -f "config/actions.yml" ]; then
    echo "❌ 缺少actions.yml"
    exit 1
fi
if [ ! -f "config/rules.yml" ]; then
    echo "❌ 缺少rules.yml"
    exit 1
fi
echo "✅ 配置文件完整"
echo ""

# 编译项目
echo "5. 编译项目..."
mvn clean compile -q -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译成功"
echo ""

# 打包项目
echo "6. 打包项目..."
mvn package -q -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 打包失败"
    exit 1
fi
echo "✅ 打包成功"
echo ""

# 检查JAR文件
echo "7. 检查生成的JAR文件..."
if [ ! -f "target/ontology-dynamic-network-1.0.0.jar" ]; then
    echo "❌ JAR文件未生成"
    exit 1
fi
JAR_SIZE=$(ls -lh target/ontology-dynamic-network-1.0.0.jar | awk '{print $5}')
echo "   JAR文件大小: $JAR_SIZE"
echo "✅ JAR文件生成成功"
echo ""

echo "======================================"
echo "✅ 所有检查通过!"
echo "======================================"
echo ""
echo "项目已就绪,可以运行以下命令启动:"
echo "  ./run.sh"
echo "  或"
echo "  java -jar target/ontology-dynamic-network-1.0.0.jar"
echo ""
