# 系统升级规划 v2.0

## 目标: 支持敏捷 + 瀑布混合模式

当前系统(v1.0)天然支持敏捷模式,现需要增强以支持瀑布及其他严格流程模型。

---

## 📋 核心设计思路

### 1. 引入流程模式抽象层 + 多项目并发管理

```
当前架构 (单项目):
  事件 → 规则评估 → 动作执行

升级架构 (多项目并发):
                    ┌─ [项目A-瀑布] ─┐
  事件 → 项目路由器 ─┼─ [项目B-敏捷] ─┼→ 规则评估 → 动作执行
                    └─ [项目C-混合] ─┘
                           ↓
                  流程模式网关 (每项目独立)
```

**关键特性**:
- ✅ 同时运行多个项目(航天瀑布 + 互联网敏捷 + 企业混合)
- ✅ 项目间资源隔离(状态/规则/动作独立)
- ✅ 项目间资源共享(测试台/人员可跨项目分配)
- ✅ 项目优先级调度(高优先级项目优先获取资源)

### 2. 四层架构设计 (支持多项目并发)

```
┌─────────────────────────────────────────────────────────────────┐
│  Layer 4: 多项目管理层 (新增)                                    │
│  - 项目注册表 (ProjectRegistry)                                 │
│  - 项目路由器 (ProjectRouter)                                   │
│  - 资源调度器 (ResourceScheduler)                               │
│  - 项目隔离器 (ProjectIsolation)                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 3: 流程模式层 (新增, 每项目独立实例)                       │
│  - 敏捷模式引擎 (AgileEngine)                                    │
│  - 瀑布模式引擎 (WaterfallEngine)                                │
│  - 混合模式编排器 (HybridEngine)                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 2: 动力学决策网络层 (现有,增强, 每项目独立上下文)          │
│  - 状态监听 (StateMonitor per project)                          │
│  - 规则引擎 (DynamicNetworkEngine per project)                  │
│  - 动作执行 (Action Executor with resource lock)                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Layer 1: 本体模型层 (现有,扩展)                                  │
│  - 对象-链接-动作 (OntologyObject/Link/Action)                  │
│  - 语义提升 (SemanticLifter)                                    │
│  - 共享资源池 (SharedResourcePool)                              │
└─────────────────────────────────────────────────────────────────┘
```

### 3. 多项目并发架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    多项目管理器                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ 项目A(瀑布) │  │ 项目B(敏捷) │  │ 项目C(混合) │          │
│  │ 优先级: 高  │  │ 优先级: 中  │  │ 优先级: 低  │          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │
│         │                │                │                  │
│         └────────────────┼────────────────┘                  │
│                          ↓                                   │
│                  ┌───────────────┐                           │
│                  │  资源调度器   │                           │
│                  └───────┬───────┘                           │
│                          ↓                                   │
│         ┌────────────────┼────────────────┐                  │
│         ↓                ↓                ↓                  │
│   ┌─────────┐      ┌─────────┐      ┌─────────┐            │
│   │ 测试台1  │      │ 测试台2  │      │ 人员池   │            │
│   │ (空闲)  │      │ (项目A) │      │ (共享)  │            │
│   └─────────┘      └─────────┘      └─────────┘            │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 具体升级方案

### Phase 0: 多项目管理基础设施 (新增核心层)

#### 0.1 项目上下文对象

```java
// ProjectContext.java
public class ProjectContext {
    private String projectId;           // 唯一项目ID
    private String projectName;         // 项目名称
    private WorkflowMode mode;          // AGILE, WATERFALL, HYBRID
    private int priority;               // 优先级 (1-10)
    private ProjectStatus status;       // ACTIVE, SUSPENDED, COMPLETED
    
    // 项目独立的引擎实例
    private DynamicNetworkEngine engine;
    private StateMonitor stateMonitor;
    private ProcessModeEngine processModeEngine;
    
    // 项目独立的配置
    private Map<String, OntologyObject> projectObjects;
    private Map<String, DynamicRule> projectRules;
    private Map<String, Action> projectActions;
    
    // 资源占用
    private Set<String> allocatedResourceIds;  // 测试台、人员等
    
    // 项目元数据
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private Map<String, String> metadata;
}

// ProjectStatus.java
public enum ProjectStatus {
    ACTIVE,      // 活跃运行中
    SUSPENDED,   // 暂停(资源不足等)
    WAITING,     // 等待资源
    COMPLETED,   // 已完成
    ARCHIVED     // 已归档
}
```

#### 0.2 多项目管理器

