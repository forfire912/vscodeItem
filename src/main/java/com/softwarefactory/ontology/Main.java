package com.softwarefactory.ontology;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;
import org.yaml.snakeyaml.Yaml;

/**
 * 软件工厂 v2.0 - 多项目并发支持 + 配置化本体/活动/关系库
 * 支持基于模板创建项目
 */
public class Main {
    
    private static ProjectManager projectManager;
    private static String currentProjectId;
    private static TemplateManager templateManager;
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  软件工厂系统 v2.0 - 多项目并发 + 多模式支持             ║");
        System.out.println("║  支持: 本体库/活动库/关系库 + 项目模板                 ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 初始化系统
        try {
            System.out.println("🔄 正在加载系统配置...");
            templateManager = new TemplateManager();
            templateManager.loadAllConfigs();
            System.out.println("✅ 配置加载完成!");
            System.out.println();
        } catch (Exception e) {
            System.err.println("❌ 配置加载失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        projectManager = new ProjectManager();
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.print("ontology> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                running = false;
                System.out.println("👋 再见!");
            } else {
                processCommand(input);
            }
        }
        
        scanner.close();
    }
    
    private static void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "templates":
                    templateManager.listTemplates();
                    break;
                case "template":
                    handleTemplateCommand(args);
                    break;
                case "library":
                    handleLibraryCommand(args);
                    break;
                case "project":
                    handleProjectCommand(args);
                    break;
                case "projects":
                    projectManager.listProjects();
                    break;
                case "switch":
                    switchProject(args.trim());
                    break;
                case "resources":
                    handleResourceCommand(args);
                    break;
                case "phase":
                    handlePhaseCommand(args);
                    break;
                case "update":
                    handleUpdateCommand(args);
                    break;
                case "create_object":
                    handleCreateObjectCommand(args);
                    break;
                case "list_objects":
                    handleListObjectsCommand(args);
                    break;
                case "status":
                    showStatus();
                    break;
                case "run":
                    if (args.trim().equalsIgnoreCase("all")) {
                        projectManager.runAllProjects();
                    }
                    break;
                case "demo":
                    runDemo();
                    break;
                default:
                    System.out.println("❌ 未知命令: " + command + " (输入 help 查看帮助)");
            }
        } catch (Exception e) {
            System.out.println("❌ 错误: " + e.getMessage());
        }
    }
    
    private static void handleProjectCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 1) {
            System.out.println("用法: project <create|delete|suspend|resume|info> [args]");
            return;
        }
        
        switch (parts[0]) {
            case "create":
                if (parts.length < 5) {
                    System.out.println("用法: project create <id> <name> <mode> <priority>");
                    System.out.println("示例: project create satellite \"卫星v1.0\" waterfall 10");
                    return;
                }
                String id = parts[1];
                String name = parts[2].replace("\"", "");
                WorkflowMode mode = WorkflowMode.valueOf(parts[3].toUpperCase());
                int priority = Integer.parseInt(parts[4]);
                
                ProjectContext ctx = projectManager.createProject(id, name, mode, priority);
                currentProjectId = id;
                break;
                
            case "delete":
                projectManager.deleteProject(parts[1]);
                if (parts[1].equals(currentProjectId)) {
                    currentProjectId = null;
                }
                break;
                
            case "suspend":
                projectManager.suspendProject(parts[1]);
                break;
                
            case "resume":
                projectManager.resumeProject(parts[1]);
                break;
                
            case "info":
                showProjectInfo(parts[1]);
                break;
        }
    }
    
    private static void switchProject(String projectId) {
        if (projectManager.hasProject(projectId)) {
            currentProjectId = projectId;
            ProjectContext ctx = projectManager.getProject(projectId);
            System.out.println("🔄 已切换到项目: " + ctx.getProjectName() + " [" + ctx.getMode() + "]");
            showProjectBrief(ctx);
        } else {
            System.out.println("❌ 项目不存在: " + projectId);
        }
    }
    
    private static void handleResourceCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 1) {
            System.out.println("用法: resources <status|request|release>");
            return;
        }
        
        switch (parts[0]) {
            case "status":
                projectManager.getResourceScheduler().showResourceStatus();
                break;
            case "request":
                if (currentProjectId == null) {
                    System.out.println("❌ 请先切换到一个项目");
                    return;
                }
                String resourceId = parts[1];
                ProjectContext currentProject = projectManager.getProject(currentProjectId);
                boolean success = projectManager.getResourceScheduler().requestResource(resourceId, currentProject);
                break;
            case "release":
                projectManager.getResourceScheduler().releaseResource(parts[1]);
                break;
        }
    }
    
    private static void handlePhaseCommand(String args) {
        if (currentProjectId == null) {
            System.out.println("❌ 请先创建或切换到一个项目");
            return;
        }
        
        ProjectContext ctx = projectManager.getProject(currentProjectId);
        String[] parts = args.split("\\s+");
        
        if (parts.length < 1) {
            System.out.println("用法: phase <transition|status>");
            return;
        }
        
        switch (parts[0]) {
            case "transition":
                Phase toPhase = Phase.valueOf(parts[1]);
                ctx.getModeEngine().transitionPhase(ctx, toPhase);
                break;
            case "status":
                showPhaseStatus(ctx);
                break;
        }
    }
    
    private static void handleUpdateCommand(String args) {
        if (currentProjectId == null) {
            System.out.println("❌ 请先创建或切换到一个项目");
            return;
        }
        
        String[] parts = args.split("\\s+");
        if (parts.length < 3) {
            System.out.println("用法: update <objectId> <attribute> <value>");
            return;
        }
        
        ProjectContext ctx = projectManager.getProject(currentProjectId);
        ctx.updateObject(parts[0], parts[1], parts[2]);
    }
    
    private static void handleCreateObjectCommand(String args) {
        if (currentProjectId == null) {
            System.out.println("❌ 请先创建或切换到一个项目");
            return;
        }
        
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            System.out.println("用法: create_object <objectId> <ontologyType>");
            System.out.println("示例: create_object req_001 requirement");
            return;
        }
        
        ProjectContext ctx = projectManager.getProject(currentProjectId);
        String objectId = parts[0];
        String ontologyType = parts[1];
        
        // 检查本体类型是否存在
        if (!ctx.getOntologies().containsKey(ontologyType)) {
            System.out.println("❌ 本体类型不存在: " + ontologyType);
            System.out.println("可用本体: " + ctx.getOntologies().keySet());
            return;
        }
        
        ctx.createObject(objectId, ontologyType);
    }
    
    private static void handleListObjectsCommand(String args) {
        if (currentProjectId == null) {
            System.out.println("❌ 请先创建或切换到一个项目");
            return;
        }
        
        ProjectContext ctx = projectManager.getProject(currentProjectId);
        ctx.listObjects();
    }
    
    private static void handleTemplateCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 1) {
            System.out.println("用法: template <show|create> [templateId] [projectId] [projectName] [priority]");
            return;
        }
        
        switch (parts[0]) {
            case "show":
                if (parts.length < 2) {
                    System.out.println("用法: template show <templateId>");
                    return;
                }
                templateManager.showTemplateDetails(parts[1]);
                break;
                
            case "create":
                if (parts.length < 5) {
                    System.out.println("用法: template create <templateId> <projectId> <projectName> <priority>");
                    System.out.println("示例: template create waterfall_medical_device med_dev_001 \"医疗设备v1.0\" 10");
                    return;
                }
                String templateId = parts[1];
                String projectId = parts[2];
                String projectName = parts[3].replace("\"", "");
                int priority = Integer.parseInt(parts[4]);
                
                ProjectContext ctx = templateManager.createProjectFromTemplate(
                    templateId, projectId, projectName, priority, projectManager);
                if (ctx != null) {
                    currentProjectId = projectId;
                    System.out.println("✅ 已基于模板 [" + templateId + "] 创建项目: " + projectName);
                    showProjectBrief(ctx);
                }
                break;
        }
    }
    
    private static void handleLibraryCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 1) {
            System.out.println("用法: library <ontologies|activities|relationships>");
            return;
        }
        
        switch (parts[0]) {
            case "ontologies":
                templateManager.listOntologies();
                break;
            case "activities":
                templateManager.listActivities();
                break;
            case "relationships":
                templateManager.listRelationships();
                break;
            default:
                System.out.println("❌ 未知库类型: " + parts[0]);
        }
    }

    
    private static void showStatus() {
        if (currentProjectId == null) {
            System.out.println("❌ 请先创建或切换到一个项目");
            return;
        }
        
        ProjectContext ctx = projectManager.getProject(currentProjectId);
        showProjectBrief(ctx);
    }
    
    private static void showProjectBrief(ProjectContext ctx) {
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│ 项目: " + ctx.getProjectName());
        System.out.println("│ 模式: " + ctx.getMode());
        System.out.println("│ 当前阶段: " + ctx.getCurrentPhase());
        System.out.println("│ 优先级: " + ctx.getPriority());
        System.out.println("│ 状态: " + ctx.getStatus());
        System.out.println("│ 资源: " + ctx.getAllocatedResources().size() + " 个");
        System.out.println("└─────────────────────────────────────┘");
    }
    
    private static void showPhaseStatus(ProjectContext ctx) {
        System.out.println("\n当前阶段: " + ctx.getCurrentPhase());
        System.out.println("模式: " + ctx.getMode());
        if (ctx.getMode() == WorkflowMode.WATERFALL) {
            System.out.println("严格模式: ✅ 启用");
        } else if (ctx.getMode() == WorkflowMode.AGILE) {
            System.out.println("灵活模式: ✅ 启用");
        }
    }
    
    private static void showProjectInfo(String projectId) {
        ProjectContext ctx = projectManager.getProject(projectId);
        if (ctx == null) {
            System.out.println("❌ 项目不存在: " + projectId);
            return;
        }
        
        System.out.println("\n项目详情:");
        System.out.println("  ID: " + ctx.getProjectId());
        System.out.println("  名称: " + ctx.getProjectName());
        System.out.println("  模式: " + ctx.getMode());
        System.out.println("  当前阶段: " + ctx.getCurrentPhase());
        System.out.println("  优先级: " + ctx.getPriority());
        System.out.println("  状态: " + ctx.getStatus());
        System.out.println("  已分配资源: " + ctx.getAllocatedResources());
    }
    
    private static void showHelp() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                     可用命令                            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        System.out.println("\n📚 库管理命令:");
        System.out.println("  library ontologies                           - 查看本体库");
        System.out.println("  library activities                           - 查看活动库");
        System.out.println("  library relationships                        - 查看关系库");
        
        System.out.println("\n📋 模板管理命令:");
        System.out.println("  templates                                    - 列出所有模板");
        System.out.println("  template show <templateId>                   - 查看模板详情");
        System.out.println("  template create <templateId> <projectId> <name> <priority>");
        System.out.println("                                               - 基于模板创建项目");
        
        System.out.println("\n🚀 项目管理命令:");
        System.out.println("  project create <id> <name> <mode> <priority> - 手动创建项目");
        System.out.println("  projects                                     - 列出所有项目");
        System.out.println("  switch <projectId>                           - 切换项目");
        System.out.println("  status                                       - 查看当前项目状态");
        
        System.out.println("\n🔄 流程控制命令:");
        System.out.println("  phase transition <PHASE>                     - 阶段转换");
        System.out.println("  phase status                                 - 查看阶段状态");
        System.out.println("  create_object <objId> <ontologyType>         - 创建对象实例");
        System.out.println("  update <objId> <attr> <value>                - 更新对象属性");
        System.out.println("  list_objects                                 - 列出项目对象");
        
        System.out.println("\n⚙️  资源管理命令:");
        System.out.println("  resources status                             - 查看资源");
        System.out.println("  resources request <resourceId>               - 请求资源");
        System.out.println("  resources release <resourceId>               - 释放资源");
        
        System.out.println("\n🎯 其他命令:");
        System.out.println("  run all                                      - 启动所有项目");
        System.out.println("  demo                                         - 运行演示");
        System.out.println("  help                                         - 显示帮助");
        System.out.println("  exit/quit                                    - 退出");
        System.out.println();
    }
    
    private static void runDemo() {
        System.out.println("\n🎬 开始演示: 多项目并发场景\n");
        
        // 创建3个不同模式的项目
        System.out.println("1️⃣ 创建航天卫星项目 (瀑布模式)");
        ProjectContext satellite = projectManager.createProject("satellite", "卫星控制系统v1.0", WorkflowMode.WATERFALL, 10);
        
        System.out.println("\n2️⃣ 创建电商App项目 (敏捷模式)");
        ProjectContext ecommerce = projectManager.createProject("ecommerce", "电商App Sprint-12", WorkflowMode.AGILE, 7);
        
        System.out.println("\n3️⃣ 创建ERP系统项目 (混合模式)");
        ProjectContext erp = projectManager.createProject("erp", "ERP系统升级v3.0", WorkflowMode.HYBRID, 5);
        
        // 显示项目列表
        System.out.println("\n4️⃣ 查看所有项目:");
        projectManager.listProjects();
        
        // 初始化资源
        System.out.println("\n5️⃣ 初始化资源池:");
        projectManager.getResourceScheduler().addResource("test_platform_1", "航天级测试台");
        projectManager.getResourceScheduler().addResource("test_platform_2", "移动端测试台");
        projectManager.getResourceScheduler().addResource("developer_pool", "开发人员池");
        
        // 资源分配
        System.out.println("\n6️⃣ 资源分配:");
        projectManager.getResourceScheduler().requestResource("test_platform_1", satellite);
        projectManager.getResourceScheduler().requestResource("developer_pool", ecommerce);
        
        // 查看资源状态
        System.out.println("\n7️⃣ 资源使用情况:");
        projectManager.getResourceScheduler().showResourceStatus();
        
        // 瀑布模式演示
        System.out.println("\n8️⃣ 瀑布模式演示 (卫星项目):");
        currentProjectId = "satellite";
        System.out.println("   尝试跳过需求阶段直接进入开发...");
        satellite.getModeEngine().transitionPhase(satellite, Phase.DEVELOPMENT);
        
        // 敏捷模式演示
        System.out.println("\n9️⃣ 敏捷模式演示 (电商项目):");
        currentProjectId = "ecommerce";
        System.out.println("   灵活跳转到测试阶段...");
        ecommerce.getModeEngine().transitionPhase(ecommerce, Phase.TESTING);
        
        // 混合模式演示
        System.out.println("\n🔟 混合模式演示 (ERP项目):");
        currentProjectId = "erp";
        erp.getModeEngine().transitionPhase(erp, Phase.DEVELOPMENT);
        
        System.out.println("\n✅ 演示完成!");
    }
}

