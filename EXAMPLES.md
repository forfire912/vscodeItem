# 完整使用示例

## 示例1: 基本流程

```bash
# 1. 启动系统
$ java -jar target/ontology-dynamic-network-1.0.0.jar

╔════════════════════════════════════════════════════════════╗
║   基于动态本体映射与动力学决策网络的软件工厂实现系统      ║
║   Ontology Dynamic Network System v1.0                    ║
╚════════════════════════════════════════════════════════════╝

系统初始化完成
注意: 未找到配置文件,可使用命令手动加载

可用命令:
  start                           - 启动动力学决策网络引擎
  stop                            - 停止引擎
  load <type> <file>              - 加载配置文件
    类型: ontology, actions, rules
  ingest <type>                   - 摄入外部数据并语义提升
    类型: git_commit, requirement, defect
  update <obj_id> <attr> <value>  - 更新对象属性(触发规则)
  status                          - 显示引擎状态
  objects                         - 显示所有本体对象
  rules                           - 显示所有规则
  demo                            - 运行演示场景
  help                            - 显示此帮助信息
  exit/quit                       - 退出系统

# 2. 启动引擎
ontology> start
========================================
动力学决策网络引擎已启动
========================================

# 3. 加载本体配置
ontology> load ontology config/ontology.yml
加载了 5 个本体对象
注册对象: req_2048 [requirement]
注册对象: branch_auth_feature [code_branch]
注册对象: testbed_sim_01 [test_platform]
注册对象: defect_1024 [defect]
注册对象: test_plan_001 [test_plan]

# 4. 加载动作配置
ontology> load actions config/actions.yml
加载了 12 个动作定义
注册动作: 启用强制审查 [code_branch]
注册动作: 代码走查 [code_change]
注册动作: 静态分析 [code_change]
注册动作: 预约测试台 [test_platform]
注册动作: 上电 [test_platform]
注册动作: 加载固件 [test_platform]
注册动作: 运行测试 [test_platform]
注册动作: 发送通知 [*]
注册动作: 审查需求 [requirement]
注册动作: 分配任务 [requirement]

# 5. 加载规则配置
ontology> load rules config/rules.yml
加载了 5 个规则
注册规则: 高安全等级需求强制审查 [优先级: 100]
注册规则: 高安全等级需求预约精密测试台 [优先级: 90]
注册规则: 强制审查触发静态分析 [优先级: 80]
注册规则: 高严重程度缺陷快速响应 [优先级: 95]
注册规则: 测试台预约后准备环境 [优先级: 70]

# 6. 查看系统状态
ontology> status

========================================
引擎状态
========================================
运行状态: 运行中
已注册对象: 5
已注册规则: 5
已注册动作: 12
========================================

# 7. 查看所有对象
ontology> objects

========================================
本体对象列表
========================================
req_2048 [requirement] - 新增用户认证功能
  属性: {requirement_id=2048, security_level=B, status=in_progress, priority=high, owner=security_team}
branch_auth_feature [code_branch] - feature/user-authentication
  属性: {branch_name=feature/user-authentication, related_requirement=req_2048, review_required=false, review_level=normal}
testbed_sim_01 [test_platform] - 嵌入式半实物仿真台01
  属性: {platform_type=embedded_simulation, capacity=high_precision, status=available, power_status=off, reserved_for=null}
defect_1024 [defect] - 登录页面XSS漏洞
  属性: {defect_id=1024, severity=high, status=open, assigned_to=dev_team_a}
test_plan_001 [test_plan] - 认证模块安全测试计划
  属性: {plan_id=001, test_type=security, status=pending, related_requirement=req_2048}
========================================

# 8. 触发规则 - 将需求安全等级提升为A级
ontology> update req_2048 security_level A
状态变更: req_2048.security_level 从 [B] 变为 [A]

>>> 评估规则触发条件 <<<
触发规则: 高安全等级需求强制审查
  执行动作: 启用强制审查 -> branch_auth_feature
    [动作执行] 启用强制审查 在对象 branch_auth_feature
    参数: {review_level=strict}
    -> 已启用强制审查模式
  执行动作: 发送通知 -> req_2048
    [动作执行] 发送通知 在对象 req_2048
    参数: {message=需求已升级为A级安全等级,已启用强制审查流程}
    -> 通知: 需求已升级为A级安全等级,已启用强制审查流程
触发规则: 高安全等级需求预约精密测试台
  执行动作: 预约测试台 -> testbed_sim_01
    [动作执行] 预约测试台 在对象 testbed_sim_01
    参数: {platform=embedded_simulation, reserved_by=auto_scheduler}
    -> 已预约测试台: embedded_simulation
  执行动作: 上电 -> testbed_sim_01
    [动作执行] 上电 在对象 testbed_sim_01
    参数: {}
    -> 设备已上电

# 9. 摄入Git提交数据
ontology> ingest git_commit

=== 语义提升: git_commit ===
  解析Git提交: a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6
    -> 发现缺陷修复关联: #1024
提升了 1 个本体对象
注册对象: change_a1b2c3d4 [code_change]

# 10. 运行完整演示
ontology> demo

========================================
运行演示场景
========================================

场景: 需求安全等级提升触发自动化流程

1. 更新需求安全等级: req_2048
状态变更: req_2048.security_level 从 [A] 变为 [A]

>>> 评估规则触发条件 <<<

2. 演示完成,查看上述输出了解规则触发情况

# 11. 退出
ontology> exit
再见!
```

