# 决策网络层设计文档

## 概述

本文档描述如何基于项目的**本体集、活动集、关系集**形成动态决策网络。

---

## 🎯 决策网络的核心机制

### 1. 事件驱动架构

```java
// 事件定义
class OntologyEvent {
    String projectId;
    String objectId;
    String objectType;      // 本体类型 (requirement, bug, etc.)
    String attribute;       // 变化的属性
    String oldValue;
    String newValue;
    long timestamp;
}
```

### 2. 规则引擎 (基于关系库)

每个关系定义自动生成对应的规则:

#### 示例1: depends_on 关系 → 依赖检查规则

**关系定义** (来自 relationship_library.yml):
```yaml
- id: depends_on
  name: 依赖于
  source_types: [requirement, design_doc]
  target_types: [requirement]
  validation_rules:
    - no_circular_dependency
    - target_must_exist
```

**生成的规则**:
```java
class DependsOnRule implements DecisionRule {
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx) {
        // 当源对象状态变化时
        if (event.attribute.equals("status")) {
            // 1. 查找所有依赖此对象的其他对象
            List<Dependency> dependents = ctx.findDependents(event.objectId);
            
            // 2. 如果源对象完成,检查依赖对象是否可以开始
            if (event.newValue.equals("completed")) {
                for (Dependency dep : dependents) {
                    if (allDependenciesMet(dep.targetId, ctx)) {
                        // 3. 触发"可以开始"事件
                        emitEvent(new ReadyToStartEvent(dep.targetId));
                    }
                }
            }
            
            // 4. 如果源对象阻塞,传播阻塞状态
            if (event.newValue.equals("blocked")) {
                for (Dependency dep : dependents) {
                    ctx.updateObject(dep.targetId, "status", "blocked");
                }
            }
        }
    }
}
```

#### 示例2: tests 关系 → 测试覆盖规则

**关系定义**:
```yaml
- id: tests
  name: 测试
  source_types: [test_case]
  target_types: [requirement, code_module]
```

**生成的规则**:
```java
class TestCoverageRule implements DecisionRule {
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx) {
        // 当测试用例状态变化时
        if (event.objectType.equals("test_case") && 
            event.attribute.equals("status")) {
            
            // 1. 查找此测试用例测试的目标对象
            List<String> targets = ctx.findTestTargets(event.objectId);
            
            for (String targetId : targets) {
                // 2. 计算目标对象的测试覆盖率
                int totalTests = ctx.countTestsFor(targetId);
                int passedTests = ctx.countPassedTestsFor(targetId);
                
                // 3. 更新目标对象的测试状态
                if (passedTests == totalTests) {
                    ctx.updateObject(targetId, "test_status", "all_passed");
                    // 4. 如果是代码模块,可能触发部署活动
                    if (ctx.getObjectType(targetId).equals("code_module")) {
                        triggerActivity("prepare_deployment_package", ctx);
                    }
                }
            }
        }
    }
}
```

#### 示例3: fixes 关系 → 缺陷修复规则

**关系定义**:
```yaml
- id: fixes
  name: 修复
  source_types: [commit, code_module]
  target_types: [bug]
```

**生成的规则**:
```java
class BugFixRule implements DecisionRule {
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx) {
        // 当commit创建且声明修复某bug时
        if (event.objectType.equals("commit") && 
            event.attribute.equals("fixes_bug")) {
            
            String bugId = event.newValue;
            
            // 1. 更新bug状态
            ctx.updateObject(bugId, "status", "resolved");
            ctx.updateObject(bugId, "fixed_by", event.objectId);
            
            // 2. 检查是否需要回归测试
            List<String> testCases = ctx.findTestsForBug(bugId);
            for (String testId : testCases) {
                // 3. 触发回归测试活动
                triggerActivity("regression_test", ctx, 
                    Map.of("test_case", testId));
            }
        }
    }
}
```

---

## 🔄 活动执行引擎 (基于活动库)

### 活动调度机制

每个活动定义包含前置条件和后置效果:

**活动定义** (来自 activity_library.yml):
```yaml
- id: approve_requirements
  name: 批准需求
  phase: REQUIREMENT
  inputs: [requirement]
  outputs: [requirement]  # status -> approved
  duration_estimate: 2
```

**活动执行器**:
```java
class ActivityExecutor {
    public void execute(String activityId, ProjectContext ctx, Map<String, String> params) {
        ActivityDef activity = ctx.getActivity(activityId);
        
        // 1. 检查前置条件
        if (!checkPreconditions(activity, params, ctx)) {
            throw new PreconditionFailedException();
        }
        
        // 2. 执行活动逻辑
        switch (activity.getId()) {
            case "approve_requirements":
                String reqId = params.get("requirement_id");
                // 3. 更新本体对象状态
                ctx.updateObject(reqId, "status", "approved");
                ctx.updateObject(reqId, "approved_by", getCurrentUser());
                ctx.updateObject(reqId, "approved_date", getCurrentDate());
                
                // 4. 触发事件 (会被规则引擎捕获)
                emitEvent(new OntologyEvent(
                    ctx.getProjectId(),
                    reqId,
                    "requirement",
                    "status",
                    "draft",
                    "approved"
                ));
                break;
                
            case "fix_bug":
                String bugId = params.get("bug_id");
                ctx.updateObject(bugId, "status", "resolved");
                // 检查是否所有阻塞问题都已解决
                checkBlockedItems(bugId, ctx);
                break;
                
            // ... 其他活动
        }
        
        // 5. 记录活动执行历史
        ctx.recordActivityExecution(activityId, params);
    }
    
    private boolean checkPreconditions(ActivityDef activity, 
                                       Map<String, String> params, 
                                       ProjectContext ctx) {
        // 检查输入本体对象是否存在且状态正确
        for (String inputType : activity.getInputs()) {
            String objectId = params.get(inputType + "_id");
            if (!ctx.objectExists(objectId)) {
                return false;
            }
        }
        return true;
    }
}
```

---

## 🕸️ 完整的决策网络示例

### 场景: 需求批准触发设计活动

1. **初始状态**:
   ```
   requirement_001:
     status: draft
     title: "用户登录功能"
   
   design_doc_001:
     status: not_started
     implements: requirement_001  # 实现关系
   ```

2. **用户操作**:
   ```java
   // 执行批准需求活动
   activityExecutor.execute("approve_requirements", ctx, 
       Map.of("requirement_id", "requirement_001"));
   ```

3. **活动执行**:
   ```
   ✓ approve_requirements 执行
   ✓ requirement_001.status = "approved"
   ✓ 触发事件: RequirementStatusChanged
   ```

4. **规则评估**:
   ```java
   // ImplementsRule 被触发
   class ImplementsRule {
       void evaluate(OntologyEvent event) {
           if (event.objectType == "requirement" && 
               event.newValue == "approved") {
               
               // 查找实现此需求的设计文档
               List<String> designs = ctx.findImplementors(event.objectId);
               
               for (String designId : designs) {
                   // 触发设计活动
                   if (ctx.getCurrentPhase() == DESIGN) {
                       triggerActivity("create_architecture_design", 
                           Map.of("requirement_id", event.objectId,
                                  "design_doc_id", designId));
                   }
               }
           }
       }
   }
   ```

5. **连锁反应**:
   ```
   ✓ create_architecture_design 活动被触发
   ✓ design_doc_001.status = "in_progress"
   ✓ 触发事件: DesignDocStatusChanged
   ✓ ... 继续传播
   ```

---

## 🏗️ 实现架构

### 完整的决策网络类图