// ==================== 枚举类 ====================

enum WorkflowMode {
    WATERFALL,  // 瀑布模式
    AGILE,      // 敏捷模式
    HYBRID      // 混合模式
}

enum Phase {
    REQUIREMENT,
    DESIGN,
    DEVELOPMENT,
    CODE_REVIEW,
    TESTING,
    DEPLOYMENT
}

enum ProjectStatus {
    ACTIVE,
    SUSPENDED,
    WAITING,
    COMPLETED
}

// ==================== 项目上下文 ====================

class ProjectContext {
    private String projectId;
    private String projectName;
    private WorkflowMode mode;
    private int priority;
    private ProjectStatus status;
    private Phase currentPhase;
    private ProcessModeEngine modeEngine;
    private Set<String> allocatedResources;
    private Map<String, Object> objects;
    
    // 项目的本体集、活动集、关系集
    private Map<String, OntologyDef> ontologies;
    private Map<String, ActivityDef> activities;
    private Map<String, RelationshipDef> relationships;
    
    // 决策网络引擎
    private DecisionNetworkEngine decisionNetwork;
    
    public ProjectContext(String projectId, String projectName, WorkflowMode mode, int priority) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.mode = mode;
        this.priority = priority;
        this.status = ProjectStatus.ACTIVE;
        this.currentPhase = Phase.REQUIREMENT;
        this.allocatedResources = new HashSet<>();
        this.objects = new HashMap<>();
        this.ontologies = new HashMap<>();
        this.activities = new HashMap<>();
        this.relationships = new HashMap<>();
        
