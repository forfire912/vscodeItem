#!/bin/bash

# ============================================
# 交互式命令行测试脚本
# ============================================

echo "╔════════════════════════════════════════════════════════╗"
echo "║         软件工厂系统 v2.1 - 命令行测试演示              ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

cd /workspaces/vscodeItem

# 使用管道输入命令进行测试
java -jar target/ontology-factory-2.0.0.jar <<'TESTCOMMANDS'
help
library ontologies
templates
template show waterfall_medical_device
template create waterfall_medical_device med_001 "心脏监护仪v1.0" 10
template create agile_mobile_app app_001 "电商App" 7
projects
switch med_001
status
phase transition DESIGN
phase status
phase transition DEVELOPMENT
phase transition CODE_REVIEW
switch app_001
status
phase transition TESTING
phase transition DEVELOPMENT
status
projects
exit
TESTCOMMANDS

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║                  测试完成!                             ║"
echo "╚════════════════════════════════════════════════════════╝"
