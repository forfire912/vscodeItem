# 决策网络使用指南

## 快速开始

### 1. 运行系统
```bash
java -jar target/ontology-factory-2.0.0.jar
```

### 2. 创建项目
```bash
# 基于医疗设备模板创建项目
ontology> template create waterfall_medical_device MED001 "医疗设备v1.0" 10

# 输出:
# ✅ 创建项目: 医疗设备v1.0 [WATERFALL]
#   ✓ 初始化本体集: 9 个
#   ✓ 初始化活动集: 22 个
#   ✓ 初始化关系集: 9 个
#   ✓ 决策网络已初始化: 9 个规则   ← 关键!
```

### 3. 创建对象
```bash
# 创建需求对象
ontology> create_object req_001 requirement

# 创建设计文档对象
ontology> create_object design_001 design_doc

# 查看对象列表
ontology> list_objects
```

### 4. 触发决策网络
```bash
# 更新需求状态 → 触发 ImplementsRule
ontology> update req_001 status approved

# 输出:
# 📝 更新对象: req_001.status = approved
#   📊 [ImplementsRule] requirement 已批准,可以开始实现
#       → 建议创建设计文档   ← 智能建议!
```

---

## 决策网络工作原理

### 架构流程图
```
配置文件 (relationship_library.yml)
    ↓
RuleFactory.createRulesFromRelationships()
    ↓ (自动生成规则)
DecisionNetworkEngine
    ↓
updateObject() 触发事件
    ↓
OntologyEvent (projectId, objectId, attribute, oldValue → newValue)
    ↓
规则评估 (ImplementsRule, TestsRule, DependsOnRule, etc.)
    ↓
智能建议输出
```

### 核心组件

#### 1. OntologyEvent (事件)
```java
class OntologyEvent {
    String projectId;      // 所属项目
    String objectId;       // 对象ID
    String objectType;     // 对象类型 (requirement, design_doc, etc.)
    String attribute;      // 属性名 (status, priority, etc.)
    String oldValue;       // 旧值
    String newValue;       // 新值
    long timestamp;        // 时间戳
}
```

#### 2. DecisionRule (规则接口)
```java
interface DecisionRule {
    String getRuleId();                          // 规则ID
    Set<String> getTriggerObjectTypes();         // 触发对象类型
    Set<String> getTriggerAttributes();          // 触发属性
    void evaluate(OntologyEvent event, ProjectContext ctx);  // 评估
}
```

#### 3. DecisionNetworkEngine (引擎)
```java
class DecisionNetworkEngine {
    private List<DecisionRule> rules;            // 规则列表
    private Queue<OntologyEvent> eventQueue;     // 事件队列
    private Set<String> processedEvents;         // 已处理事件(防重复)
    
    void addRule(DecisionRule rule);             // 添加规则
    void emitEvent(OntologyEvent event);         // 触发事件
}
```

---

## 内置规则

### 1. ImplementsRule
**触发条件**: requirement 的 status 属性变化  
**关系**: implements (设计实现需求)  
**行为**: 当需求批准时,建议创建设计文档

**示例**:
```bash
ontology> update req_001 status approved
  📊 [ImplementsRule] requirement 已批准,可以开始实现
      → 建议创建设计文档
```

---

### 2. DependsOnRule
**触发条件**: 任何对象的 status 属性变化  
**关系**: depends_on (依赖关系)  
**行为**: 追踪依赖链,检测阻塞状态

**示例**:
```bash
ontology> update design_001 status blocked
  📊 [DependsOnRule] 检测到 design_doc 状态变化: null → blocked
```

---

### 3. TestsRule
**触发条件**: test_case 的 status 属性变化  
**关系**: tests (测试关系)  
**行为**: 测试通过时更新被测对象状态

**示例**:
```bash
ontology> update test_001 status passed
  📊 [TestsRule] 测试用例状态: passed
      ✓ 测试通过,更新被测对象的测试状态
```

---

### 4. FixesRule
**触发条件**: bug 的 status 属性变化  
**关系**: fixes (修复关系)  
**行为**: 缺陷修复时触发回归测试

**示例**:
```bash
ontology> update bug_001 status fixed
  📊 [FixesRule] 缺陷已修复
      → 建议触发回归测试
```

---

### 5. BlocksRule
**触发条件**: 任何对象的 status 属性变化  
**关系**: blocks (阻塞关系)  
**行为**: 传播阻塞/解除阻塞状态

**示例**:
```bash
ontology> update issue_001 status resolved
  📊 [BlocksRule] 阻塞已解除
      → 更新被阻塞对象状态
```

---

### 6. GenericRelationshipRule
**触发条件**: 匹配关系定义的对象类型和属性  
**关系**: approved_by, reviews, traces_to, found_by 等  
**行为**: 通用关系处理,记录触发事件

**示例**:
```bash
ontology> create_object req_001 requirement
  📊 [批准人Rule] 关系: approved_by 触发,对象: req_001
  📊 [评审Rule] 关系: reviews 触发,对象: req_001
  📊 [追溯到Rule] 关系: traces_to 触发,对象: req_001
```

---

## 完整示例场景

### 场景: 需求到测试的完整流程