```java
// ProjectManager.java
public class ProjectManager {
    private Map<String, ProjectContext> projects;
    private ResourceScheduler resourceScheduler;
    private ProjectRouter projectRouter;
    
    /**
     * 创建新项目
     */
    public ProjectContext createProject(String projectId, 
                                       String projectName,
                                       WorkflowMode mode,
                                       int priority) {
        ProjectContext context = new ProjectContext(projectId, projectName, mode, priority);
        
        // 为项目创建独立的引擎实例
        context.setEngine(new DynamicNetworkEngine());
        context.setStateMonitor(new StateMonitor());
        
        // 根据模式创建流程引擎
        ProcessModeEngine modeEngine = createModeEngine(mode);
        context.setProcessModeEngine(modeEngine);
        
        // 加载项目配置
        loadProjectConfig(context);
        
        projects.put(projectId, context);
        System.out.println("✅ 创建项目: " + projectName + " [" + mode + "]");
        
        return context;
    }
    
    /**
     * 切换活跃项目
     */
    public void switchProject(String projectId) {
        if (!projects.containsKey(projectId)) {
            throw new IllegalArgumentException("项目不存在: " + projectId);
        }
        
        ProjectContext context = projects.get(projectId);
        System.out.println("🔄 切换到项目: " + context.getProjectName());
    }
    
    /**
     * 列出所有项目
     */
    public void listProjects() {
        System.out.println("\n📋 项目列表:");
        System.out.println("┌────────┬──────────────────┬──────────┬────────┬────────┐");
        System.out.println("│ ID     │ 项目名            │ 模式     │ 优先级 │ 状态   │");
        System.out.println("├────────┼──────────────────┼──────────┼────────┼────────┤");
        
        for (ProjectContext ctx : projects.values()) {
            System.out.printf("│ %-6s │ %-16s │ %-8s │ %-6d │ %-6s │%n",
                ctx.getProjectId(),
                ctx.getProjectName(),
                ctx.getMode(),
                ctx.getPriority(),
                ctx.getStatus()
            );
        }
        
        System.out.println("└────────┴──────────────────┴──────────┴────────┴────────┘");
    }
    
    /**
     * 项目并发执行
     */
    public void runAllProjects() {
        // 按优先级排序
        List<ProjectContext> sortedProjects = projects.values().stream()
            .filter(ctx -> ctx.getStatus() == ProjectStatus.ACTIVE)
            .sorted(Comparator.comparingInt(ProjectContext::getPriority).reversed())
            .collect(Collectors.toList());
        
        System.out.println("🚀 启动 " + sortedProjects.size() + " 个项目...");
        
        // 并发启动所有活跃项目的引擎
        for (ProjectContext ctx : sortedProjects) {
            ctx.getEngine().start();
            System.out.println("  ✅ " + ctx.getProjectName() + " 已启动");
        }
    }
}
```

#### 0.3 资源调度器 (支持资源共享)

```java
// ResourceScheduler.java
public class ResourceScheduler {
    private Map<String, SharedResource> resourcePool;
    private Map<String, String> resourceAllocation;  // resourceId -> projectId
    
    /**
     * 请求资源(带优先级)
     */
    public boolean requestResource(String resourceId, 
                                   ProjectContext project,
                                   boolean blocking) {
        SharedResource resource = resourcePool.get(resourceId);
        
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在: " + resourceId);
        }
        
        // 检查资源是否可用
        if (!resource.isAvailable()) {
            String currentOwner = resourceAllocation.get(resourceId);
            ProjectContext ownerProject = getProject(currentOwner);
            
            // 比较优先级
            if (project.getPriority() > ownerProject.getPriority()) {
                System.out.println("⚠️ 高优先级项目 " + project.getProjectName() + 
                                 " 抢占资源: " + resourceId);
                // 挂起低优先级项目
                ownerProject.setStatus(ProjectStatus.SUSPENDED);
                releaseResource(resourceId);
            } else if (blocking) {
                System.out.println("⏳ 项目 " + project.getProjectName() + 
                                 " 等待资源: " + resourceId);
                project.setStatus(ProjectStatus.WAITING);
                return false;
            } else {
                return false;
            }
        }
        
        // 分配资源
        resource.allocate(project.getProjectId());
        resourceAllocation.put(resourceId, project.getProjectId());
        project.getAllocatedResourceIds().add(resourceId);
        
        System.out.println("✅ 分配资源 " + resourceId + " → " + project.getProjectName());
        return true;
    }
    
    /**
     * 释放资源
     */
    public void releaseResource(String resourceId) {
        SharedResource resource = resourcePool.get(resourceId);
        if (resource != null) {
            String projectId = resourceAllocation.remove(resourceId);
            resource.release();
            
            if (projectId != null) {
                ProjectContext project = getProject(projectId);
                project.getAllocatedResourceIds().remove(resourceId);
                System.out.println("🔓 释放资源 " + resourceId + " ← " + project.getProjectName());
            }
            
            // 唤醒等待此资源的项目
            wakeUpWaitingProjects(resourceId);
        }
    }
    
    /**
     * 查看资源使用情况
     */
    public void showResourceStatus() {
        System.out.println("\n📊 资源使用情况:");
        System.out.println("┌──────────────┬────────┬──────────────────┐");
        System.out.println("│ 资源ID        │ 状态   │ 占用项目          │");
        System.out.println("├──────────────┼────────┼──────────────────┤");
        
        for (SharedResource resource : resourcePool.values()) {
            String owner = resourceAllocation.get(resource.getId());
            String ownerName = owner != null ? getProject(owner).getProjectName() : "-";
            String status = resource.isAvailable() ? "空闲" : "占用";
            
            System.out.printf("│ %-12s │ %-6s │ %-16s │%n",
                resource.getId(),
                status,
                ownerName
            );
        }
        
        System.out.println("└──────────────┴────────┴──────────────────┘");
    }
}

// SharedResource.java
public class SharedResource {
    private String id;
    private String type;          // TEST_PLATFORM, DEVELOPER, TESTER, etc.
    private boolean available;
    private String currentOwner;  // projectId
    private Map<String, String> attributes;
    
    public void allocate(String projectId) {
        this.available = false;
        this.currentOwner = projectId;
    }
    
    public void release() {
        this.available = true;
        this.currentOwner = null;
    }
}
```