```
┌─────────────────────────────────────────┐
│         ProjectContext                  │
│  - ontologies: Map<OntologyDef>        │
│  - activities: Map<ActivityDef>        │
│  - relationships: Map<RelationshipDef> │
│  - objects: Map<String, OntologyObject>│ ← 实际数据
│  - links: List<OntologyLink>           │ ← 关系实例
└────────────┬────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────┐
│      DecisionNetworkEngine              │
│  - rules: List<DecisionRule>            │
│  - eventQueue: Queue<OntologyEvent>     │
│  + processEvent(event)                  │
│  + evaluateRules(event, context)        │
└────────────┬────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────┐
│         RuleFactory                     │
│  + createRulesFromRelationships()       │
│  + createDependsOnRule()                │
│  + createImplementsRule()               │
│  + createTestsRule()                    │
│  + createFixesRule()                    │
└─────────────────────────────────────────┘
```

### 核心接口定义

```java
// 决策规则接口
interface DecisionRule {
    String getRuleId();
    List<String> getTriggerObjectTypes();
    List<String> getTriggerAttributes();
    void evaluate(OntologyEvent event, ProjectContext ctx);
}

// 本体对象 (运行时实例)
class OntologyObject {
    String objectId;
    String objectType;          // 对应 OntologyDef
    Map<String, Object> attributes;
    long createdTime;
    long lastModifiedTime;
}

// 关系链接 (运行时实例)
class OntologyLink {
    String linkId;
    String relationshipType;    // 对应 RelationshipDef
    String sourceObjectId;
    String targetObjectId;
    Map<String, Object> properties;
}

// 活动执行记录
class ActivityExecution {
    String executionId;
    String activityId;
    Map<String, String> parameters;
    long startTime;
    long endTime;
    String status;  // running, completed, failed
}
```

---

## 📝 配置驱动的决策网络生成

### 自动规则生成流程

```java
class DecisionNetworkBuilder {
    
    public DecisionNetworkEngine build(ProjectContext ctx) {
        DecisionNetworkEngine engine = new DecisionNetworkEngine();
        
        // 1. 从关系库生成规则
        for (RelationshipDef rel : ctx.getRelationships().values()) {
            DecisionRule rule = createRuleFromRelationship(rel);
            engine.addRule(rule);
        }
        
        // 2. 从活动库生成活动触发器
        for (ActivityDef act : ctx.getActivities().values()) {
            ActivityTrigger trigger = createTriggerFromActivity(act);
            engine.addTrigger(trigger);
        }
        
        // 3. 从本体库生成状态机
        for (OntologyDef ont : ctx.getOntologies().values()) {
            StateMachine sm = createStateMachineFromOntology(ont);
            engine.addStateMachine(sm);
        }
        
        return engine;
    }
    
    private DecisionRule createRuleFromRelationship(RelationshipDef rel) {
        switch (rel.getId()) {
            case "depends_on":
                return new DependsOnRule(rel);
            case "implements":
                return new ImplementsRule(rel);
            case "tests":
                return new TestsRule(rel);
            case "fixes":
                return new FixesRule(rel);
            case "blocks":
                return new BlocksRule(rel);
            // ... 其他关系类型
            default:
                return new GenericRelationshipRule(rel);
        }
    }
}
```

---

## 🎯 总结

### 决策网络的形成过程

1. **加载配置** → 本体库、活动库、关系库
2. **创建项目** → 选取本体集、活动集、关系集
3. **生成规则** → 每个关系定义 → 一个或多个决策规则
4. **注册触发器** → 每个活动定义 → 前置条件和后置效果
5. **运行时执行**:
   - updateObject() → 触发事件
   - 事件 → 评估所有相关规则
   - 规则 → 可能触发活动
   - 活动 → 更新本体对象
   - 循环往复,形成动态决策网络

### 优势

- ✅ **配置驱动**: 修改YAML即可改变决策逻辑,无需编码
- ✅ **可追溯**: 所有决策都有明确的规则来源
- ✅ **可扩展**: 新增关系类型自动生成对应规则
- ✅ **项目隔离**: 每个项目独立的决策网络实例

### 下一步实现

1. 实现 `DecisionNetworkEngine` 核心引擎
2. 实现 `RuleFactory` 规则工厂
3. 实现具体规则类 (DependsOnRule, ImplementsRule等)
4. 集成到 `ProjectContext` 中
5. 修改 `updateObject()` 方法触发事件