## 示例2: 语义提升场景

```bash
ontology> start
ontology> load ontology config/ontology.yml
ontology> load actions config/actions.yml
ontology> load rules config/rules.yml

# 摄入需求数据
ontology> ingest requirement

=== 语义提升: requirement ===
  解析需求: 2048 [安全等级: A]
提升了 1 个本体对象
注册对象: req_2048 [requirement]

# 摄入缺陷数据
ontology> ingest defect

=== 语义提升: defect ===
  解析缺陷: 1024 [严重程度: high]
提升了 1 个本体对象
注册对象: defect_1024 [defect]

>>> 评估规则触发条件 <<<
触发规则: 高严重程度缺陷快速响应
  执行动作: 发送通知 -> defect_1024
    [动作执行] 发送通知 在对象 defect_1024
    参数: {message=发现高严重程度缺陷,请立即处理}
    -> 通知: 发现高严重程度缺陷,请立即处理

# 摄入Git提交
ontology> ingest git_commit

=== 语义提升: git_commit ===
  解析Git提交: b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7
    -> 发现缺陷修复关联: #1024
提升了 1 个本体对象
注册对象: change_b2c3d4e5 [code_change]
```

## 示例3: 规则链触发

```bash
ontology> start
ontology> load ontology config/ontology.yml
ontology> load actions config/actions.yml
ontology> load rules config/rules.yml

# 步骤1: 更新需求等级
ontology> update req_2048 security_level A
# 触发规则: rule_001, rule_002
# 动作: 启用强制审查, 预约测试台, 上电

# 步骤2: 观察级联效果
ontology> objects
# 可以看到:
# - branch_auth_feature.review_required = true
# - testbed_sim_01.reserved_for = auto_scheduler
# - testbed_sim_01.power_status = on

# 步骤3: 手动触发另一个规则
ontology> update testbed_sim_01 reserved_for test_scheduler
# 触发规则: rule_005
# 动作: 加载固件
```

## 示例4: 查看详细信息

```bash
ontology> start
ontology> load ontology config/ontology.yml
ontology> load actions config/actions.yml
ontology> load rules config/rules.yml

# 查看所有规则
ontology> rules

========================================
规则列表
========================================
rule_001 - 高安全等级需求强制审查
  描述: 当需求的安全等级提升为A级时,自动启用相关代码分支的强制审查
  优先级: 100
  状态: 启用
rule_002 - 高安全等级需求预约精密测试台
  描述: 当需求为A级安全等级时,自动预约嵌入式半实物仿真台
  优先级: 90
  状态: 启用
rule_003 - 强制审查触发静态分析
  描述: 当代码分支启用强制审查后,自动触发静态代码分析
  优先级: 80
  状态: 启用
rule_004 - 高严重程度缺陷快速响应
  描述: 当缺陷严重程度为高时,发送紧急通知
  优先级: 95
  状态: 启用
rule_005 - 测试台预约后准备环境
  描述: 测试台被预约后,自动加载最新固件
  优先级: 70
  状态: 启用
========================================

# 查看引擎状态
ontology> status

========================================
引擎状态
========================================
运行状态: 运行中
已注册对象: 5
已注册规则: 5
已注册动作: 12
========================================
```

## 核心命令总结

| 命令 | 功能 | 示例 |
|------|------|------|
| `start` | 启动引擎 | `start` |
| `load ontology <file>` | 加载本体配置 | `load ontology config/ontology.yml` |
| `load actions <file>` | 加载动作配置 | `load actions config/actions.yml` |
| `load rules <file>` | 加载规则配置 | `load rules config/rules.yml` |
| `update <id> <attr> <val>` | 更新属性 | `update req_2048 security_level A` |
| `ingest <type>` | 摄入数据 | `ingest git_commit` |
| `status` | 查看状态 | `status` |
| `objects` | 查看对象 | `objects` |
| `rules` | 查看规则 | `rules` |
| `demo` | 运行演示 | `demo` |
| `exit` | 退出 | `exit` |