#### 0.4 项目路由器 (事件路由到正确项目)

```java
// ProjectRouter.java
public class ProjectRouter {
    private ProjectManager projectManager;
    
    /**
     * 路由事件到对应项目
     */
    public void routeEvent(String projectId, StateChangeEvent event) {
        ProjectContext context = projectManager.getProject(projectId);
        
        if (context == null) {
            System.out.println("❌ 项目不存在: " + projectId);
            return;
        }
        
        if (context.getStatus() != ProjectStatus.ACTIVE) {
            System.out.println("⚠️ 项目未激活: " + context.getProjectName());
            return;
        }
        
        // 路由到项目的引擎
        context.getEngine().evaluateRules(event);
    }
    
    /**
     * 批量路由(同一事件影响多个项目)
     */
    public void broadcastEvent(StateChangeEvent event, Set<String> projectIds) {
        System.out.println("📢 广播事件到 " + projectIds.size() + " 个项目");
        
        for (String projectId : projectIds) {
            routeEvent(projectId, event);
        }
    }
}
```

---

### Phase 1: 扩展本体模型 (向下兼容)

#### 1.1 新增流程对象类型

```java
// 新增: WorkflowObject
public class WorkflowObject extends OntologyObject {
    private WorkflowMode mode;      // AGILE, WATERFALL, HYBRID
    private Phase currentPhase;      // 当前阶段
    private List<Phase> phases;      // 阶段定义
    private Map<Phase, GateCondition> gates;  // 阶段门禁
}

// 阶段定义
public enum Phase {
    REQUIREMENT("需求"),
    DESIGN("设计"),
    DEVELOPMENT("开发"),
    CODE_REVIEW("代码审查"),
    TESTING("测试"),
    DEPLOYMENT("部署");
}

// 门禁条件
public class GateCondition {
    private Phase fromPhase;
    private Phase toPhase;
    private List<Predicate<WorkflowObject>> conditions;
    private boolean strict;  // 严格模式(瀑布) vs 宽松模式(敏捷)
}
```

#### 1.2 配置文件扩展 (支持多项目)

```yaml
# config/projects.yml (新增 - 项目定义)
projects:
  # 项目1: 航天卫星系统 (严格瀑布)
  - id: proj_satellite
    name: 卫星控制系统v1.0
    mode: WATERFALL
    priority: 10  # 最高优先级
    workflow_ref: wf_waterfall_v1
    config_dir: config/projects/satellite/
    resources:
      required:
        - test_platform_1  # 专用测试台
        - senior_developer_team
      shared:
        - code_review_service
    
  # 项目2: 电商App (敏捷)
  - id: proj_ecommerce
    name: 电商App Sprint-12
    mode: AGILE
    priority: 7
    workflow_ref: wf_agile_v1
    config_dir: config/projects/ecommerce/
    sprint_length: 2_weeks
    resources:
      required:
        - test_platform_2
      shared:
        - developer_pool
        - tester_pool
    
  # 项目3: 企业ERP (混合)
  - id: proj_erp
    name: ERP系统升级v3.0
    mode: HYBRID
    priority: 5
    workflow_ref: wf_hybrid_v1
    config_dir: config/projects/erp/
    waterfall_phases: [REQUIREMENT, DEPLOYMENT]
    agile_phases: [DEVELOPMENT, TESTING]
    resources:
      shared:
        - developer_pool
        - tester_pool
        - test_platform_3

# 共享资源定义
shared_resources:
  # 测试台资源
  - id: test_platform_1
    name: 航天级测试台
    type: TEST_PLATFORM
    attributes:
      location: 北京
      capability: high_reliability
      
  - id: test_platform_2
    name: 移动端测试台
    type: TEST_PLATFORM
    attributes:
      devices: [iOS, Android]
      
  - id: test_platform_3
    name: 企业级测试台
    type: TEST_PLATFORM
    attributes:
      load_test: true
      
  # 人员资源
  - id: developer_pool
    name: 开发人员池
    type: HUMAN_RESOURCE
    capacity: 10
    
  - id: tester_pool
    name: 测试人员池
    type: HUMAN_RESOURCE
    capacity: 5
    
  - id: senior_developer_team
    name: 资深开发团队
    type: HUMAN_RESOURCE
    capacity: 3
    
  # 服务资源
  - id: code_review_service
    name: 代码审查服务
    type: SERVICE
    concurrent: true  # 支持并发
```

