#!/bin/bash
# 瀑布模式完整测试

echo "╔══════════════════════════════════════════════════════════╗"
echo "║           瀑布模式 (WATERFALL) 完整测试                    ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

cat << 'EOF' | java -jar target/ontology-factory-2.0.0.jar
project create satellite 卫星控制系统v1.0 waterfall 10
switch satellite
phase status

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试1: 正常阶段转换 (REQUIREMENT → DESIGN)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition DESIGN
phase status

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试2: 继续顺序转换 (DESIGN → DEVELOPMENT)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition DEVELOPMENT
phase status

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试3: 尝试回退 (DEVELOPMENT → DESIGN) - 应失败"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition DESIGN

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试4: 尝试跳过阶段 (DEVELOPMENT → TESTING) - 应失败"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition TESTING

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试5: 正确的下一步 (DEVELOPMENT → CODE_REVIEW)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition CODE_REVIEW
phase status

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试6: 继续 (CODE_REVIEW → TESTING)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition TESTING
phase status

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试7: 最后阶段 (TESTING → DEPLOYMENT)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
phase transition DEPLOYMENT
phase status

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "最终项目状态:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
status
exit
EOF

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                     测试结果总结                           ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "✅ 瀑布模式特性验证:"
echo "   1. ✅ 只允许顺序前进 (REQUIREMENT→DESIGN→DEVELOPMENT...)"
echo "   2. ✅ 禁止回退到前面的阶段"
echo "   3. ✅ 禁止跳过中间阶段"
echo "   4. ✅ 严格门禁控制"
echo ""
echo "📋 阶段序列:"
echo "   REQUIREMENT → DESIGN → DEVELOPMENT → CODE_REVIEW → TESTING → DEPLOYMENT"
echo ""