        // 初始化决策网络引擎
        this.decisionNetwork = new DecisionNetworkEngine(projectId);
        
        // 根据模式创建引擎
        switch (mode) {
            case WATERFALL:
                this.modeEngine = new WaterfallEngine();
                break;
            case AGILE:
                this.modeEngine = new AgileEngine();
                break;
            case HYBRID:
                this.modeEngine = new HybridEngine();
                break;
        }
    }
    
    public void updateObject(String objId, String attr, String value) {
        // 获取旧值
        String oldValue = (String) objects.get(objId + "." + attr);
        
        System.out.println("📝 更新对象: " + objId + "." + attr + " = " + value);
        objects.put(objId + "." + attr, value);
        
        // 触发决策网络事件
        String objectType = (String) objects.get(objId + ".type");
        if (objectType == null) {
            objectType = objId.split("_")[0]; // 从ID推断类型
        }
        OntologyEvent event = new OntologyEvent(
            projectId, objId, objectType, attr, oldValue, value);
        decisionNetwork.emitEvent(event);
    }
    
    public void createObject(String objectId, String ontologyType) {
        // 存储对象类型
        objects.put(objectId + ".type", ontologyType);
        System.out.println("✅ 创建对象: " + objectId + " (类型: " + ontologyType + ")");
        
        // 触发创建事件
        OntologyEvent event = new OntologyEvent(
            projectId, objectId, ontologyType, "created", null, "true");
        decisionNetwork.emitEvent(event);
    }
    
    public void listObjects() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║          项目对象列表 - " + projectId + "                  ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        // 收集所有对象ID
        Set<String> objectIds = new HashSet<>();
        for (String key : objects.keySet()) {
            String objId = key.substring(0, key.lastIndexOf('.'));
            objectIds.add(objId);
        }
        
        if (objectIds.isEmpty()) {
            System.out.println("  (无对象)");
            return;
        }
        
        // 按本体类型分组
        Map<String, List<String>> byType = new HashMap<>();
        for (String objId : objectIds) {
            String type = (String) objects.get(objId + ".type");
            byType.computeIfAbsent(type, k -> new ArrayList<>()).add(objId);
        }
        
        // 显示每种类型的对象
        byType.forEach((type, objList) -> {
            System.out.println("📦 " + type + ":");
            objList.forEach(objId -> {
                System.out.println("  - " + objId);
                // 显示对象的所有属性
                objects.keySet().stream()
                    .filter(k -> k.startsWith(objId + ".") && !k.endsWith(".type"))
                    .forEach(k -> {
                        String attr = k.substring(objId.length() + 1);
                        System.out.println("      " + attr + " = " + objects.get(k));
                    });
            });
            System.out.println();
        });
        
        System.out.println("总计: " + objectIds.size() + " 个对象\n");
    }
    
    public void addOntology(String id, OntologyDef ontology) {
        if (ontology != null) {
            ontologies.put(id, ontology);
        }
    }
    
    public void addActivity(String id, ActivityDef activity) {
        if (activity != null) {
            activities.put(id, activity);
        }
    }
    
    public void addRelationship(String id, RelationshipDef relationship) {
        if (relationship != null) {
            relationships.put(id, relationship);
        }
    }
    
    // 初始化决策网络 - 从关系集生成规则
    public void initializeDecisionNetwork() {
        List<DecisionRule> rules = RuleFactory.createRulesFromRelationships(relationships);
        for (DecisionRule rule : rules) {
            decisionNetwork.addRule(rule);
        }
        System.out.println("  ✓ 决策网络已初始化: " + rules.size() + " 个规则");
    }
    
    // Getters and Setters
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public WorkflowMode getMode() { return mode; }
    public int getPriority() { return priority; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public Phase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Phase phase) { this.currentPhase = phase; }
    public ProcessModeEngine getModeEngine() { return modeEngine; }
    public Set<String> getAllocatedResources() { return allocatedResources; }
    public Map<String, OntologyDef> getOntologies() { return ontologies; }
    public Map<String, ActivityDef> getActivities() { return activities; }
    public Map<String, RelationshipDef> getRelationships() { return relationships; }
    public DecisionNetworkEngine getDecisionNetwork() { return decisionNetwork; }
}