```yaml
# config/workflows.yml (新增)
workflows:
  # 瀑布模式工作流
  - id: wf_waterfall_v1
    name: 标准瀑布流程
    mode: WATERFALL
    phases:
      - id: requirement
        name: 需求阶段
        entry_actions:
          - 创建需求文档
          - 分配需求分析师
        exit_conditions:
          - requirement_status: approved
          - requirement_review: passed
        
      - id: design
        name: 设计阶段
        depends_on: [requirement]  # 必须依赖
        entry_actions:
          - 创建设计文档
          - 分配架构师
        exit_conditions:
          - design_status: approved
          
      - id: development
        name: 开发阶段
        depends_on: [design]
        entry_actions:
          - 创建开发分支
          - 分配开发人员
        exit_conditions:
          - code_complete: true
          - unit_test_passed: true
          
      - id: code_review
        name: 代码审查阶段
        depends_on: [development]
        entry_actions:
          - 启动代码审查
        exit_conditions:
          - review_approved: true
          
      - id: testing
        name: 测试阶段
        depends_on: [code_review]
        entry_actions:
          - 预约测试台
          - 创建测试计划
        exit_conditions:
          - test_passed: true
          - defect_count: 0
          
      - id: deployment
        name: 部署阶段
        depends_on: [testing]
        entry_actions:
          - 准备发布
        exit_conditions:
          - deployed: true

  # 敏捷模式工作流
  - id: wf_agile_v1
    name: 敏捷Scrum流程
    mode: AGILE
    iteration_length: 2_weeks
    phases:
      - id: sprint_planning
        name: Sprint规划
        flexible: true  # 灵活模式
        
      - id: development
        name: 开发
        parallel_allowed: true  # 允许并行
        continuous: true  # 持续集成
        
      - id: daily_standup
        name: 每日站会
        recurring: true
        
      - id: sprint_review
        name: Sprint评审
        
      - id: retrospective
        name: 回顾会议

  # 混合模式工作流
  - id: wf_hybrid_v1
    name: 混合模式-敏捷开发+瀑布部署
    mode: HYBRID
    agile_phases: [development, testing]  # 敏捷阶段
    waterfall_phases: [requirement, design, deployment]  # 瀑布阶段
```

---

### Phase 2: 实现流程模式引擎

#### 2.1 流程模式接口

```java
// ProcessModeEngine.java
public interface ProcessModeEngine {
    /**
     * 检查是否允许执行动作
     */
    boolean canExecuteAction(WorkflowObject workflow, 
                            OntologyObject target, 
                            Action action);
    
    /**
     * 检查是否允许状态转换
     */
    boolean canTransitionState(WorkflowObject workflow,
                              StateChangeEvent event);
    
    /**
     * 执行阶段转换
     */
    void transitionPhase(WorkflowObject workflow, Phase toPhase);
    
    /**
     * 验证门禁条件
     */
    ValidationResult validateGate(WorkflowObject workflow, Phase phase);
}
```

#### 2.2 瀑布模式引擎

```java
// WaterfallEngine.java
public class WaterfallEngine implements ProcessModeEngine {
    
    @Override
    public boolean canExecuteAction(WorkflowObject workflow, 
                                   OntologyObject target, 
                                   Action action) {
        // 检查当前阶段是否允许此动作
        Phase currentPhase = workflow.getCurrentPhase();
        
        // 严格检查: 只能在指定阶段执行对应动作
        if (!isActionAllowedInPhase(action, currentPhase)) {
            System.out.println("❌ 瀑布模式: 动作 " + action.getName() + 
                             " 不允许在阶段 " + currentPhase + " 执行");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void transitionPhase(WorkflowObject workflow, Phase toPhase) {
        Phase currentPhase = workflow.getCurrentPhase();
        
        // 1. 检查依赖关系
        if (!isDependencySatisfied(workflow, toPhase)) {
            throw new IllegalStateException(
                "前置阶段未完成,无法进入阶段: " + toPhase);
        }
        
        // 2. 验证门禁条件
        ValidationResult result = validateGate(workflow, currentPhase);
        if (!result.isPassed()) {
            throw new IllegalStateException(
                "阶段 " + currentPhase + " 门禁未通过: " + result.getMessage());
        }
        
        // 3. 执行阶段转换
        System.out.println("✅ 瀑布模式: 阶段转换 " + 
                         currentPhase + " → " + toPhase);
        workflow.setCurrentPhase(toPhase);
        
        // 4. 执行入口动作
        executePhaseEntryActions(workflow, toPhase);
    }
    
    @Override
    public ValidationResult validateGate(WorkflowObject workflow, Phase phase) {
        GateCondition gate = workflow.getGates().get(phase);
        
        if (gate == null) {
            return ValidationResult.success();
        }
        
        // 严格检查所有条件
        for (Predicate<WorkflowObject> condition : gate.getConditions()) {
            if (!condition.test(workflow)) {
                return ValidationResult.failure(
                    "门禁条件未满足: " + phase);
            }
        }
        
        return ValidationResult.success();
    }
}
```

#### 2.3 敏捷模式引擎

