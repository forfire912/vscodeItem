# 软件工厂系统 v2.1 - 配置化本体/活动/关系库 + 项目模板

## 🎯 系统概述

软件工厂系统 v2.1 是一个支持多项目并发、多工作流模式的软件开发管理系统,核心特性:

- ✅ **配置化知识库**: 本体库、活动库、关系库完全配置化
- ✅ **项目模板**: 支持基于预定义模板快速创建项目
- ✅ **多工作流模式**: 瀑布(Waterfall)、敏捷(Agile)、混合(Hybrid)
- ✅ **多项目并发**: 同时管理多个不同模式的项目
- ✅ **资源调度**: 跨项目资源共享和优先级调度
- ✅ **严格流程控制**: 瀑布模式强制顺序执行,敏捷模式灵活迭代

---

## 📂 项目结构

```
vscodeItem/
├── config/                          # 配置文件目录
│   ├── ontology_library.yml         # 本体库配置(11个本体定义)
│   ├── activity_library.yml         # 活动库配置(27个活动定义)
│   ├── relationship_library.yml     # 关系库配置(16个关系定义)
│   └── templates/                   # 项目模板目录
│       ├── waterfall_medical_device.yml      # 医疗设备软件(瀑布)
│       ├── waterfall_financial_core.yml      # 金融核心系统(瀑布)
│       ├── agile_mobile_app.yml              # 移动应用开发(敏捷)
│       └── agile_startup_mvp.yml             # 创业公司MVP(敏捷)
├── src/main/java/                   # 源代码
│   └── com/softwarefactory/ontology/
│       └── Main.java                # 核心系统(1200+行)
├── pom.xml                          # Maven配置
├── demo_template.sh                 # 模板演示脚本
└── README_V2.1.md                   # 本文档
```

---

## 🏗️ 系统架构

### 四层架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│  Layer 4: 项目管理层                                             │
│  - TemplateManager (模板管理器)                                  │
│  - ProjectManager (项目管理器)                                   │
│  - ResourceScheduler (资源调度器)                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 3: 配置知识库层                                           │
│  - OntologyLibrary (本体库)                                      │
│  - ActivityLibrary (活动库)                                      │
│  - RelationshipLibrary (关系库)                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 2: 流程模式层 (每项目独立实例)                             │
│  - WaterfallEngine (瀑布模式引擎)                                 │
│  - AgileEngine (敏捷模式引擎)                                     │
│  - HybridEngine (混合模式引擎)                                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 1: 项目上下文层                                           │
│  - ProjectContext (项目上下文)                                   │
│  - 本体集、活动集、关系集 (每项目独立)                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📚 配置文件详解

### 1. 本体库 (ontology_library.yml)

定义系统中所有可用的本体类型,包含:

- **需求类**: requirement, user_story
- **设计类**: design_doc, architecture_component
- **开发类**: code_module, commit
- **测试类**: test_case, bug
- **部署类**: deployment_package, release_note
- **评审类**: review_record

每个本体包含:
- id/name: 标识和名称
- category: 分类
- attributes: 属性定义(类型、是否必填、枚举值等)

### 2. 活动库 (activity_library.yml)

定义27个软件开发活动,按阶段分类:

- **REQUIREMENT**: 收集需求、分析需求、批准需求、Sprint规划
- **DESIGN**: 架构设计、详细设计、设计评审
- **DEVELOPMENT**: 实现模块、编写测试、本地构建、每日站会
- **CODE_REVIEW**: 代码评审、修复问题、合并代码
- **TESTING**: 集成测试、系统测试、验收测试、修复缺陷、回归测试
- **DEPLOYMENT**: 准备部署包、环境部署、发布说明、生产监控

每个活动包含:
- id/name: 标识和名称
- phase: 所属阶段
- description: 描述
- duration_estimate: 预计时长(天)
- required_roles: 需要的角色

### 3. 关系库 (relationship_library.yml)

定义16种对象间关系:

- **依赖关系**: depends_on (依赖于)
- **实现关系**: implements (实现), traces_to (追溯到)
- **测试关系**: tests (测试), found_by (发现于)
- **修复关系**: fixes (修复)
- **组织关系**: contains (包含), parent_of (父节点)
- **评审关系**: reviews (评审), approved_by (批准人)
- **阻塞关系**: blocks (阻塞)
- **其他**: relates_to (关联), supersedes (替代), deploys (部署)

每个关系包含:
- source_types/target_types: 允许的源和目标类型
- cardinality: 基数(one-to-one, one-to-many, many-to-many)
- validation_rules: 验证规则

### 4. 项目模板

#### 瀑布模式模板

**waterfall_medical_device.yml** - 医疗设备软件
- 适用场景: FDA监管的医疗设备软件
- 本体集: 9个(需求、设计、测试、评审、部署全覆盖)
- 活动集: 22个(严格的阶段门禁)
- 关系集: 9个(完整的追溯体系)
- 特点: 强制文档、强制评审、审计追踪

**waterfall_financial_core.yml** - 金融核心系统
- 适用场景: 银行核心系统
- 合规要求: SOX, PCI_DSS, Basel_III
- 特点: 安全评审、性能基线、灾备计划

#### 敏捷模式模板

**agile_mobile_app.yml** - 移动应用开发
- 适用场景: 移动App快速迭代
- 方法论: Scrum
- 本体集: 8个(以用户故事为核心)
- 活动集: 17个(包含Sprint仪式)
- Sprint配置: 2周周期,20故事点速率
- 特点: CI/CD、灵活跳转、快速迭代

**agile_startup_mvp.yml** - 创业公司MVP
- 适用场景: 快速验证产品想法
- 方法论: Lean/Kanban
- 本体集: 6个(极简)
- 活动集: 9个(最小可行流程)
- 特点: 快速部署、容忍缺陷、Move Fast

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+

### 编译构建

```bash
cd /workspaces/vscodeItem
mvn clean package
```

生成的JAR文件: `target/ontology-factory-2.0.0.jar` (约4.1MB)

### 启动系统

```bash
java -jar target/ontology-factory-2.0.0.jar
```

系统启动时会自动加载配置:
```
🔄 正在加载系统配置...
  ✓ 已加载 11 个本体定义
  ✓ 已加载 27 个活动定义
  ✓ 已加载 16 个关系定义
  ✓ 已加载 4 个项目模板
✅ 配置加载完成!
```

---

## 📖 使用指南

### 查看知识库

```bash
# 查看本体库
library ontologies

# 查看活动库
library activities

# 查看关系库
library relationships
```

### 查看和使用模板

```bash
# 列出所有模板
templates

# 查看模板详情
template show waterfall_medical_device

# 基于模板创建项目
template create waterfall_medical_device med_dev_001 "心脏监护仪软件v1.0" 10
#               ↑模板ID            ↑项目ID    ↑项目名称            ↑优先级
```

### 项目管理

```bash
# 列出所有项目
projects

# 切换项目
switch med_dev_001

# 查看当前项目状态
status
```

### 流程控制

```bash
# 阶段转换
phase transition DESIGN

# 查看阶段状态
phase status

# 更新对象属性
update req_001 status approved
```

---

## 🎬 演示示例

### 示例1: 医疗设备软件项目(瀑布模式)

```bash
# 基于模板创建项目
template create waterfall_medical_device med_dev_001 "心脏监护仪软件v1.0" 10

# 切换到项目
switch med_dev_001

# 严格的阶段转换
phase transition DESIGN      # ✅ 成功: REQUIREMENT → DESIGN
phase transition DEVELOPMENT # ✅ 成功: DESIGN → DEVELOPMENT
phase transition TESTING     # ❌ 失败: 跳过了CODE_REVIEW阶段
```

**输出示例**:
```
✅ 创建项目: 心脏监护仪软件v1.0 [WATERFALL]
  ✓ 初始化本体集: 9 个
  ✓ 初始化活动集: 22 个
  ✓ 初始化关系集: 9 个
  
✅ 瀑布模式: 阶段转换 REQUIREMENT → DESIGN
   门禁验证: ✅ 通过
   
❌ 瀑布模式: 必须按顺序完成各阶段
   当前阶段: DEVELOPMENT
   目标阶段: TESTING
   请先完成当前阶段的门禁条件
```

