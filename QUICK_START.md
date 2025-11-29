# 🚀 5分钟快速上手指南

## 第一步: 运行系统 (30秒)

```bash
java -jar target/ontology-factory-2.0.0.jar
```

看到这个界面就成功了:
```
╔════════════════════════════════════════════════════════╗
║  软件工厂系统 v2.0 - 多项目并发 + 多模式支持             ║
║  支持: 本体库/活动库/关系库 + 项目模板                 ║
╚════════════════════════════════════════════════════════╝

🔄 正在加载系统配置...
  ✓ 已加载 11 个本体定义
  ✓ 已加载 27 个活动定义
  ✓ 已加载 16 个关系定义
  ✓ 已加载 4 个项目模板
✅ 配置加载完成!

ontology>
```

---

## 第二步: 查看可用模板 (1分钟)

```bash
ontology> templates
```

你会看到4个预置模板:
- `waterfall_medical_device` - 医疗设备(瀑布模式)
- `waterfall_financial_core` - 金融核心(瀑布模式)
- `agile_mobile_app` - 移动应用(敏捷模式)
- `agile_startup_mvp` - 创业MVP(敏捷模式)

---

## 第三步: 创建项目 (1分钟)

```bash
ontology> template create waterfall_medical_device MED001 "医疗设备v1.0" 10
```

看到这个输出:
```
✅ 创建项目: 医疗设备v1.0 [WATERFALL]
  ✓ 初始化本体集: 9 个
  ✓ 初始化活动集: 22 个
  ✓ 初始化关系集: 9 个
  ✓ 决策网络已初始化: 9 个规则   ← 决策网络自动启动!
```

---

## 第四步: 创建对象 (1分钟)

```bash
# 创建需求
ontology> create_object req_func_001 requirement

# 创建设计文档
ontology> create_object design_hw_001 design_doc

# 创建测试用例
ontology> create_object test_unit_001 test_case
```

---

## 第五步: 触发决策网络 (1.5分钟)

### 测试1: 需求批准 → 智能建议
```bash
ontology> update req_func_001 status approved
```

看到智能建议:
```
📊 [ImplementsRule] requirement 已批准,可以开始实现
    → 建议创建设计文档   ← 这是决策网络的智能建议!
```

### 测试2: 测试通过 → 状态更新
```bash
ontology> update test_unit_001 status passed
```

看到:
```
📊 [TestsRule] 测试用例状态: passed
    ✓ 测试通过,更新被测对象的测试状态
```

---

## 第六步: 查看对象列表 (30秒)

```bash
ontology> list_objects
```

看到所有对象和状态:
```
╔════════════════════════════════════════════════════════╗
║          项目对象列表 - MED001                  ║
╚════════════════════════════════════════════════════════╝

📦 design_doc:
  - design_hw_001

📦 requirement:
  - req_func_001
      status = approved

📦 test_case:
  - test_unit_001
      status = passed

总计: 3 个对象
```

---

## 🎉 恭喜! 你已经掌握了核心功能!

### 你刚才体验了:
- ✅ 基于模板快速创建项目
- ✅ 决策网络自动初始化 (9个规则)
- ✅ 创建本体对象实例
- ✅ 对象更新触发智能建议
- ✅ 对象状态管理

---

## 💡 接下来可以尝试:

### 1. 查看知识库
```bash
ontology> library ontologies     # 11个本体定义
ontology> library activities     # 27个活动定义
ontology> library relationships  # 16个关系定义
```

### 2. 创建缺陷并修复
```bash
ontology> create_object bug_001 bug
ontology> update bug_001 severity critical
ontology> update bug_001 status fixed
# 会触发 FixesRule,建议回归测试
```

### 3. 查看其他模板
```bash
ontology> template show agile_mobile_app
```

### 4. 创建多个项目
```bash
ontology> template create agile_mobile_app APP001 "电商App" 8
ontology> projects    # 列出所有项目
ontology> switch MED001    # 切换回医疗设备项目
```

---

## 📚 想了解更多?

### 初学者
- [README.md](README.md) - 项目总览
- [DECISION_NETWORK_GUIDE.md](DECISION_NETWORK_GUIDE.md) - 详细使用指南

### 开发者
- [DECISION_NETWORK_DESIGN.md](DECISION_NETWORK_DESIGN.md) - 架构设计
- [UPGRADE_PLAN.md](UPGRADE_PLAN.md) - 完整设计文档

### 测试人员
- [DECISION_NETWORK_TEST_REPORT.md](DECISION_NETWORK_TEST_REPORT.md) - 测试报告
- `./test_decision_network.sh` - 自动化测试脚本

### 项目经理
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - 项目总结
- [RELEASE_NOTES_v2.1.0.md](RELEASE_NOTES_v2.1.0.md) - 发布说明

---

## 🆘 需要帮助?

### 在系统内
```bash
ontology> help    # 查看所有命令
```

### 常见问题
**Q: 规则没有触发?**  
A: 检查对象类型和属性是否匹配规则的触发条件

**Q: 如何查看决策网络生成了哪些规则?**  
A: 创建项目时会显示 "决策网络已初始化: X 个规则"

**Q: 如何添加自定义规则?**  
A: 在 `config/relationship_library.yml` 中定义新关系,系统会自动生成规则

---

## 🎯 核心概念速记

### 本体 (Ontology)
软件开发中的实体类型,如:
- requirement (需求)
- design_doc (设计文档)
- code_module (代码模块)
- test_case (测试用例)
- bug (缺陷)

### 活动 (Activity)
软件开发中的任务,如:
- gather_requirements (收集需求)
- create_architecture_design (创建架构设计)
- implement_module (实现模块)
- execute_unit_test (执行单元测试)

### 关系 (Relationship)
对象间的关联,如:
- implements (实现)
- depends_on (依赖于)
- tests (测试)
- fixes (修复)
- blocks (阻塞)

### 决策网络 (Decision Network)
自动规则引擎:
- 监听对象更新事件
- 评估匹配的规则
- 提供智能建议

### 项目模板 (Project Template)
预定义的项目配置:
- 包含特定的本体集
- 包含特定的活动集
- 包含特定的关系集
- 自动生成决策网络

---

**祝你使用愉快!** 🎉

如有问题,随时输入 `help` 查看命令,或查看文档目录中的详细指南。