```java
// AgileEngine.java
public class AgileEngine implements ProcessModeEngine {
    
    @Override
    public boolean canExecuteAction(WorkflowObject workflow, 
                                   OntologyObject target, 
                                   Action action) {
        // 敏捷模式: 更灵活,允许大部分动作
        
        // 只检查严重冲突
        if (hasConflict(workflow, action)) {
            System.out.println("⚠️ 敏捷模式: 检测到冲突,建议延后执行: " + 
                             action.getName());
            return true;  // 仍然允许,但给出警告
        }
        
        return true;
    }
    
    @Override
    public void transitionPhase(WorkflowObject workflow, Phase toPhase) {
        Phase currentPhase = workflow.getCurrentPhase();
        
        // 1. 软性依赖检查(可跳过)
        if (!isDependencySatisfied(workflow, toPhase)) {
            System.out.println("⚠️ 敏捷模式: 建议先完成前置阶段,但允许继续");
        }
        
        // 2. 宽松门禁(部分通过即可)
        ValidationResult result = validateGate(workflow, currentPhase);
        if (!result.isPassed()) {
            System.out.println("⚠️ 敏捷模式: 门禁未完全通过,但允许继续: " + 
                             result.getMessage());
        }
        
        // 3. 灵活转换
        System.out.println("✅ 敏捷模式: 灵活转换 " + 
                         currentPhase + " → " + toPhase);
        workflow.setCurrentPhase(toPhase);
        
        // 4. 执行入口动作(可选)
        executePhaseEntryActions(workflow, toPhase);
    }
    
    @Override
    public ValidationResult validateGate(WorkflowObject workflow, Phase phase) {
        GateCondition gate = workflow.getGates().get(phase);
        
        if (gate == null) {
            return ValidationResult.success();
        }
        
        // 宽松检查: 部分条件满足即可
        int passedCount = 0;
        int totalCount = gate.getConditions().size();
        
        for (Predicate<WorkflowObject> condition : gate.getConditions()) {
            if (condition.test(workflow)) {
                passedCount++;
            }
        }
        
        // 只要超过50%条件满足就通过
        if (passedCount >= totalCount * 0.5) {
            return ValidationResult.success();
        }
        
        return ValidationResult.warning(
            passedCount + "/" + totalCount + " 条件满足");
    }
}
```

#### 2.4 混合模式引擎

```java
// HybridEngine.java
public class HybridEngine implements ProcessModeEngine {
    private WaterfallEngine waterfallEngine;
    private AgileEngine agileEngine;
    private Set<Phase> waterfallPhases;
    private Set<Phase> agilePhases;
    
    @Override
    public boolean canExecuteAction(WorkflowObject workflow, 
                                   OntologyObject target, 
                                   Action action) {
        Phase currentPhase = workflow.getCurrentPhase();
        
        // 根据阶段选择引擎
        if (waterfallPhases.contains(currentPhase)) {
            return waterfallEngine.canExecuteAction(workflow, target, action);
        } else {
            return agileEngine.canExecuteAction(workflow, target, action);
        }
    }
    
    @Override
    public void transitionPhase(WorkflowObject workflow, Phase toPhase) {
        Phase currentPhase = workflow.getCurrentPhase();
        
        // 阶段边界处理
        if (waterfallPhases.contains(currentPhase) && 
            agilePhases.contains(toPhase)) {
            // 从瀑布转向敏捷: 严格验证
            System.out.println("🔄 混合模式: 瀑布→敏捷转换,执行严格验证");
            waterfallEngine.validateGate(workflow, currentPhase);
        } else if (agilePhases.contains(currentPhase) && 
                   waterfallPhases.contains(toPhase)) {
            // 从敏捷转向瀑布: 收敛检查
            System.out.println("🔄 混合模式: 敏捷→瀑布转换,执行收敛检查");
            convergenceCheck(workflow, currentPhase);
        }
        
        // 委托给对应引擎
        if (waterfallPhases.contains(toPhase)) {
            waterfallEngine.transitionPhase(workflow, toPhase);
        } else {
            agileEngine.transitionPhase(workflow, toPhase);
        }
    }
    
    private void convergenceCheck(WorkflowObject workflow, Phase phase) {
        // 确保敏捷阶段产出物完整
        System.out.println("🔍 检查敏捷阶段产出物完整性...");
        // 实现收敛逻辑
    }
}
```

---

### Phase 3: 增强规则引擎

#### 3.1 规则扩展

```yaml
# config/rules.yml (扩展)
rules:
  # 瀑布模式规则
  - id: rule_waterfall_001
    name: 瀑布-需求阶段门禁
    workflow_mode: WATERFALL  # 新增字段
    condition:
      workflow_phase: REQUIREMENT
      attribute: requirement_status
      value: completed
    gate_validation:  # 新增门禁验证
      required_fields:
        - requirement_doc_url
        - requirement_approval
        - stakeholder_signoff
    actions:
      - action: 进入设计阶段
        target: $workflow
        
  # 敏捷模式规则
  - id: rule_agile_001
    name: 敏捷-持续集成
    workflow_mode: AGILE
    condition:
      object_type: code_change
      event_type: git_push
    actions:
      - action: 启动CI流水线
        target: $source
        parallel: true  # 并行执行
      - action: 通知团队
        target: $source
        async: true  # 异步执行
        
  # 混合模式规则
  - id: rule_hybrid_001
    name: 混合-开发完成检查点
    workflow_mode: HYBRID
    condition:
      workflow_phase: DEVELOPMENT
      iteration: completed
    actions:
      - action: 收敛检查
        validation: strict
      - action: 准备进入测试
        gate_check: true
```

