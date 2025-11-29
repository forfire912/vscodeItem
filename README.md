# 基于动态本体映射与动力学决策网络的软件工厂实现

## 项目简介

本项目实现了一种创新的软件研发自动化方法,通过统一语义模型将研发全要素纳入可推理结构,再利用状态监听与规则触发机制生成动作,从而形成数据驱动的自动化研发体系,实现"数据即运营"。

## 核心特性

### 1. 动态研发本体
- **对象-链接-动作模型**: 统一抽象需求、代码、测试台等研发要素
- **可推理结构**: 每个对象可被赋予属性、关系及可执行动作
- **图谱化管理**: 通过链接关系形成本体图谱

### 2. 语义提升技术
- **异构数据摄入**: 从Git、需求管理、缺陷跟踪等工具获取原始数据
- **自动语义转换**: 将原始数据转换为本体对象
- **关系自动发现**: 解析数据中的隐含关系(如"Fixes #1024")

### 3. 动力学决策网络
- **状态监听**: 实时监听本体对象的状态变化
- **规则引擎**: 基于"若状态A发生,则执行动作B"的规则
- **自动化编排**: 根据规则自动触发动作链

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                     命令行接口 (CLI)                      │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼──────┐  ┌────────▼─────────┐  ┌──────▼──────┐
│  配置加载器   │  │   语义提升器      │  │  状态监视器  │
└───────┬──────┘  └────────┬─────────┘  └──────┬──────┘
        │                  │                   │
        │         ┌────────▼─────────┐         │
        │         │   本体图谱        │         │
        │         │  OntologyObject  │         │
        │         │      Link        │         │
        │         └────────┬─────────┘         │
        │                  │                   │
        └──────────────────┼───────────────────┘
                           │
                  ┌────────▼─────────┐
                  │ 动力学决策网络    │
                  │   DynamicRule    │
                  │   ActionEngine   │
                  └──────────────────┘
```

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+

### 编译项目

```bash
mvn clean package
```

### 运行系统

```bash
java -jar target/ontology-dynamic-network-1.0.0.jar
```

或使用Maven运行:

```bash
mvn exec:java -Dexec.mainClass="com.softwarefactory.ontology.Main"
```

## 使用指南

### 命令行操作

启动系统后,可使用以下命令:

```bash
# 启动动力学决策网络引擎
ontology> start

# 加载配置文件
ontology> load ontology config/ontology.yml
ontology> load actions config/actions.yml
ontology> load rules config/rules.yml

# 查看系统状态
ontology> status
ontology> objects
ontology> rules

# 更新对象属性(触发规则)
ontology> update req_2048 security_level A

# 摄入外部数据
ontology> ingest git_commit
ontology> ingest requirement
ontology> ingest defect

# 运行演示场景
ontology> demo

# 退出系统
ontology> exit
```

### 配置文件说明

#### 1. 本体配置 (config/ontology.yml)

定义研发要素对象:

```yaml
ontology_objects:
  - id: req_2048
    type: requirement
    name: 新增用户认证功能
    attributes:
      security_level: B
      status: in_progress
    available_actions:
      - 审查需求
      - 分配任务
```

#### 2. 动作配置 (config/actions.yml)

定义可执行的动作:

```yaml
actions:
  - name: 启用强制审查
    description: 对代码分支启用强制审查流程
    target_object_type: code_branch
    parameters:
      review_level: string
```

#### 3. 规则配置 (config/rules.yml)

定义动力学规则:

```yaml
rules:
  - id: rule_001
    name: 高安全等级需求强制审查
    priority: 100
    condition:
      object_type: requirement
      attribute: security_level
      operator: equals
      value: A
    actions:
      - action: 启用强制审查
        target: branch_auth_feature
        params:
          review_level: strict
```

## 典型应用场景

### 场景1: 需求安全等级提升

当需求的安全等级从B级提升到A级时:

1. **状态变更**: `req_2048.security_level: B → A`
2. **规则触发**: 高安全等级需求强制审查规则
3. **动作执行**:
   - 对相关代码分支启用强制审查
   - 预约高精度测试台
   - 发送通知给相关团队

### 场景2: Git提交语义提升

系统解析Git提交信息 "Fixes #1024: 修复安全漏洞":

1. **数据摄入**: 获取Git提交原始数据
2. **语义提升**: 创建代码变更对象
3. **关系发现**: 自动建立与缺陷#1024的"修复"链接
4. **动作触发**: 执行代码走查和静态分析

### 场景3: 测试台自动化编排

当测试台被预约时:

1. **状态变更**: `testbed_sim_01.reserved_for: null → auto_scheduler`
2. **规则触发**: 测试台预约后准备环境规则
3. **动作链执行**:
   - 上电
   - 加载最新固件
   - 运行环境检查

## 核心类说明

### 模型层 (model)
- `OntologyObject`: 本体对象,研发要素的统一抽象
- `Link`: 对象间的关系链接
- `Action`: 可执行的动作定义

### 引擎层 (engine)
- `StateMonitor`: 状态监视器,监听对象状态变化
- `StateChangeEvent`: 状态变更事件
- `DynamicRule`: 动力学规则定义
- `DynamicNetworkEngine`: 动力学决策网络引擎

### 配置层 (config)
- `ConfigLoader`: 配置文件加载器,支持JSON/YAML

### 语义层 (semantic)
- `SemanticLifter`: 语义提升器,将原始数据转换为本体对象
- `DataMapper`: 数据映射器接口

## 扩展开发

### 添加新的本体对象类型

1. 在配置文件中定义对象类型
2. 可选:创建专门的映射器

### 添加新的动作

1. 在`actions.yml`中定义动作
2. 在`ConfigLoader.createDefaultExecutor()`中实现执行逻辑

### 添加新的规则

1. 在`rules.yml`中定义规则
2. 配置触发条件和要执行的动作链

### 添加新的数据源

实现`SemanticLifter.DataMapper`接口:

```java
public class CustomDataMapper implements DataMapper {
    @Override
    public List<OntologyObject> map(Map<String, Object> rawData) {
        // 解析原始数据
        // 创建本体对象
        // 建立关系链接
        return objects;
    }
}

// 注册映射器
semanticLifter.registerMapper("custom_type", new CustomDataMapper());
```

## 项目结构

```
.
├── config/                    # 配置文件目录
│   ├── ontology.yml          # 本体对象配置
│   ├── actions.yml           # 动作配置
│   └── rules.yml             # 规则配置
├── src/main/java/com/softwarefactory/ontology/
│   ├── Main.java             # 主程序入口
│   ├── model/                # 模型层
│   │   ├── OntologyObject.java
│   │   ├── Link.java
│   │   └── Action.java
│   ├── engine/               # 引擎层
│   │   ├── StateMonitor.java
│   │   ├── StateChangeEvent.java
│   │   ├── DynamicRule.java
│   │   └── DynamicNetworkEngine.java
│   ├── config/               # 配置层
│   │   └── ConfigLoader.java
│   └── semantic/             # 语义层
│       └── SemanticLifter.java
└── pom.xml                   # Maven配置
```

## 技术特点

1. **事件驱动架构**: 基于状态变更事件的响应式设计
2. **规则引擎**: 声明式规则配置,易于扩展维护
3. **可配置化**: 核心逻辑通过配置文件驱动
4. **语义化抽象**: 统一的本体模型简化复杂度
5. **图谱化管理**: 通过链接关系形成知识图谱

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request!