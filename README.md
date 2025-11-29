# 动态本体软件工厂 v2.1

> 事件驱动的智能软件开发系统 - 支持多项目并发、多工作流模式、配置化知识库与决策网络

---

## 🚀 核心特性

### v2.1 新特性 - 决策网络层
- ✅ **事件驱动架构**: 对象更新自动触发决策网络
- ✅ **智能规则引擎**: 从关系库自动生成规则
- ✅ **实时建议系统**: 需求批准 → 建议创建设计文档
- ✅ **规则可扩展**: 支持自定义规则,零代码侵入

### v2.0 基础架构
- ✅ **多项目并发**: 同时管理多个不同模式的项目
- ✅ **三种工作流**: 瀑布模式、敏捷模式、混合模式
- ✅ **配置化知识库**: 11个本体、27个活动、16个关系
- ✅ **项目模板系统**: 4个预置模板,一键创建项目

---

## 📊 系统架构

```
┌─────────────────────────────────────────────────────┐
│  Layer 4: 多项目管理层                                │
│  - ProjectManager (多项目并发调度)                    │
│  - ResourceScheduler (资源分配与冲突解决)             │
│  - TemplateManager (模板加载与项目生成)               │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 3: 配置知识库                                  │
│  - OntologyLibrary (11个本体定义)                    │
│  - ActivityLibrary (27个活动定义)                    │
│  - RelationshipLibrary (16个关系定义)                │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 2: 决策网络层 (v2.1 新增) ✨                   │
│  - DecisionNetworkEngine (事件队列 + 规则评估)        │
│  - RuleFactory (关系 → 规则自动生成)                  │
│  - DecisionRule (5个专用规则 + 1个通用规则)            │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 1: 项目上下文                                  │
│  - ProjectContext (本体集/活动集/关系集)              │
│  - ProcessModeEngine (工作流引擎)                     │
│  - Object Storage (对象实例存储)                      │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 快速开始

### 1. 编译项目
```bash
mvn clean package -DskipTests
```

### 2. 运行系统
```bash
java -jar target/ontology-factory-2.0.0.jar
```

### 3. 创建项目 (基于模板)
```bash
ontology> template create waterfall_medical_device MED001 "医疗设备v1.0" 10
```

输出:
```
✅ 创建项目: 医疗设备v1.0 [WATERFALL]
  ✓ 初始化本体集: 9 个
  ✓ 初始化活动集: 22 个
  ✓ 初始化关系集: 9 个
  ✓ 决策网络已初始化: 9 个规则   ← 决策网络自动启动!
```

### 4. 创建对象并触发决策网络
```bash
# 创建需求
ontology> create_object req_001 requirement

# 更新需求状态 → 触发智能建议
ontology> update req_001 status approved
  📊 [ImplementsRule] requirement 已批准,可以开始实现
      → 建议创建设计文档   ← 智能建议!
```

---

## 📚 文档导航

### 核心文档
- [UPGRADE_PLAN.md](UPGRADE_PLAN.md) - v2.0 升级规划 (1401行详细设计)
- [DECISION_NETWORK_DESIGN.md](DECISION_NETWORK_DESIGN.md) - 决策网络架构设计
- [DECISION_NETWORK_TEST_REPORT.md](DECISION_NETWORK_TEST_REPORT.md) - 决策网络测试报告
- [DECISION_NETWORK_GUIDE.md](DECISION_NETWORK_GUIDE.md) - 决策网络使用指南

### 配置文件
- `config/ontology_library.yml` - 本体库 (11个定义)
- `config/activity_library.yml` - 活动库 (27个定义)
- `config/relationship_library.yml` - 关系库 (16个定义)
- `config/templates/` - 项目模板 (4个模板)

---

## 🔥 核心功能演示

### 1. 决策网络工作原理

```
对象更新 (update req_001 status approved)
    ↓
触发事件 (OntologyEvent)
    ↓
规则评估 (ImplementsRule, DependsOnRule, etc.)
    ↓
智能建议 (建议创建设计文档)
```

### 2. 内置规则

| 规则 | 触发条件 | 行为 |
|------|---------|------|
| **ImplementsRule** | requirement.status = approved | 建议创建设计文档 |
| **TestsRule** | test_case.status = passed | 更新被测对象状态 |
| **FixesRule** | bug.status = fixed | 建议触发回归测试 |
| **DependsOnRule** | 任意对象状态变化 | 追踪依赖链 |
| **BlocksRule** | 任意对象状态变化 | 传播阻塞状态 |
| **GenericRule** | 匹配关系定义 | 通用关系处理 |

### 3. 配置驱动的规则生成

```yaml
# relationship_library.yml
- id: implements
  name: 实现
  description: 设计实现需求,代码实现设计
  from_object_types: [design_doc, code_module]
  to_object_types: [requirement, design_doc]