// ==================== 流程模式引擎接口 ====================

interface ProcessModeEngine {
    void transitionPhase(ProjectContext project, Phase toPhase);
}

class WaterfallEngine implements ProcessModeEngine {
    @Override
    public void transitionPhase(ProjectContext project, Phase toPhase) {
        Phase currentPhase = project.getCurrentPhase();
        
        // 检查依赖关系
        if (!isValidTransition(currentPhase, toPhase)) {
            System.out.println("❌ 瀑布模式: 必须按顺序完成各阶段");
            System.out.println("   当前阶段: " + currentPhase);
            System.out.println("   目标阶段: " + toPhase);
            System.out.println("   请先完成当前阶段的门禁条件");
            return;
        }
        
        System.out.println("✅ 瀑布模式: 阶段转换 " + currentPhase + " → " + toPhase);
        System.out.println("   门禁验证: ✅ 通过");
        project.setCurrentPhase(toPhase);
    }
    
    private boolean isValidTransition(Phase from, Phase to) {
        return to.ordinal() == from.ordinal() + 1;
    }
}

class AgileEngine implements ProcessModeEngine {
    @Override
    public void transitionPhase(ProjectContext project, Phase toPhase) {
        Phase currentPhase = project.getCurrentPhase();
        
        if (Math.abs(toPhase.ordinal() - currentPhase.ordinal()) > 1) {
            System.out.println("⚠️ 敏捷模式: 建议按顺序进行,但允许跳转");
        }
        
        System.out.println("✅ 敏捷模式: 灵活转换 " + currentPhase + " → " + toPhase);
        project.setCurrentPhase(toPhase);
    }
}

class HybridEngine implements ProcessModeEngine {
    private WaterfallEngine waterfallEngine = new WaterfallEngine();
    private AgileEngine agileEngine = new AgileEngine();
    private Set<Phase> waterfallPhases = Set.of(Phase.REQUIREMENT, Phase.DESIGN, Phase.DEPLOYMENT);
    
    @Override
    public void transitionPhase(ProjectContext project, Phase toPhase) {
        Phase currentPhase = project.getCurrentPhase();
        
        boolean isCurrentWaterfall = waterfallPhases.contains(currentPhase);
        boolean isTargetWaterfall = waterfallPhases.contains(toPhase);
        
        if (isCurrentWaterfall && !isTargetWaterfall) {
            System.out.println("🔄 混合模式: 瀑布→敏捷转换");
            waterfallEngine.transitionPhase(project, toPhase);
        } else if (!isCurrentWaterfall && isTargetWaterfall) {
            System.out.println("🔄 混合模式: 敏捷→瀑布转换,执行收敛检查");
            System.out.println("🔍 检查产出物完整性...");
            agileEngine.transitionPhase(project, toPhase);
        } else if (isCurrentWaterfall) {
            waterfallEngine.transitionPhase(project, toPhase);
        } else {
            agileEngine.transitionPhase(project, toPhase);
        }
    }
}

// ==================== 项目管理器 ====================

class ProjectManager {
    private Map<String, ProjectContext> projects;
    private ResourceScheduler resourceScheduler;
    
    public ProjectManager() {
        this.projects = new HashMap<>();
        this.resourceScheduler = new ResourceScheduler(this);
    }
    
    public ProjectContext createProject(String projectId, String projectName, WorkflowMode mode, int priority) {
        ProjectContext context = new ProjectContext(projectId, projectName, mode, priority);
        projects.put(projectId, context);
        System.out.println("✅ 创建项目: " + projectName + " [" + mode + "]");
        System.out.println("   优先级: " + priority);
        return context;
    }
    
    public void deleteProject(String projectId) {
        ProjectContext ctx = projects.remove(projectId);
        if (ctx != null) {
            // 释放资源
            for (String resourceId : ctx.getAllocatedResources()) {
                resourceScheduler.releaseResource(resourceId);
            }
            System.out.println("🗑️ 删除项目: " + ctx.getProjectName());
        }
    }
    
    public void suspendProject(String projectId) {
        ProjectContext ctx = projects.get(projectId);
        if (ctx != null) {
            ctx.setStatus(ProjectStatus.SUSPENDED);
            System.out.println("⏸️ 暂停项目: " + ctx.getProjectName());
        }
    }
    
    public void resumeProject(String projectId) {
        ProjectContext ctx = projects.get(projectId);
        if (ctx != null) {
            ctx.setStatus(ProjectStatus.ACTIVE);
            System.out.println("▶️ 恢复项目: " + ctx.getProjectName());
        }
    }
    
    public void listProjects() {
        System.out.println("\n📋 项目列表:");
        System.out.println("┌────────────┬──────────────────┬──────────┬────────┬──────────┐");
        System.out.println("│ ID         │ 项目名            │ 模式     │ 优先级 │ 状态     │");
        System.out.println("├────────────┼──────────────────┼──────────┼────────┼──────────┤");
        
        for (ProjectContext ctx : projects.values()) {
            System.out.printf("│ %-10s │ %-16s │ %-8s │ %-6d │ %-8s │%n",
                ctx.getProjectId(),
                truncate(ctx.getProjectName(), 16),
                ctx.getMode(),
                ctx.getPriority(),
                ctx.getStatus()
            );
        }
        
        System.out.println("└────────────┴──────────────────┴──────────┴────────┴──────────┘");
    }
    
