#!/bin/bash

# ============================================
# 配置化系统演示
# 展示本体库/活动库/关系库 + 项目模板功能
# ============================================

cd /workspaces/vscodeItem

echo "╔════════════════════════════════════════════════════════╗"
echo "║      配置化系统演示 - 基于模板创建项目                  ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

java -jar target/ontology-factory-2.0.0.jar <<'EOF'
templates
template show waterfall_medical_device
template create waterfall_medical_device med_dev_001 "心脏监护仪软件v1.0" 10
template create agile_mobile_app mobile_app_001 "电商App" 7
projects
switch med_dev_001
phase transition DESIGN
phase transition DEVELOPMENT
switch mobile_app_001
phase transition TESTING
phase transition DEVELOPMENT
status
exit
EOF