```

自动生成:
```java
new ImplementsRule(relationship)
```

---

## 🎮 可用命令

### 库管理
```bash
library ontologies       # 查看11个本体
library activities       # 查看27个活动
library relationships    # 查看16个关系
```

### 模板管理
```bash
templates                                    # 列出4个模板
template show waterfall_medical_device       # 查看模板详情
template create <templateId> <projectId> <name> <priority>
```

### 对象管理 (v2.1 新增)
```bash
create_object <objectId> <ontologyType>      # 创建对象
update <objectId> <attr> <value>             # 更新对象 → 触发决策网络!
list_objects                                 # 列出所有对象
```

### 项目管理
```bash
projects                                     # 列出所有项目
switch <projectId>                           # 切换项目
status                                       # 查看当前项目状态
```

---

## 🧪 测试场景

### 运行自动化测试
```bash
./test_decision_network.sh
```

### 测试步骤
1. 创建项目 (waterfall_medical_device)
2. 创建需求对象 (requirement)
3. 批准需求 → 触发 ImplementsRule
4. 创建测试用例 (test_case)
5. 测试通过 → 触发 TestsRule
6. 创建缺陷 (bug)
7. 修复缺陷 → 触发 FixesRule

### 预期输出
```
📊 [ImplementsRule] requirement 已批准,可以开始实现
    → 建议创建设计文档
📊 [TestsRule] 测试用例状态: passed
    ✓ 测试通过,更新被测对象的测试状态
```

---

## 📦 配置文件结构

### 本体库 (11个本体)
- requirement (需求)
- design_doc (设计文档)
- code_module (代码模块)
- test_case (测试用例)
- bug (缺陷)
- release (发布)
- user_story (用户故事)
- task (任务)
- sprint (迭代)
- backlog (待办项)
- deployment (部署)

### 活动库 (27个活动)
按6个阶段组织:
- REQUIREMENT: 需求分析、评审、变更管理
- DESIGN: 架构设计、详细设计、设计评审
- IMPLEMENTATION: 编码、代码审查、集成
- TESTING: 单元测试、集成测试、系统测试
- DEPLOYMENT: 打包、部署、验证
- MAINTENANCE: 监控、缺陷修复、优化

### 关系库 (16个关系)
- implements (实现)
- depends_on (依赖)
- tests (测试)
- fixes (修复)
- blocks (阻塞)
- approved_by (批准人)
- reviews (评审)
- traces_to (追溯到)
- found_by (发现于)
- assigned_to (分配给)
- belongs_to (属于)
- derives_from (派生自)
- impacts (影响)
- extends (扩展)
- refines (细化)
- supersedes (替代)

### 项目模板 (4个模板)
1. **waterfall_medical_device**: 医疗设备(瀑布模式) - 严格流程,FDA合规
2. **waterfall_financial_core**: 金融核心系统(瀑布模式) - 审计追踪
3. **agile_mobile_app**: 移动应用(敏捷模式) - Scrum框架
4. **agile_startup_mvp**: 创业MVP(敏捷模式) - 极简流程

---

## 🏗️ 技术栈

- **Java**: 17
- **Maven**: 3.6+
- **Jackson**: 2.15.2 (YAML解析)
- **SnakeYAML**: 2.0 (配置管理)
- **架构模式**:
  - 事件驱动架构 (Event-Driven)
  - 工厂模式 (RuleFactory)
  - 策略模式 (DecisionRule)
  - 观察者模式 (Event System)

---

## 📈 版本历史

### v2.1 (2024) - 决策网络层
- ✅ 实现事件驱动的决策网络
- ✅ 自动规则生成机制 (RuleFactory)
- ✅ 5个专用规则 + 1个通用规则
- ✅ 对象管理命令 (create_object, list_objects)
- ✅ 完整测试报告与使用指南

### v2.0 (2024) - 多项目多模式
- ✅ 多项目并发管理
- ✅ 三种工作流模式 (瀑布/敏捷/混合)
- ✅ 配置化知识库 (11+27+16)
- ✅ 项目模板系统 (4个模板)
- ✅ 资源调度与冲突解决

### v1.0 - 单项目瀑布模式
- 基础本体建模
- 瀑布流程引擎
- 简单活动管理

---

## 🔮 未来规划

### 短期 (1-2天)
- [ ] 增强 FixesRule 输出反馈
- [ ] 完善 BlocksRule 依赖追踪
- [ ] 添加规则优先级机制

### 中期 (1周)
- [ ] 实现 ActivityExecutor (自动执行活动)
- [ ] 添加事件历史记录功能
- [ ] 规则配置化 (从YAML加载规则逻辑)

### 长期 (1个月)
- [ ] 可视化决策网络 (Mermaid图)
- [ ] 机器学习驱动的规则推荐
- [ ] 分布式决策网络 (多项目协同)

---

## 🤝 贡献指南

### 添加新规则
1. 在 `relationship_library.yml` 中定义关系
2. 实现 `DecisionRule` 接口
3. 在 `RuleFactory` 中注册规则
4. 编写测试场景验证

### 添加新模板
1. 在 `config/templates/` 创建 YAML 文件
2. 定义 ontologies, activities, relationships
3. 设置 workflow_mode
4. 使用 `template create` 测试

---

## 📝 许可证

MIT License

---

## 👨‍💻 作者

GitHub Copilot

---

## 📞 支持

- 📖 查看 [DECISION_NETWORK_GUIDE.md](DECISION_NETWORK_GUIDE.md)
- 🧪 运行 `./test_decision_network.sh`
- 💬 输入 `help` 查看所有命令