#### 3.2 规则引擎增强

```java
// DynamicNetworkEngine.java (增强)
public class DynamicNetworkEngine {
    private Map<WorkflowMode, ProcessModeEngine> modeEngines;
    private WorkflowObject currentWorkflow;  // 新增
    
    public void evaluateRules(StateChangeEvent event) {
        if (!running) return;
        
        // 1. 获取当前工作流
        WorkflowObject workflow = getCurrentWorkflow(event);
        
        // 2. 选择流程引擎
        ProcessModeEngine engine = modeEngines.get(workflow.getMode());
        
        // 3. 过滤规则(按工作流模式)
        List<DynamicRule> applicableRules = rules.values().stream()
            .filter(rule -> isApplicableToWorkflow(rule, workflow))
            .sorted(Comparator.comparingInt(DynamicRule::getPriority).reversed())
            .collect(Collectors.toList());
        
        // 4. 评估规则
        for (DynamicRule rule : applicableRules) {
            if (rule.evaluate(event)) {
                // 5. 检查流程模式约束
                if (canExecuteRule(engine, workflow, rule)) {
                    rule.execute(event, stateMonitor, actionRegistry);
                } else {
                    System.out.println("⚠️ 规则 " + rule.getName() + 
                                     " 被流程模式约束阻止");
                }
            }
        }
    }
    
    private boolean canExecuteRule(ProcessModeEngine engine, 
                                   WorkflowObject workflow, 
                                   DynamicRule rule) {
        // 检查规则中的所有动作是否允许执行
        for (RuleAction ruleAction : rule.getActions()) {
            OntologyObject target = stateMonitor.getObject(
                ruleAction.getTargetObjectId());
            Action action = actionRegistry.get(ruleAction.getActionName());
            
            if (!engine.canExecuteAction(workflow, target, action)) {
                return false;
            }
        }
        return true;
    }
}
```

---

### Phase 4: 命令行增强 (多项目管理)