```bash
# 1. 创建项目
ontology> template create waterfall_medical_device MED001 "医疗设备v1.0" 10

# 2. 创建需求
ontology> create_object req_func_001 requirement
ontology> update req_func_001 title "用户登录功能"

# 3. 批准需求 → 触发 ImplementsRule
ontology> update req_func_001 status approved
  📊 [ImplementsRule] requirement 已批准,可以开始实现
      → 建议创建设计文档

# 4. 创建设计文档
ontology> create_object design_login_001 design_doc
ontology> update design_login_001 title "登录模块设计"

# 5. 创建代码模块
ontology> create_object code_auth_001 code_module
ontology> update code_auth_001 status completed

# 6. 创建测试用例
ontology> create_object test_login_001 test_case
ontology> update test_login_001 type "单元测试"

# 7. 执行测试 → 触发 TestsRule
ontology> update test_login_001 status passed
  📊 [TestsRule] 测试用例状态: passed
      ✓ 测试通过,更新被测对象的测试状态

# 8. 创建缺陷
ontology> create_object bug_001 bug
ontology> update bug_001 severity critical

# 9. 修复缺陷 → 触发 FixesRule
ontology> update bug_001 status fixed
  📊 [FixesRule] 缺陷已修复
      → 建议触发回归测试

# 10. 查看所有对象
ontology> list_objects
```

---

## 可用命令速查

### 库管理
```bash
library ontologies       # 查看11个本体定义
library activities       # 查看27个活动定义
library relationships    # 查看16个关系定义
```

### 模板管理
```bash
templates                                 # 列出4个模板
template show waterfall_medical_device    # 查看模板详情
template create <templateId> <projectId> <name> <priority>
```

### 对象管理
```bash
create_object <objectId> <ontologyType>   # 创建对象
update <objectId> <attr> <value>          # 更新对象 (触发决策网络!)
list_objects                              # 列出所有对象
```

### 项目管理
```bash
projects                 # 列出所有项目
switch <projectId>       # 切换项目
status                   # 查看当前项目状态
```

---

## 常见问题

### Q1: 为什么规则没有触发?
**A**: 检查以下几点:
1. 对象类型是否匹配规则的 `triggerObjectTypes`
2. 属性名是否匹配规则的 `triggerAttributes`
3. 使用 `list_objects` 确认对象已创建
4. 查看项目是否有对应的关系定义

### Q2: 如何添加自定义规则?
**A**: 两种方式:
1. **配置方式**: 在 `relationship_library.yml` 中添加新关系,GenericRelationshipRule 会自动处理
2. **代码方式**: 
   - 实现 `DecisionRule` 接口
   - 在 `RuleFactory.createRulesFromRelationships()` 中添加映射

### Q3: 规则会无限循环吗?
**A**: 不会,DecisionNetworkEngine 使用 `processedEvents` 集合防止重复处理同一事件。

### Q4: 规则评估顺序?
**A**: 按添加到 DecisionNetworkEngine 的顺序评估,可以通过实现优先级机制调整。

---

## 技术细节

### 事件去重机制
```java
String eventKey = event.getProjectId() + "-" + 
                  event.getObjectId() + "-" + 
                  event.getAttribute() + "-" + 
                  event.getNewValue();
                  
if (processedEvents.contains(eventKey)) {
    return; // 已处理,跳过
}
processedEvents.add(eventKey);
```

### 对象类型推断
```java
// 方法1: 从对象存储获取
String objectType = (String) objects.get(objId + ".type");

// 方法2: 从对象ID推断 (简单场景)
String objectType = objId.split("_")[0]; // req_001 → req
```

### 规则生成逻辑
```java
// RuleFactory.java
for (RelationshipDef rel : relationships.values()) {
    switch (rel.getId()) {
        case "implements":
            rules.add(new ImplementsRule(rel));
            break;
        case "depends_on":
            rules.add(new DependsOnRule(rel));
            break;
        // ... 其他规则
        default:
            rules.add(new GenericRelationshipRule(rel));
    }
}
```

---

## 性能优化建议

### 1. 规则数量优化
- **当前**: 9个规则 (对应9个关系)
- **建议**: 只为高频关系创建专用规则,其余使用 GenericRule

### 2. 事件队列大小
- **当前**: 无限制 LinkedList
- **建议**: 使用有界队列,避免内存溢出

### 3. 缓存机制
- **当前**: 每次评估都遍历所有规则
- **建议**: 建立 objectType → Rules 索引

---

## 扩展指南

### 添加新规则示例

```java
// 1. 定义规则
class AutoAssignRule implements DecisionRule {
    @Override
    public String getRuleId() { return "auto_assign"; }
    
    @Override
    public Set<String> getTriggerObjectTypes() {
        return Set.of("bug");
    }
    
    @Override
    public Set<String> getTriggerAttributes() {
        return Set.of("severity");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx) {
        if ("critical".equals(event.getNewValue())) {
            System.out.println("  📊 [AutoAssignRule] 严重缺陷!");
            System.out.println("      → 自动分配给资深工程师");
        }
    }
}

// 2. 在 RuleFactory 中注册
case "auto_assign":
    rules.add(new AutoAssignRule(rel));
    break;

// 3. 在 relationship_library.yml 中定义
- id: auto_assign
  name: 自动分配
  description: 根据严重程度自动分配任务
  from_object_types: [bug]
  to_object_types: [developer]
  cardinality: many_to_one
```

---

**文档版本**: v1.0  
**系统版本**: v2.1 (决策网络层)  
**最后更新**: 2024年
