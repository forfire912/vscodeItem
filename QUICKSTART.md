# 快速开始指南

## 编译和运行

### 方式1: 使用脚本(推荐)

```bash
./run.sh
```

### 方式2: 使用Maven

```bash
# 编译
mvn clean compile

# 运行
mvn exec:java -Dexec.mainClass="com.softwarefactory.ontology.Main"
```

### 方式3: 使用JAR包

```bash
# 打包
mvn clean package

# 运行
java -jar target/ontology-dynamic-network-1.0.0.jar
```

## 快速演示

启动系统后,按以下步骤体验完整功能:

```bash
# 1. 启动引擎
ontology> start

# 2. 加载配置
ontology> load ontology config/ontology.yml
ontology> load actions config/actions.yml
ontology> load rules config/rules.yml

# 3. 查看当前状态
ontology> status
ontology> objects
ontology> rules

# 4. 触发规则 - 将需求安全等级提升为A级
ontology> update req_2048 security_level A

# 观察系统输出,你会看到:
# - 规则被触发
# - 代码分支启用强制审查
# - 测试台被自动预约
# - 设备自动上电

# 5. 摄入外部数据进行语义提升
ontology> ingest git_commit
ontology> ingest requirement

# 6. 运行完整演示
ontology> demo

# 7. 退出
ontology> exit
```

## 核心概念演示

### 1. 本体对象

系统中的所有研发要素都被建模为本体对象:

- **需求对象** (req_2048): 用户认证功能需求
- **代码分支对象** (branch_auth_feature): 功能开发分支
- **测试台对象** (testbed_sim_01): 嵌入式仿真测试平台
- **缺陷对象** (defect_1024): 安全漏洞缺陷

### 2. 动作定义

每个对象可以执行特定的动作:

- 代码分支: 启用强制审查、代码走查
- 测试台: 上电、加载固件、预约测试台
- 需求: 审查需求、分配任务

### 3. 动力学规则

规则定义了"若状态A发生,则执行动作B"的逻辑:

**规则示例:**
```yaml
- 当需求安全等级变为A级时
  → 启用代码分支的强制审查
  → 预约高精度测试台
  → 发送通知
```

### 4. 语义提升

系统可以从外部工具摄入原始数据并转换为本体对象:

```bash
# Git提交信息: "Fixes #1024: 修复安全漏洞"
ontology> ingest git_commit

# 系统会:
# 1. 创建代码变更对象
# 2. 解析"Fixes #1024"并建立与缺陷的链接
# 3. 自动触发代码审查动作
```

## 自定义配置

### 添加新的本体对象

编辑 `config/ontology.yml`:

```yaml
ontology_objects:
  - id: my_object
    type: custom_type
    name: 我的对象
    attributes:
      custom_attr: value
    available_actions:
      - my_action
```

### 添加新的动作

编辑 `config/actions.yml`:

```yaml
actions:
  - name: my_action
    description: 我的自定义动作
    target_object_type: custom_type
    parameters:
      param1: string
```

### 添加新的规则

编辑 `config/rules.yml`:

```yaml
rules:
  - id: rule_custom
    name: 我的规则
    priority: 100
    condition:
      object_type: custom_type
      attribute: custom_attr
      operator: equals
      value: trigger_value
    actions:
      - action: my_action
        target: my_object
        params: {}
```

## 命令参考

| 命令 | 说明 | 示例 |
|------|------|------|
| `start` | 启动引擎 | `start` |
| `stop` | 停止引擎 | `stop` |
| `load` | 加载配置文件 | `load ontology config/ontology.yml` |
| `ingest` | 摄入外部数据 | `ingest git_commit` |
| `update` | 更新对象属性 | `update req_2048 security_level A` |
| `status` | 显示引擎状态 | `status` |
| `objects` | 显示所有对象 | `objects` |
| `rules` | 显示所有规则 | `rules` |
| `demo` | 运行演示 | `demo` |
| `help` | 显示帮助 | `help` |
| `exit/quit` | 退出系统 | `exit` |

## 常见问题

### Q: 如何调试规则没有触发的问题?

A: 
1. 确认引擎已启动 (`start`)
2. 检查规则配置中的条件是否匹配
3. 使用 `status` 查看系统状态
4. 使用 `objects` 确认对象属性值

### Q: 如何添加新的数据源?

A: 在 `SemanticLifter` 中实现 `DataMapper` 接口,然后注册映射器

### Q: 如何修改动作的执行逻辑?

A: 编辑 `ConfigLoader.java` 中的 `createDefaultExecutor()` 方法

## 进阶使用

### 批量更新

```bash
ontology> update req_2048 status completed
ontology> update testbed_sim_01 power_status on
ontology> update branch_auth_feature review_level strict
```

### 链式触发观察

通过更新一个对象的属性,观察级联触发的规则链:

```bash
ontology> update req_2048 security_level A
# 观察:
# 1. 规则1触发 -> 启用强制审查
# 2. 规则2触发 -> 预约测试台  
# 3. 规则5触发 -> 加载固件
```

## 学习资源

- 查看 `README.md` 了解架构设计
- 阅读配置文件了解配置格式
- 查看源代码了解实现细节