```java
// Main.java (增强命令)
private static ProjectManager projectManager;  // 新增全局项目管理器
private static String currentProjectId;         // 当前活跃项目

private static void processCommand(String input) {
    String[] parts = input.split("\\s+", 2);
    String command = parts[0].toLowerCase();
    String args = parts.length > 1 ? parts[1] : "";

    switch (command) {
        // ... 现有命令 ...
        
        // === 多项目管理命令 ===
        case "project":
            handleProjectCommand(args);
            break;
            
        case "projects":
            projectManager.listProjects();
            break;
            
        case "switch":
            handleSwitchProject(args);
            break;
            
        case "resources":
            handleResourceCommand(args);
            break;
            
        // === 工作流命令(作用于当前项目) ===
        case "workflow":
            handleWorkflowCommand(args);
            break;
            
        case "phase":
            handlePhaseCommand(args);
            break;
            
        case "mode":
            handleModeCommand(args);
            break;
    }
}

// 项目管理命令
private static void handleProjectCommand(String args) {
    String[] parts = args.split("\\s+");
    if (parts.length < 1) {
        System.out.println("用法: project <create|list|delete|suspend|resume> [args]");
        return;
    }
    
    switch (parts[0]) {
        case "create":
            // project create satellite "卫星控制v1.0" waterfall 10
            if (parts.length < 5) {
                System.out.println("用法: project create <id> <name> <mode> <priority>");
                return;
            }
            String projectId = parts[1];
            String projectName = parts[2];
            WorkflowMode mode = WorkflowMode.valueOf(parts[3].toUpperCase());
            int priority = Integer.parseInt(parts[4]);
            
            ProjectContext ctx = projectManager.createProject(
                projectId, projectName, mode, priority);
            currentProjectId = projectId;
            
            System.out.println("✅ 项目已创建并切换: " + projectName);
            break;
            
        case "list":
            projectManager.listProjects();
            break;
            
        case "delete":
            // project delete satellite
            projectManager.deleteProject(parts[1]);
            break;
            
        case "suspend":
            // project suspend satellite
            projectManager.suspendProject(parts[1]);
            break;
            
        case "resume":
            // project resume satellite
            projectManager.resumeProject(parts[1]);
            break;
            
        case "info":
            // project info satellite
            showProjectInfo(parts[1]);
            break;
    }
}

// 切换项目
private static void handleSwitchProject(String projectId) {
    if (projectManager.hasProject(projectId)) {
        currentProjectId = projectId;
        ProjectContext ctx = projectManager.getProject(projectId);
        System.out.println("🔄 已切换到项目: " + ctx.getProjectName() + 
                         " [" + ctx.getMode() + "]");
        
        // 显示项目状态
        showProjectBrief(ctx);
    } else {
        System.out.println("❌ 项目不存在: " + projectId);
    }
}

// 资源管理命令
private static void handleResourceCommand(String args) {
    String[] parts = args.split("\\s+");
    if (parts.length < 1) {
        System.out.println("用法: resources <status|request|release> [args]");
        return;
    }
    
    switch (parts[0]) {
        case "status":
            projectManager.getResourceScheduler().showResourceStatus();
            break;
            
        case "request":
            // resources request test_platform_1
            String resourceId = parts[1];
            ProjectContext currentProject = getCurrentProject();
            boolean success = projectManager.getResourceScheduler()
                .requestResource(resourceId, currentProject, false);
            
            if (success) {
                System.out.println("✅ 资源请求成功: " + resourceId);
            } else {
                System.out.println("❌ 资源不可用: " + resourceId);
            }
            break;
            
        case "release":
            // resources release test_platform_1
            projectManager.getResourceScheduler().releaseResource(parts[1]);
            break;
            
        case "list":
            listAvailableResources();
            break;
    }
}

// 工作流命令(作用于当前项目)
private static void handleWorkflowCommand(String args) {
    ProjectContext ctx = getCurrentProject();
    if (ctx == null) {
        System.out.println("❌ 请先创建或切换到一个项目");
        return;
    }
    
    String[] parts = args.split("\\s+");
    // ... 工作流逻辑 (在当前项目上下文中执行)
}

// 阶段命令(作用于当前项目)
private static void handlePhaseCommand(String args) {
    ProjectContext ctx = getCurrentProject();
    if (ctx == null) {
        System.out.println("❌ 请先创建或切换到一个项目");
        return;
    }
    
    String[] parts = args.split("\\s+");
    if (parts.length < 1) {
        System.out.println("用法: phase <transition|status|validate>");
        return;
    }
    
    switch (parts[0]) {
        case "transition":
            // phase transition DEVELOPMENT
            Phase toPhase = Phase.valueOf(parts[1]);
            ctx.getProcessModeEngine().transitionPhase(
                ctx.getWorkflowObject(), toPhase);
            break;
            
        case "status":
            showPhaseStatus(ctx);
            break;
            
        case "validate":
            validateCurrentPhase(ctx);
            break;
    }
}

// 辅助方法
private static ProjectContext getCurrentProject() {
    if (currentProjectId == null) {
        return null;
    }
    return projectManager.getProject(currentProjectId);
}

private static void showProjectBrief(ProjectContext ctx) {
    System.out.println("┌─────────────────────────────────────┐");
    System.out.println("│ 项目: " + ctx.getProjectName());
    System.out.println("│ 模式: " + ctx.getMode());
    System.out.println("│ 优先级: " + ctx.getPriority());
    System.out.println("│ 状态: " + ctx.getStatus());
    System.out.println("│ 资源: " + ctx.getAllocatedResourceIds().size() + " 个");
    System.out.println("└─────────────────────────────────────┘");
}
```

---

## 🎯 多项目使用场景

### 场景1: 瀑布模式 - 航天软件开发

```bash
ontology> workflow create waterfall "卫星控制系统v1.0"
✅ 创建瀑布模式工作流: 卫星控制系统v1.0

ontology> phase status
当前阶段: REQUIREMENT
严格模式: ✅ 启用
门禁状态: ⏳ 等待完成

ontology> update req_satellite_001 requirement_status completed
❌ 瀑布模式: 门禁验证失败
   - requirement_doc_url: 缺失
   - stakeholder_signoff: 未签字

ontology> update req_satellite_001 requirement_doc_url "https://..."
ontology> update req_satellite_001 stakeholder_signoff "approved"

ontology> phase transition DESIGN
✅ 瀑布模式: 阶段转换 REQUIREMENT → DESIGN
   门禁验证: ✅ 通过
   执行动作: 创建设计文档
   分配: 架构师团队
```

### 场景2: 敏捷模式 - 互联网应用开发

```bash
ontology> workflow create agile "电商App Sprint-12"
✅ 创建敏捷模式工作流: 电商App Sprint-12

ontology> ingest git_commit
✅ 敏捷模式: 持续集成触发
   → 自动代码审查
   → 自动单元测试
   → 自动部署到测试环境

ontology> phase transition TESTING
⚠️ 敏捷模式: 开发未完全完成,但允许继续
✅ 敏捷模式: 灵活转换 DEVELOPMENT → TESTING

ontology> update sprint_12 iteration completed
✅ 自动触发Sprint评审准备
```

### 场景3: 混合模式 - 企业级系统

```bash
ontology> workflow create hybrid "ERP系统升级v3.0"
✅ 创建混合模式工作流
   瀑布阶段: [REQUIREMENT, DESIGN, DEPLOYMENT]
   敏捷阶段: [DEVELOPMENT, TESTING]

ontology> phase transition DEVELOPMENT
🔄 混合模式: 瀑布→敏捷转换,执行严格验证
✅ 设计阶段门禁通过,进入敏捷开发

ontology> update code_module_001 status developing
✅ 敏捷模式: 并行开发中...

ontology> phase transition DEPLOYMENT
🔄 混合模式: 敏捷→瀑布转换,执行收敛检查
🔍 检查产出物完整性...
   - 代码覆盖率: 85% ✅
   - 文档完整度: 90% ✅
   - 缺陷数量: 2 ⚠️
✅ 收敛检查通过,进入严格部署流程
```

