#!/bin/bash

# 决策网络测试脚本
# 测试目标: 验证对象更新能够触发决策网络规则评估

echo "================================"
echo "决策网络完整功能测试"
echo "================================"
echo ""

# 启动程序并使用管道输入命令
java -jar target/ontology-factory-2.0.0.jar <<EOF

# 1. 查看可用模板
templates

# 2. 基于 waterfall_medical_device 模板创建项目
template create waterfall_medical_device MED001 "医疗设备原型v1.0" 10

# 3. 查看项目状态
status

# 4. 创建需求对象
create_object req_functional_001 requirement

# 5. 创建设计对象
create_object design_hardware_001 design_doc

# 6. 列出所有对象
list_objects

# 7. 更新需求状态为 "approved" - 应该触发 ImplementsRule
update req_functional_001 status approved

# 8. 再次列出对象查看变化
list_objects

# 9. 创建测试用例对象
create_object test_unit_001 test_case

# 10. 更新测试状态为 "passed" - 应该触发 TestsRule
update test_unit_001 status passed

# 11. 创建缺陷对象
create_object bug_critical_001 bug

# 12. 更新缺陷状态为 "fixed" - 应该触发 FixesRule
update bug_critical_001 status fixed

# 13. 查看最终状态
list_objects

# 退出
exit
EOF

echo ""
echo "================================"
echo "测试完成!"
echo "================================"
