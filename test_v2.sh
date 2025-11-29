#!/bin/bash
# v2.0 完整功能测试脚本

echo "╔══════════════════════════════════════════════════════════╗"
echo "║  软件工厂系统 v2.0 - 完整功能测试                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 定义测试命令序列
cat << 'EOF' | java -jar target/ontology-factory-2.0.0.jar
help
project create satellite 卫星控制系统v1.0 waterfall 10
project create ecommerce 电商AppSprint-12 agile 7
project create erp ERP系统升级v3.0 hybrid 5
projects
resources status
switch satellite
phase status
phase transition DESIGN
switch ecommerce
phase status
phase transition TESTING
switch erp
phase status
project info satellite
project info ecommerce
project info erp
status
exit
EOF

echo ""
echo "✅ 测试完成!"
