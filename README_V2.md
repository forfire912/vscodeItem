# 软件工厂系统 v2.0

## 🎉 重大升级: 多项目并发 + 多模式支持

基于动态本体映射与动力学决策网络的软件工厂实现方法 v2.0

### ✨ 新特性

#### 1️⃣ **多项目并发管理**
- ✅ 同时运行多个项目(瀑布 + 敏捷 + 混合)
- ✅ 项目优先级调度(1-10级)
- ✅ 项目隔离(独立上下文/状态)
- ✅ 项目暂停/恢复/删除

#### 2️⃣ **三种流程模式**
- **瀑布模式** (WATERFALL): 严格阶段门禁,适合航天/国防项目
- **敏捷模式** (AGILE): 灵活迭代,适合互联网/创业项目
- **混合模式** (HYBRID): 可配置组合,适合企业级项目

#### 3️⃣ **智能资源调度**
- ✅ 共享资源池(测试台/人员/服务)
- ✅ 优先级抢占(高优先级可抢占资源)
- ✅ 资源使用监控
- ✅ 自动资源分配/释放

### 🚀 快速开始

#### 编译打包
\`\`\`bash
mvn clean package
\`\`\`

#### 运行演示
\`\`\`bash
echo "demo" | java -jar target/ontology-factory-2.0.0.jar
\`\`\`

#### 交互模式
\`\`\`bash
java -jar target/ontology-factory-2.0.0.jar
\`\`\`

### 📖 命令手册

#### 项目管理
\`\`\`bash
# 创建项目
project create <id> <name> <mode> <priority>
# 示例: project create satellite "卫星v1.0" waterfall 10

# 列出所有项目
projects

# 切换项目
switch <projectId>

# 删除项目
project delete <projectId>

# 暂停/恢复项目
project suspend <projectId>
project resume <projectId>
\`\`\`

#### 资源管理
\`\`\`bash
# 查看资源状态
resources status

# 请求资源
resources request <resourceId>

# 释放资源
resources release <resourceId>
\`\`\`

#### 阶段管理
\`\`\`bash
# 阶段转换
phase transition <PHASE>
# 可用阶段: REQUIREMENT, DESIGN, DEVELOPMENT, CODE_REVIEW, TESTING, DEPLOYMENT

# 查看当前阶段状态
phase status
\`\`\`

#### 对象更新
\`\`\`bash
# 更新对象属性
update <objectId> <attribute> <value>
\`\`\`

### 🎯 使用场景

#### 场景1: 航天项目 (瀑布模式)
\`\`\`bash
ontology> project create satellite "卫星控制v1.0" waterfall 10
ontology> switch satellite
ontology> resources request test_platform_1
ontology> phase transition DESIGN
❌ 瀑布模式: 必须按顺序完成各阶段
\`\`\`

#### 场景2: 互联网App (敏捷模式)
\`\`\`bash
ontology> project create app "电商App" agile 7
ontology> switch app
ontology> phase transition TESTING
⚠️ 敏捷模式: 建议按顺序进行,但允许跳转
✅ 敏捷模式: 灵活转换 REQUIREMENT → TESTING
\`\`\`

#### 场景3: 企业ERP (混合模式)
\`\`\`bash
ontology> project create erp "ERP升级" hybrid 5
ontology> switch erp
ontology> phase transition DEVELOPMENT
🔄 混合模式: 瀑布→敏捷转换
\`\`\`

#### 场景4: 多项目并发
\`\`\`bash
ontology> project create sat "卫星" waterfall 10
ontology> project create app "App" agile 7
ontology> project create erp "ERP" hybrid 5
ontology> run all
🚀 启动 3 个活跃项目...
  ✅ 卫星 已启动
  ✅ App 已启动
  ✅ ERP 已启动
\`\`\`

### 🏗️ 架构设计

#### 四层架构
\`\`\`
┌─────────────────────────────────────┐
│  Layer 4: 多项目管理层               │
│  - ProjectManager                   │
│  - ResourceScheduler                │
│  - ProjectRouter                    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Layer 3: 流程模式层                 │
│  - WaterfallEngine (严格)           │
│  - AgileEngine (灵活)               │
│  - HybridEngine (混合)              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Layer 2: 决策网络层                 │
│  - StateMonitor                     │
│  - RuleEngine                       │
│  - ActionExecutor                   │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Layer 1: 本体模型层                 │
│  - OntologyObject                   │
│  - SemanticLifter                   │
└─────────────────────────────────────┘
\`\`\`

### 📊 核心类

| 类名 | 职责 |
|------|------|
| **ProjectContext** | 项目上下文(ID/名称/模式/优先级/状态) |
| **ProjectManager** | 项目管理器(创建/删除/列表/运行) |
| **ResourceScheduler** | 资源调度器(分配/释放/抢占) |
| **WaterfallEngine** | 瀑布模式引擎(严格门禁) |
| **AgileEngine** | 敏捷模式引擎(灵活跳转) |
| **HybridEngine** | 混合模式引擎(分区管理) |
| **SharedResource** | 共享资源(测试台/人员/服务) |

### 🎓 核心概念

#### 阶段 (Phase)
- REQUIREMENT (需求)
- DESIGN (设计)
- DEVELOPMENT (开发)
- CODE_REVIEW (代码审查)
- TESTING (测试)
- DEPLOYMENT (部署)

#### 模式 (WorkflowMode)
- WATERFALL (瀑布): 严格顺序,门禁必须通过
- AGILE (敏捷): 灵活迭代,允许跳转
- HYBRID (混合): 部分严格,部分灵活

#### 项目状态 (ProjectStatus)
- ACTIVE (活跃)
- SUSPENDED (暂停)
- WAITING (等待资源)
- COMPLETED (已完成)

### 🔧 技术栈

- **Java**: 17+
- **构建工具**: Maven 3.6+
- **依赖**:
  - Jackson 2.15.2 (JSON/YAML)
  - SnakeYAML 2.0
  - SLF4J 2.0.7 + Logback 1.4.8

### 📈 版本演进

- **v1.0.0**: 基础动态本体映射 + 决策网络
- **v2.0.0** ⭐: 多项目并发 + 三种流程模式
- **v2.1.0** (计划): 可视化仪表板 + 监控
- **v2.2.0** (计划): AI智能调度
- **v2.3.0** (计划): 分布式 + RESTful API

### 🤝 贡献

欢迎提交Issue和Pull Request!

### 📄 许可证

MIT License

### 📧 联系方式

GitHub: https://github.com/forfire912/vscodeItem

---

**核心价值**: 一套系统,多种模式,同时并发,覆盖全场景 🚀