---

## 📊 实施计划 (更新)

### v2.0.0 (核心功能 + 多项目支持)
- ✅ Phase 0: 多项目管理基础设施 (3周)
  - 项目上下文 & 项目管理器
  - 资源调度器 & 项目路由器
  - 多项目配置加载
  
- ✅ Phase 1: 扩展本体模型 (2周)
  - WorkflowObject & Phase枚举
  - GateCondition & 门禁验证
  
- ✅ Phase 2: 实现三种模式引擎 (3周)
  - WaterfallEngine (严格模式)
  - AgileEngine (灵活模式)
  - HybridEngine (混合模式)
  
- ✅ Phase 3: 规则引擎增强 (2周)
  - 规则过滤(按工作流模式)
  - 流程模式约束检查
  
- ✅ Phase 4: CLI增强 (2周)
  - 多项目管理命令
  - 资源管理命令
  - 项目切换与状态查询
  
- ✅ 测试和文档 (2周)
  - 多项目并发测试
  - 资源竞争测试
  - 完整文档

**总计: 14周 (约3.5个月)**

### v2.1.0 (可视化 + 监控)
- 多项目仪表板 (实时状态)
- 工作流图形化展示
- 资源使用率监控
- 项目性能分析
- 阶段仪表板
- 实时进度跟踪

### v2.2.0 (智能化)
- AI推荐最佳流程模式
- 自动门禁条件生成
- 流程优化建议
- 资源智能调度

### v2.3.0 (企业级特性)
- 分布式多节点支持
- 持久化存储 (数据库)
- RESTful API接口
- Webhook集成

---

## 🔧 技术要点 (多项目增强)

### 向下兼容
- 现有v1.0配置文件完全兼容
- 单项目模式自动转换为多项目模式
- 不指定工作流模式时默认为敏捷模式
- 渐进式升级,不影响现有功能

### 可扩展性
- 流程引擎可插拔设计
- 支持自定义流程模式
- 规则DSL扩展
- **资源类型可扩展** (新增)
- **项目调度策略可配置** (新增)

### 性能优化
- 规则缓存机制
- 异步门禁验证
- 并行动作执行
- **项目级线程池隔离** (新增)
- **资源锁粒度优化** (新增)
- **事件批处理** (新增)

### 并发安全
- **项目上下文线程安全** (新增)
- **资源分配CAS操作** (新增)
- **死锁检测与恢复** (新增)
- **优先级反转预防** (新增)

---

## 📖 配置文件示例 (多项目)

```yaml
# config/projects.yml - 多项目配置
projects:
  - id: proj_001
    name: "严格瀑布-卫星控制系统"
    mode: WATERFALL
    priority: 10
    strict_level: HIGH
    rollback_enabled: true
    config_dir: config/projects/satellite/
    
  - id: proj_002  
    name: "标准敏捷-电商App"
    mode: AGILE
    priority: 7
    sprint_length: 2_weeks
    daily_standup: true
    config_dir: config/projects/ecommerce/
    
  - id: proj_003
    name: "混合模式-ERP系统"
    mode: HYBRID
    priority: 5
    waterfall_phases: [REQUIREMENT, DEPLOYMENT]
    agile_phases: [DEVELOPMENT, TESTING]
    convergence_check: true
    config_dir: config/projects/erp/

# 资源池配置
shared_resources:
  - id: test_platform_1
    type: TEST_PLATFORM
    capacity: 1
    preemptible: false  # 不可抢占
  
  - id: developer_pool
    type: HUMAN_RESOURCE
    capacity: 10
    preemptible: true   # 可抢占
```

---

## 🎓 总结 (多项目版本)

这个升级方案实现了:

1. **灵活性**: 同一系统支持多种流程模式
2. **严格性**: 瀑布模式提供严格的门禁控制
3. **敏捷性**: 保留原有敏捷特性
4. **混合性**: 支持灵活组合
5. **兼容性**: 完全向下兼容v1.0
6. **🆕 并发性**: 支持多项目同时运行
7. **🆕 隔离性**: 项目间状态隔离
8. **🆕 共享性**: 资源池智能调度
9. **🆕 优先级**: 关键项目优先保障

**核心价值**: 
- 一套系统适应不同项目需求
- 从航天到互联网全覆盖
- 数据驱动 + 流程约束 = 最佳实践
- **🆕 多项目并发 + 资源共享 = 企业级解决方案**

**典型应用场景**:
- 🛰️ 航天项目 (瀑布, 优先级10) + 📱 互联网App (敏捷, 优先级7) + 🏢 企业ERP (混合, 优先级5)
- 同一团队同时推进不同类型项目
- 资源(测试台/人员)按需分配
- 高优先级项目可抢占低优先级资源