    public void runAllProjects() {
        List<ProjectContext> activeProjects = projects.values().stream()
            .filter(ctx -> ctx.getStatus() == ProjectStatus.ACTIVE)
            .sorted(Comparator.comparingInt(ProjectContext::getPriority).reversed())
            .collect(Collectors.toList());
        
        System.out.println("🚀 启动 " + activeProjects.size() + " 个活跃项目...");
        for (ProjectContext ctx : activeProjects) {
            System.out.println("  ✅ " + ctx.getProjectName() + " 已启动");
        }
    }
    
    public boolean hasProject(String projectId) {
        return projects.containsKey(projectId);
    }
    
    public ProjectContext getProject(String projectId) {
        return projects.get(projectId);
    }
    
    public ResourceScheduler getResourceScheduler() {
        return resourceScheduler;
    }
    
    private String truncate(String str, int length) {
        return str.length() > length ? str.substring(0, length - 2) + ".." : str;
    }
}

// ==================== 资源调度器 ====================

class ResourceScheduler {
    private Map<String, SharedResource> resourcePool;
    private Map<String, String> resourceAllocation;  // resourceId -> projectId
    private ProjectManager projectManager;
    
    public ResourceScheduler(ProjectManager projectManager) {
        this.resourcePool = new HashMap<>();
        this.resourceAllocation = new HashMap<>();
        this.projectManager = projectManager;
    }
    
    public void addResource(String resourceId, String name) {
        resourcePool.put(resourceId, new SharedResource(resourceId, name));
        System.out.println("➕ 添加资源: " + name);
    }
    
    public boolean requestResource(String resourceId, ProjectContext project) {
        SharedResource resource = resourcePool.get(resourceId);
        
        if (resource == null) {
            System.out.println("❌ 资源不存在: " + resourceId);
            return false;
        }
        
        if (!resource.isAvailable()) {
            String currentOwner = resourceAllocation.get(resourceId);
            ProjectContext ownerProject = projectManager.getProject(currentOwner);
            
            if (project.getPriority() > ownerProject.getPriority()) {
                System.out.println("⚠️ 高优先级项目抢占资源: " + resourceId);
                ownerProject.setStatus(ProjectStatus.SUSPENDED);
                releaseResource(resourceId);
            } else {
                System.out.println("❌ 资源不可用: " + resourceId);
                System.out.println("   当前占用: " + ownerProject.getProjectName() + " (优先级 " + ownerProject.getPriority() + ")");
                return false;
            }
        }
        
        resource.allocate(project.getProjectId());
        resourceAllocation.put(resourceId, project.getProjectId());
        project.getAllocatedResources().add(resourceId);
        
        System.out.println("✅ 分配资源 " + resourceId + " → " + project.getProjectName());
        return true;
    }
    
    public void releaseResource(String resourceId) {
        SharedResource resource = resourcePool.get(resourceId);
        if (resource != null) {
            String projectId = resourceAllocation.remove(resourceId);
            resource.release();
            
            if (projectId != null) {
                ProjectContext project = projectManager.getProject(projectId);
                if (project != null) {
                    project.getAllocatedResources().remove(resourceId);
                    System.out.println("🔓 释放资源 " + resourceId + " ← " + project.getProjectName());
                }
            }
        }
    }
    
    public void showResourceStatus() {
        System.out.println("\n📊 资源使用情况:");
        System.out.println("┌──────────────────┬────────┬──────────────────┐");
        System.out.println("│ 资源ID            │ 状态   │ 占用项目          │");
        System.out.println("├──────────────────┼────────┼──────────────────┤");
        
        for (SharedResource resource : resourcePool.values()) {
            String owner = resourceAllocation.get(resource.getId());
            String ownerName = "-";
            if (owner != null) {
                ProjectContext project = projectManager.getProject(owner);
                if (project != null) {
                    ownerName = truncate(project.getProjectName(), 16);
                }
            }
            String status = resource.isAvailable() ? "空闲" : "占用";
            
            System.out.printf("│ %-16s │ %-6s │ %-16s │%n",
                truncate(resource.getId(), 16),
                status,
                ownerName
            );
        }
        
        System.out.println("└──────────────────┴────────┴──────────────────┘");
    }
    
    private String truncate(String str, int length) {
        return str.length() > length ? str.substring(0, length - 2) + ".." : str;
    }
}

// ==================== 共享资源 ====================

class SharedResource {
    private String id;
    private String name;
    private boolean available;
    private String currentOwner;
    
    public SharedResource(String id, String name) {
        this.id = id;
        this.name = name;
        this.available = true;
    }
    
    public void allocate(String projectId) {
        this.available = false;
        this.currentOwner = projectId;
    }
    
    public void release() {
        this.available = true;
        this.currentOwner = null;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isAvailable() { return available; }
    public String getCurrentOwner() { return currentOwner; }
}

// ==================== 配置数据模型 ====================

class OntologyDef {
    private String id;
    private String name;
    private String category;
    private List<Map<String, Object>> attributes;
    
    public OntologyDef() {}
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<Map<String, Object>> getAttributes() { return attributes; }
    public void setAttributes(List<Map<String, Object>> attributes) { this.attributes = attributes; }
}

class ActivityDef {
    private String id;
    private String name;
    private String phase;
    private String category;
    private String description;
    private int duration_estimate;
    
    public ActivityDef() {}
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDuration_estimate() { return duration_estimate; }
    public void setDuration_estimate(int duration_estimate) { this.duration_estimate = duration_estimate; }
}

class RelationshipDef {
    private String id;
    private String name;
    private List<String> source_types;
    private List<String> target_types;
    private String cardinality;
    private String description;
    
