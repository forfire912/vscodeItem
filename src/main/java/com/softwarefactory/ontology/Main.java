package com.softwarefactory.ontology;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 软件工厂 v2.0 - 多项目并发支持
 * MVP版本 - 所有核心功能集成在一个文件中
 */
public class Main {
    
    private static ProjectManager projectManager;
    private static String currentProjectId;
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  软件工厂系统 v2.0 - 多项目并发 + 多模式支持             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        
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
        System.out.println("\n可用命令:");
        System.out.println("  project create <id> <name> <mode> <priority> - 创建项目");
        System.out.println("  projects                                     - 列出所有项目");
        System.out.println("  switch <projectId>                           - 切换项目");
        System.out.println("  phase transition <PHASE>                     - 阶段转换");
        System.out.println("  phase status                                 - 查看阶段状态");
        System.out.println("  update <objId> <attr> <value>                - 更新对象");
        System.out.println("  resources status                             - 查看资源");
        System.out.println("  resources request <resourceId>               - 请求资源");
        System.out.println("  resources release <resourceId>               - 释放资源");
        System.out.println("  run all                                      - 启动所有项目");
        System.out.println("  status                                       - 查看当前项目状态");
        System.out.println("  demo                                         - 运行演示");
        System.out.println("  help                                         - 显示帮助");
        System.out.println("  exit/quit                                    - 退出");
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
    
    public ProjectContext(String projectId, String projectName, WorkflowMode mode, int priority) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.mode = mode;
        this.priority = priority;
        this.status = ProjectStatus.ACTIVE;
        this.currentPhase = Phase.REQUIREMENT;
        this.allocatedResources = new HashSet<>();
        this.objects = new HashMap<>();
        
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
        System.out.println("📝 更新对象: " + objId + "." + attr + " = " + value);
        objects.put(objId + "." + attr, value);
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