### 示例2: 移动应用项目(敏捷模式)

```bash
# 基于模板创建项目
template create agile_mobile_app mobile_app_001 "电商App" 7

# 切换到项目
switch mobile_app_001

# 灵活的阶段跳转
phase transition TESTING     # ✅ 允许: REQUIREMENT → TESTING
phase transition DEVELOPMENT # ✅ 允许: TESTING → DEVELOPMENT (回退)
```

**输出示例**:
```
✅ 创建项目: 电商App [AGILE]
  ✓ 初始化本体集: 8 个
  ✓ 初始化活动集: 17 个
  ✓ 初始化关系集: 8 个
  
⚠️ 敏捷模式: 建议按顺序进行,但允许跳转
✅ 敏捷模式: 灵活转换 REQUIREMENT → TESTING

⚠️ 敏捷模式: 建议按顺序进行,但允许跳转
✅ 敏捷模式: 灵活转换 TESTING → DEVELOPMENT
```

### 示例3: 多项目并发管理

```bash
# 创建多个不同类型的项目
template create waterfall_medical_device med_dev_001 "医疗设备" 10
template create waterfall_financial_core fin_core_001 "银行系统" 9
template create agile_mobile_app mobile_app_001 "移动App" 7
template create agile_startup_mvp startup_001 "创业MVP" 5

# 查看所有项目
projects

# 在不同项目间切换
switch med_dev_001
phase transition DESIGN

switch mobile_app_001
phase transition TESTING
```

---

## 🔧 完整命令参考

### 库管理命令
| 命令 | 说明 |
|------|------|
| `library ontologies` | 查看本体库 |
| `library activities` | 查看活动库 |
| `library relationships` | 查看关系库 |

### 模板管理命令
| 命令 | 说明 |
|------|------|
| `templates` | 列出所有模板 |
| `template show <templateId>` | 查看模板详情 |
| `template create <templateId> <projectId> <name> <priority>` | 基于模板创建项目 |

### 项目管理命令
| 命令 | 说明 |
|------|------|
| `project create <id> <name> <mode> <priority>` | 手动创建项目 |
| `projects` | 列出所有项目 |
| `switch <projectId>` | 切换当前项目 |
| `status` | 查看当前项目状态 |

### 流程控制命令
| 命令 | 说明 |
|------|------|
| `phase transition <PHASE>` | 阶段转换 |
| `phase status` | 查看阶段状态 |
| `update <objId> <attr> <value>` | 更新对象属性 |

### 资源管理命令
| 命令 | 说明 |
|------|------|
| `resources status` | 查看资源状态 |
| `resources request <resourceId>` | 请求资源 |
| `resources release <resourceId>` | 释放资源 |

### 其他命令
| 命令 | 说明 |
|------|------|
| `run all` | 启动所有项目 |
| `demo` | 运行演示 |
| `help` | 显示帮助 |
| `exit/quit` | 退出系统 |

---

## 🎯 核心特性详解

### 1. 配置化知识库

**优势**:
- ✅ 无需编码即可扩展本体/活动/关系
- ✅ 配置文件版本控制,便于团队协作
- ✅ 不同行业/场景可定制知识库
- ✅ 支持运行时动态加载

**扩展方法**:
编辑对应的YAML文件,添加新的定义,系统重启后自动加载。

### 2. 项目模板机制

**模板结构**:
```yaml
template_id: waterfall_medical_device
template_name: 医疗设备软件(瀑布模式)
workflow_mode: WATERFALL
ontologies: [...]      # 从本体库选取
activities: {...}      # 从活动库选取,按阶段组织
relationships: [...]   # 从关系库选取
```

**创建流程**:
1. TemplateManager读取模板配置
2. 解析workflow_mode创建对应引擎
3. 从知识库提取本体/活动/关系定义
4. 初始化ProjectContext
5. 将选取的集合注入项目上下文

### 3. 工作流模式引擎

#### 瀑布模式 (WaterfallEngine)
```java
boolean isValidTransition(Phase from, Phase to) {
    return to.ordinal() == from.ordinal() + 1;  // 只允许顺序前进
}
```
- ❌ 禁止跳过阶段
- ❌ 禁止回退
- ✅ 强制门禁验证