    public RelationshipDef() {}
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getSource_types() { return source_types; }
    public void setSource_types(List<String> source_types) { this.source_types = source_types; }
    public List<String> getTarget_types() { return target_types; }
    public void setTarget_types(List<String> target_types) { this.target_types = target_types; }
    public String getCardinality() { return cardinality; }
    public void setCardinality(String cardinality) { this.cardinality = cardinality; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

class ProjectTemplate {
    private String template_id;
    private String template_name;
    private String workflow_mode;
    private String description;
    private List<String> ontologies;
    private Map<String, List<String>> activities;
    private List<String> relationships;
    
    public ProjectTemplate() {}
    
    public String getTemplate_id() { return template_id; }
    public void setTemplate_id(String template_id) { this.template_id = template_id; }
    public String getTemplate_name() { return template_name; }
    public void setTemplate_name(String template_name) { this.template_name = template_name; }
    public String getWorkflow_mode() { return workflow_mode; }
    public void setWorkflow_mode(String workflow_mode) { this.workflow_mode = workflow_mode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getOntologies() { return ontologies; }
    public void setOntologies(List<String> ontologies) { this.ontologies = ontologies; }
    public Map<String, List<String>> getActivities() { return activities; }
    public void setActivities(Map<String, List<String>> activities) { this.activities = activities; }
    public List<String> getRelationships() { return relationships; }
    public void setRelationships(List<String> relationships) { this.relationships = relationships; }
}

// ==================== 模板管理器 ====================

class TemplateManager {
    private Map<String, OntologyDef> ontologyLibrary;
    private Map<String, ActivityDef> activityLibrary;
    private Map<String, RelationshipDef> relationshipLibrary;
    private Map<String, ProjectTemplate> templates;
    
    public TemplateManager() {
        this.ontologyLibrary = new HashMap<>();
        this.activityLibrary = new HashMap<>();
        this.relationshipLibrary = new HashMap<>();
        this.templates = new HashMap<>();
    }
    
    public void loadAllConfigs() throws IOException {
        loadOntologyLibrary();
        loadActivityLibrary();
        loadRelationshipLibrary();
        loadTemplates();
    }
    
    @SuppressWarnings("unchecked")
    private void loadOntologyLibrary() throws IOException {
        Yaml yaml = new Yaml();
        String configPath = "config/ontology_library.yml";
        
        try (InputStream input = new FileInputStream(configPath)) {
            Map<String, Object> data = yaml.load(input);
            List<Map<String, Object>> ontologies = (List<Map<String, Object>>) data.get("ontologies");
            
            for (Map<String, Object> ont : ontologies) {
                OntologyDef def = new OntologyDef();
                def.setId((String) ont.get("id"));
                def.setName((String) ont.get("name"));
                def.setCategory((String) ont.get("category"));
                def.setAttributes((List<Map<String, Object>>) ont.get("attributes"));
                ontologyLibrary.put(def.getId(), def);
            }
            System.out.println("  ✓ 已加载 " + ontologyLibrary.size() + " 个本体定义");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadActivityLibrary() throws IOException {
        Yaml yaml = new Yaml();
        String configPath = "config/activity_library.yml";
        
        try (InputStream input = new FileInputStream(configPath)) {
            Map<String, Object> data = yaml.load(input);
            List<Map<String, Object>> activities = (List<Map<String, Object>>) data.get("activities");
            
            for (Map<String, Object> act : activities) {
                ActivityDef def = new ActivityDef();
                def.setId((String) act.get("id"));
                def.setName((String) act.get("name"));
                def.setPhase((String) act.get("phase"));
                def.setCategory((String) act.get("category"));
                def.setDescription((String) act.get("description"));
                Object durationObj = act.get("duration_estimate");
                int duration = 0;
                if (durationObj instanceof Integer) {
                    duration = (Integer) durationObj;
                } else if (durationObj instanceof Double) {
                    duration = ((Double) durationObj).intValue();
                } else if (durationObj instanceof String) {
                    duration = Integer.parseInt((String) durationObj);
                }
                def.setDuration_estimate(duration);
                activityLibrary.put(def.getId(), def);
            }
            System.out.println("  ✓ 已加载 " + activityLibrary.size() + " 个活动定义");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadRelationshipLibrary() throws IOException {
        Yaml yaml = new Yaml();
        String configPath = "config/relationship_library.yml";
        
        try (InputStream input = new FileInputStream(configPath)) {
            Map<String, Object> data = yaml.load(input);
            List<Map<String, Object>> relationships = (List<Map<String, Object>>) data.get("relationships");
            
            for (Map<String, Object> rel : relationships) {
                RelationshipDef def = new RelationshipDef();
                def.setId((String) rel.get("id"));
                def.setName((String) rel.get("name"));
                def.setSource_types((List<String>) rel.get("source_types"));
                def.setTarget_types((List<String>) rel.get("target_types"));
                def.setCardinality((String) rel.get("cardinality"));
                def.setDescription((String) rel.get("description"));
                relationshipLibrary.put(def.getId(), def);
            }
            System.out.println("  ✓ 已加载 " + relationshipLibrary.size() + " 个关系定义");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadTemplates() throws IOException {
        Yaml yaml = new Yaml();
        Path templatesDir = Paths.get("config/templates");
        
        if (!Files.exists(templatesDir)) {
            System.out.println("  ⚠ 模板目录不存在: " + templatesDir);
            return;
        }
        
        Files.list(templatesDir)
            .filter(path -> path.toString().endsWith(".yml"))
            .forEach(path -> {
                try (InputStream input = new FileInputStream(path.toFile())) {
                    Map<String, Object> data = yaml.load(input);
                    ProjectTemplate template = new ProjectTemplate();
                    template.setTemplate_id((String) data.get("template_id"));
                    template.setTemplate_name((String) data.get("template_name"));
                    template.setWorkflow_mode((String) data.get("workflow_mode"));
                    template.setDescription((String) data.get("description"));
                    template.setOntologies((List<String>) data.get("ontologies"));
                    template.setActivities((Map<String, List<String>>) data.get("activities"));
                    template.setRelationships((List<String>) data.get("relationships"));
                    templates.put(template.getTemplate_id(), template);
                } catch (Exception e) {
                    System.err.println("  ✗ 加载模板失败: " + path.getFileName() + " - " + e.getMessage());
                }
            });
        
        System.out.println("  ✓ 已加载 " + templates.size() + " 个项目模板");
    }
    
    public void listTemplates() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  可用项目模板                           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        if (templates.isEmpty()) {
            System.out.println("  (无可用模板)");
            return;
        }
        
        templates.values().forEach(t -> {
            System.out.println("📋 " + t.getTemplate_id());
            System.out.println("   名称: " + t.getTemplate_name());
            System.out.println("   模式: " + t.getWorkflow_mode());
            System.out.println("   说明: " + t.getDescription());
            System.out.println("   本体: " + t.getOntologies().size() + " 个");
            System.out.println("   活动: " + (t.getActivities() != null ? 
                t.getActivities().values().stream().mapToInt(List::size).sum() : 0) + " 个");
            System.out.println("   关系: " + (t.getRelationships() != null ? t.getRelationships().size() : 0) + " 个");
            System.out.println();
        });
    }
    
    public void showTemplateDetails(String templateId) {
        ProjectTemplate template = templates.get(templateId);
        if (template == null) {
            System.out.println("❌ 模板不存在: " + templateId);
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║              模板详情: " + template.getTemplate_name());
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        System.out.println("\n基本信息:");
        System.out.println("  ID: " + template.getTemplate_id());
        System.out.println("  模式: " + template.getWorkflow_mode());
        System.out.println("  说明: " + template.getDescription());
        
        System.out.println("\n本体集 (" + template.getOntologies().size() + " 个):");
        template.getOntologies().forEach(id -> {
            OntologyDef ont = ontologyLibrary.get(id);
            if (ont != null) {
                System.out.println("  - " + ont.getId() + " (" + ont.getName() + ") [" + ont.getCategory() + "]");
            }
        });
        
        if (template.getActivities() != null) {
            System.out.println("\n活动集 (按阶段):");
            template.getActivities().forEach((phase, activities) -> {
                System.out.println("  " + phase + ":");
                activities.forEach(id -> {
                    ActivityDef act = activityLibrary.get(id);
                    if (act != null) {
                        System.out.println("    - " + act.getId() + " (" + act.getName() + ")");
                    }
                });
            });
        }
        
        if (template.getRelationships() != null) {
            System.out.println("\n关系集 (" + template.getRelationships().size() + " 个):");
            template.getRelationships().forEach(id -> {
                RelationshipDef rel = relationshipLibrary.get(id);
                if (rel != null) {
                    System.out.println("  - " + rel.getId() + " (" + rel.getName() + ")");
                }
            });
        }
        System.out.println();
    }
    
    public ProjectContext createProjectFromTemplate(String templateId, String projectId, 
                                                     String projectName, int priority,
                                                     ProjectManager projectManager) {
        ProjectTemplate template = templates.get(templateId);
        if (template == null) {
            System.out.println("❌ 模板不存在: " + templateId);
            return null;
        }
        
        // 解析工作流模式
        WorkflowMode mode;
        try {
            mode = WorkflowMode.valueOf(template.getWorkflow_mode().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ 无效的工作流模式: " + template.getWorkflow_mode());
            return null;
        }
        
        // 创建项目
        ProjectContext ctx = projectManager.createProject(projectId, projectName, mode, priority);
        
        // 初始化本体集
        if (template.getOntologies() != null) {
            System.out.println("  ✓ 初始化本体集: " + template.getOntologies().size() + " 个");
            template.getOntologies().forEach(ontId -> {
                ctx.addOntology(ontId, ontologyLibrary.get(ontId));
            });
        }
        
        // 初始化活动集
        if (template.getActivities() != null) {
            int totalActivities = template.getActivities().values().stream()
                .mapToInt(List::size).sum();
            System.out.println("  ✓ 初始化活动集: " + totalActivities + " 个");
            template.getActivities().forEach((phase, activities) -> {
                activities.forEach(actId -> {
                    ctx.addActivity(actId, activityLibrary.get(actId));
                });
            });
        }
        
        // 初始化关系集
        if (template.getRelationships() != null) {
            System.out.println("  ✓ 初始化关系集: " + template.getRelationships().size() + " 个");
            template.getRelationships().forEach(relId -> {
                ctx.addRelationship(relId, relationshipLibrary.get(relId));
            });
        }
        
        // 初始化决策网络 - 从关系集生成规则
        ctx.initializeDecisionNetwork();
        
        return ctx;
    }
    
    public void listOntologies() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    本体库                              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        Map<String, List<OntologyDef>> byCategory = ontologyLibrary.values().stream()
            .collect(Collectors.groupingBy(OntologyDef::getCategory));
        
        byCategory.forEach((category, ontologies) -> {
            System.out.println("📦 " + category + ":");
            ontologies.forEach(ont -> {
                System.out.println("  - " + ont.getId() + " (" + ont.getName() + ")");
            });
            System.out.println();
        });
        
        System.out.println("总计: " + ontologyLibrary.size() + " 个本体定义\n");
    }
    
    public void listActivities() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    活动库                              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        Map<String, List<ActivityDef>> byPhase = activityLibrary.values().stream()
            .filter(a -> a.getPhase() != null)
            .collect(Collectors.groupingBy(ActivityDef::getPhase));
        
        byPhase.forEach((phase, activities) -> {
            System.out.println("📍 " + phase + ":");
            activities.forEach(act -> {
                System.out.println("  - " + act.getId() + " (" + act.getName() + 
                    ") [预计: " + act.getDuration_estimate() + "天]");
            });
            System.out.println();
        });
        
        System.out.println("总计: " + activityLibrary.size() + " 个活动定义\n");
    }
    
    public void listRelationships() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    关系库                              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        relationshipLibrary.values().forEach(rel -> {
            System.out.println("🔗 " + rel.getId() + " (" + rel.getName() + ")");
            System.out.println("   基数: " + rel.getCardinality());
            System.out.println("   说明: " + rel.getDescription());
            System.out.println();
        });
        
        System.out.println("总计: " + relationshipLibrary.size() + " 个关系定义\n");
    }
}

// ==================== 决策网络层 ====================

// 本体事件
class OntologyEvent {
    private String projectId;
    private String objectId;
    private String objectType;
    private String attribute;
    private String oldValue;
    private String newValue;
    private long timestamp;
    
    public OntologyEvent(String projectId, String objectId, String objectType,
                        String attribute, String oldValue, String newValue) {
        this.projectId = projectId;
        this.objectId = objectId;
        this.objectType = objectType;
        this.attribute = attribute;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getProjectId() { return projectId; }
    public String getObjectId() { return objectId; }
    public String getObjectType() { return objectType; }
    public String getAttribute() { return attribute; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public long getTimestamp() { return timestamp; }
}

// 决策规则接口
interface DecisionRule {
    String getRuleId();
    List<String> getTriggerObjectTypes();
    List<String> getTriggerAttributes();
    void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine);
}

// 决策网络引擎
class DecisionNetworkEngine {
    private String projectId;
    private List<DecisionRule> rules;
    private Queue<OntologyEvent> eventQueue;
    private boolean processing = false;
    
    public DecisionNetworkEngine(String projectId) {
        this.projectId = projectId;
        this.rules = new ArrayList<>();
        this.eventQueue = new LinkedList<>();
    }
    
    public void addRule(DecisionRule rule) {
        rules.add(rule);
    }
    
    public void emitEvent(OntologyEvent event) {
        eventQueue.offer(event);
        processEvents();
    }
    
    private void processEvents() {
        if (processing) return; // 防止递归
        
        processing = true;
        try {
            while (!eventQueue.isEmpty()) {
                OntologyEvent event = eventQueue.poll();
                evaluateRules(event);
            }
        } finally {
            processing = false;
        }
    }
    
    private void evaluateRules(OntologyEvent event) {
        for (DecisionRule rule : rules) {
            // 检查规则是否应该被触发
            if (rule.getTriggerObjectTypes().contains(event.getObjectType()) ||
                rule.getTriggerObjectTypes().contains("*")) {
                
                if (rule.getTriggerAttributes().contains(event.getAttribute()) ||
                    rule.getTriggerAttributes().contains("*")) {
                    
                    // 评估规则 (传入引擎引用以便规则可以触发新事件)
                    rule.evaluate(event, null, this);
                }
            }
        }
    }
    
    public void triggerActivity(String activityId, ProjectContext ctx, Map<String, String> params) {
        System.out.println("  🎯 决策网络触发活动: " + activityId);
        // 这里可以扩展为实际执行活动的逻辑
    }
}

// 规则工厂
class RuleFactory {
    
    public static List<DecisionRule> createRulesFromRelationships(
            Map<String, RelationshipDef> relationships) {
        List<DecisionRule> rules = new ArrayList<>();
        
        for (RelationshipDef rel : relationships.values()) {
            DecisionRule rule = createRuleFromRelationship(rel);
            if (rule != null) {
                rules.add(rule);
            }
        }
        
        return rules;
    }
    
    private static DecisionRule createRuleFromRelationship(RelationshipDef rel) {
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
            default:
                return new GenericRelationshipRule(rel);
        }
    }
}

// 依赖关系规则
class DependsOnRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public DependsOnRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_depends_on";
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        return relationshipDef.getSource_types();
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("status", "*");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        if ("status".equals(event.getAttribute())) {
            System.out.println("  📊 [DependsOnRule] 检测到 " + event.getObjectType() + 
                " 状态变化: " + event.getOldValue() + " → " + event.getNewValue());
            
            if ("completed".equals(event.getNewValue())) {
                System.out.println("      ✓ 对象完成,检查依赖此对象的其他对象");
                // 这里可以查找依赖关系并解除阻塞
            } else if ("blocked".equals(event.getNewValue())) {
                System.out.println("      ⚠ 对象被阻塞,传播阻塞状态");
                // 这里可以传播阻塞状态到依赖对象
            }
        }
    }
}

// 实现关系规则
class ImplementsRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public ImplementsRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_implements";
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        return relationshipDef.getTarget_types(); // 需求被批准时触发
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("status");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        if ("status".equals(event.getAttribute()) && "approved".equals(event.getNewValue())) {
            System.out.println("  📊 [ImplementsRule] " + event.getObjectType() + 
                " 已批准,可以开始实现");
            
            if ("requirement".equals(event.getObjectType())) {
                System.out.println("      → 建议创建设计文档");
                // engine.triggerActivity("create_architecture_design", ctx, params);
            }
        }
    }
}

// 测试关系规则
class TestsRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public TestsRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_tests";
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        return Arrays.asList("test_case");
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("status");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        if ("status".equals(event.getAttribute())) {
            String status = event.getNewValue();
            System.out.println("  📊 [TestsRule] 测试用例状态: " + status);
            
            if ("passed".equals(status)) {
                System.out.println("      ✓ 测试通过,更新被测对象的测试状态");
            } else if ("failed".equals(status)) {
                System.out.println("      ✗ 测试失败,可能需要创建缺陷报告");
            }
        }
    }
}

// 修复关系规则
class FixesRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public FixesRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_fixes";
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        return relationshipDef.getSource_types(); // commit, code_module
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("fixes_bug", "*");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        if ("fixes_bug".equals(event.getAttribute())) {
            System.out.println("  📊 [FixesRule] 提交修复了缺陷: " + event.getNewValue());
            System.out.println("      → 建议执行回归测试");
            // engine.triggerActivity("regression_test", ctx, params);
        }
    }
}

