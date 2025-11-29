# 决策网络测试场景

## 测试目标
验证决策网络能够根据对象更新自动触发规则评估并建议活动

## 测试场景：医疗设备项目中的需求流转

### 场景描述
在医疗设备项目(waterfall_medical_device)中:
1. 创建一个需求对象 "req_001"
2. 将需求状态更新为 "approved"
3. 验证决策网络是否触发 ImplementsRule
4. 验证是否建议执行设计活动

### 预期行为

#### 1. 项目创建时
```
从模板创建项目 (waterfall_medical_device)
  ✓ 初始化本体集: 9 个
  ✓ 初始化活动集: 22 个
  ✓ 初始化关系集: 9 个
  ✓ 决策网络已初始化: X 个规则
```

#### 2. 更新需求状态时
```
📝 更新对象: req_001.status = approved

🔥 事件触发: 
  项目: P001
  对象: req_001 (requirement)
  属性: status
  变化: null → approved

🔍 规则评估: ImplementsRule
  - 检测到需求已批准
  - 搜索 implements 关系: design_doc implements req_001
  - 建议: 触发设计活动 "design_hardware"
```

### 测试步骤

1. **启动程序**
```bash
java -jar target/ontology-factory-2.0.0.jar
```

2. **创建项目**
```
create_template P001 waterfall_medical_device "医疗设备项目V1" 1
```

3. **查看项目关系**
```
show P001
```
应显示项目包含的关系，包括 "implements"

4. **创建需求对象（手动测试）**
这需要在代码中添加创建对象的功能，当前只有 updateObject

5. **更新需求状态**
```
update P001 req_001 status approved
```

### 当前限制
- ❌ 缺少 `create_object` 命令来创建本体实例
- ❌ 缺少 `show_objects` 命令来查看项目中的对象
- ✅ 已有 `updateObject()` 会触发决策网络
- ✅ 规则会被自动生成和评估

## 下一步改进建议

### 1. 添加对象管理命令
```java
// 创建对象实例
create <projectId> <objectId> <ontologyType>
例: create P001 req_001 requirement

// 列出对象
list_objects <projectId>

// 删除对象  
delete <projectId> <objectId>
```

### 2. 增强决策网络反馈
当前 DecisionNetworkEngine 只打印建议，应该:
- 记录建议到建议列表
- 提供查询建议的接口
- 支持用户确认或拒绝建议

### 3. 活动自动触发
实现 `ActivityExecutor` 来真正执行建议的活动:
```java
class ActivityExecutor {
    public void executeActivity(String projectId, String activityId) {
        // 1. 检查活动前置条件
        // 2. 分配资源
        // 3. 更新活动状态为 IN_PROGRESS
        // 4. 执行活动逻辑
    }
}
```

### 4. 关系链追踪
当一个对象更新时，追踪整个依赖链:
```
需求A更新 → 触发设计B → 设计B完成后触发代码C → 代码C完成后触发测试D
```

### 5. 可视化决策网络
生成 Mermaid 图展示:
- 对象间的关系
- 当前触发的规则
- 建议的活动流