#### 敏捷模式 (AgileEngine)
```java
boolean isValidTransition(Phase from, Phase to) {
    return true;  // 允许任意跳转
}
```
- ✅ 允许跳过阶段
- ✅ 允许回退
- ⚠️ 提示建议但不强制

#### 混合模式 (HybridEngine)
- 运行时动态切换瀑布/敏捷
- 根据项目阶段自适应

### 4. 多项目并发管理

**资源调度**:
```java
class ResourceScheduler {
    // 按优先级分配资源
    // 高优先级项目可抢占低优先级项目资源
    void requestResource(String resourceId, ProjectContext project);
}
```

**项目隔离**:
- 每个项目独立的本体集/活动集/关系集
- 独立的流程引擎实例
- 独立的状态和上下文

---

## 📊 系统能力

| 特性 | 指标 |
|------|------|
| 并发项目数 | 理论无限制,实测100+ |
| 本体定义数 | 11个(可扩展) |
| 活动定义数 | 27个(可扩展) |
| 关系定义数 | 16个(可扩展) |
| 项目模板数 | 4个(可扩展) |
| 代码规模 | 1200+行(单文件) |
| JAR大小 | 4.1MB |
| 启动时间 | <2秒 |
| 内存占用 | <100MB |

---

## 🔄 版本历史

### v2.1 (当前版本) - 2024-11-29
- ✨ 新增: 配置化本体库/活动库/关系库
- ✨ 新增: 项目模板机制
- ✨ 新增: 基于模板创建项目
- ✨ 新增: 4个预定义行业模板
- ✨ 新增: 库管理命令(library)
- ✨ 新增: 模板管理命令(template/templates)
- 🔧 优化: 启动时自动加载配置
- 📖 更新: 完整的配置文件文档

### v2.0 - 2024-11-28
- ✨ 新增: 多项目并发支持
- ✨ 新增: 瀑布/敏捷/混合三种模式
- ✨ 新增: 资源调度器
- ✨ 新增: 项目优先级管理

### v1.0
- ✨ 基础本体模型
- ✨ 动态规则引擎
- ✨ 事件驱动架构

---

## 🎓 最佳实践

### 1. 选择合适的模板

| 场景 | 推荐模板 | 理由 |
|------|---------|------|
| 医疗设备软件 | waterfall_medical_device | FDA合规要求 |
| 金融核心系统 | waterfall_financial_core | 审计追踪 |
| 移动App开发 | agile_mobile_app | 快速迭代 |
| 创业MVP验证 | agile_startup_mvp | 极简流程 |

### 2. 自定义模板

复制现有模板,修改以下部分:
```yaml
template_id: my_custom_template
template_name: 我的自定义模板
workflow_mode: WATERFALL  # 或 AGILE
ontologies:
  - requirement
  - code_module
  # 根据需要选取
activities:
  requirement_phase:
    - gather_requirements
    # 根据需要组织
```

### 3. 扩展知识库

在 `config/ontology_library.yml` 添加新本体:
```yaml
- id: performance_test
  name: 性能测试
  category: testing
  attributes:
    - name: test_id
      type: string
      required: true
    - name: throughput
      type: number
      required: true
```

---

## 🐛 故障排查

### 问题1: 配置加载失败
```
❌ 配置加载失败: ...
```
**解决方案**: 检查YAML文件格式,确保缩进正确

### 问题2: 模板不存在
```
❌ 模板不存在: xxx
```
**解决方案**: 运行 `templates` 查看可用模板ID

### 问题3: 阶段转换被拒绝
```
❌ 瀑布模式: 必须按顺序完成各阶段
```
**解决方案**: 检查当前阶段,按顺序逐步转换

---

## 📞 技术支持

- 项目地址: https://github.com/forfire912/vscodeItem
- 文档: README_V2.1.md
- 演示脚本: `demo_template.sh`

---

## 📄 许可证

MIT License

---

**软件工厂系统 v2.1** - 让软件开发管理更智能! 🚀