// 阻塞关系规则
class BlocksRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public BlocksRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_blocks";
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        return relationshipDef.getSource_types();
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("status");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        if ("status".equals(event.getAttribute())) {
            if ("blocked".equals(event.getNewValue())) {
                System.out.println("  📊 [BlocksRule] 对象被阻塞: " + event.getObjectId());
                System.out.println("      ⚠ 传播阻塞状态到被阻塞的对象");
            } else if ("resolved".equals(event.getNewValue()) || 
                      "completed".equals(event.getNewValue())) {
                System.out.println("  📊 [BlocksRule] 阻塞对象已解决");
                System.out.println("      ✓ 检查被阻塞对象是否可以继续");
            }
        }
    }
}

// 通用关系规则
class GenericRelationshipRule implements DecisionRule {
    private RelationshipDef relationshipDef;
    
    public GenericRelationshipRule(RelationshipDef relationshipDef) {
        this.relationshipDef = relationshipDef;
    }
    
    @Override
    public String getRuleId() {
        return "rule_" + relationshipDef.getId();
    }
    
    @Override
    public List<String> getTriggerObjectTypes() {
        List<String> types = new ArrayList<>();
        if (relationshipDef.getSource_types() != null) {
            types.addAll(relationshipDef.getSource_types());
        }
        if (relationshipDef.getTarget_types() != null) {
            types.addAll(relationshipDef.getTarget_types());
        }
        return types;
    }
    
    @Override
    public List<String> getTriggerAttributes() {
        return Arrays.asList("*");
    }
    
    @Override
    public void evaluate(OntologyEvent event, ProjectContext ctx, DecisionNetworkEngine engine) {
        System.out.println("  📊 [" + relationshipDef.getName() + "Rule] 关系: " + 
            relationshipDef.getId() + " 触发,对象: " + event.getObjectId());
    }
}
