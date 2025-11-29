package com.softwarefactory.ontology;

import com.softwarefactory.ontology.config.ConfigLoader;
import com.softwarefactory.ontology.engine.DynamicNetworkEngine;
import com.softwarefactory.ontology.model.Action;
import com.softwarefactory.ontology.model.OntologyObject;
import com.softwarefactory.ontology.engine.DynamicRule;
import com.softwarefactory.ontology.semantic.SemanticLifter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 主程序入口 - 命令行驱动的动力学决策网络
 */
public class Main {
    private static DynamicNetworkEngine engine;
    private static SemanticLifter semanticLifter;
    private static ConfigLoader configLoader;
    private static boolean running = true;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   基于动态本体映射与动力学决策网络的软件工厂实现系统      ║");
        System.out.println("║   Ontology Dynamic Network System v1.0                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // 初始化系统
        initialize();

        // 加载默认配置
        loadDefaultConfigs();

        // 显示帮助信息
        printHelp();

        // 启动命令行循环
        runCommandLoop();
    }

    /**
     * 初始化系统
     */
    private static void initialize() {
        engine = new DynamicNetworkEngine();
        configLoader = new ConfigLoader();
        semanticLifter = new SemanticLifter(engine.getStateMonitor());
        
        System.out.println("系统初始化完成\n");
    }

    /**
     * 加载默认配置
     */
    private static void loadDefaultConfigs() {
        try {
            // 尝试加载配置文件
            loadConfigIfExists("config/ontology.yml");
            loadConfigIfExists("config/actions.yml");
            loadConfigIfExists("config/rules.yml");
        } catch (Exception e) {
            System.out.println("注意: 未找到配置文件,可使用命令手动加载\n");
        }
    }

    /**
     * 如果文件存在则加载配置
     */
    private static void loadConfigIfExists(String filePath) {
        try {
            if (filePath.contains("ontology")) {
                List<OntologyObject> objects = configLoader.loadOntologyObjects(filePath);
                for (OntologyObject obj : objects) {
                    engine.getStateMonitor().registerObject(obj);
                }
            } else if (filePath.contains("actions")) {
                List<Action> actions = configLoader.loadActions(filePath);
                for (Action action : actions) {
                    engine.registerAction(action);
                }
            } else if (filePath.contains("rules")) {
                List<DynamicRule> rules = configLoader.loadRules(filePath);
                for (DynamicRule rule : rules) {
                    engine.registerRule(rule);
                }
            }
        } catch (Exception e) {
            // 静默失败,配置文件可选
        }
    }

    /**
     * 命令行循环
     */
    private static void runCommandLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        while (running) {
            try {
                System.out.print("ontology> ");
                String input = reader.readLine();
                
                if (input == null || input.trim().isEmpty()) {
                    continue;
                }
                
                processCommand(input.trim());
                
            } catch (Exception e) {
                System.out.println("错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理命令
     */
    private static void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "start":
                engine.start();
                break;
                
            case "stop":
                engine.stop();
                break;
                
            case "load":
                handleLoadCommand(args);
                break;
                
            case "ingest":
                handleIngestCommand(args);
                break;
                
            case "update":
                handleUpdateCommand(args);
                break;
                
            case "status":
                engine.displayStatus();
                break;
                
            case "objects":
                engine.displayObjects();
                break;
                
            case "rules":
                engine.displayRules();
                break;
                
            case "help":
                printHelp();
                break;
                
            case "demo":
                runDemo();
                break;
                
            case "exit":
            case "quit":
                running = false;
                System.out.println("再见!");
                break;
                
            default:
                System.out.println("未知命令: " + command + ". 输入 'help' 查看帮助");
        }
    }

    /**
     * 处理加载命令
     */
    private static void handleLoadCommand(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("用法: load <type> <file>");
            System.out.println("类型: ontology, actions, rules");
            return;
        }
        
        String type = parts[0];
        String filePath = parts[1];
        
        try {
            switch (type) {
                case "ontology":
                    List<OntologyObject> objects = configLoader.loadOntologyObjects(filePath);
                    for (OntologyObject obj : objects) {
                        engine.getStateMonitor().registerObject(obj);
                    }
                    break;
                    
                case "actions":
                    List<Action> actions = configLoader.loadActions(filePath);
                    for (Action action : actions) {
                        engine.registerAction(action);
                    }
                    break;
                    
                case "rules":
                    List<DynamicRule> rules = configLoader.loadRules(filePath);
                    for (DynamicRule rule : rules) {
                        engine.registerRule(rule);
                    }
                    break;
                    
                default:
                    System.out.println("未知配置类型: " + type);
            }
        } catch (Exception e) {
            System.out.println("加载失败: " + e.getMessage());
        }
    }

    /**
     * 处理摄入命令
     */
    private static void handleIngestCommand(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 1) {
            System.out.println("用法: ingest <type> [data]");
            System.out.println("类型: git_commit, requirement, defect");
            return;
        }
        
        String type = parts[0];
        
        // 创建示例数据用于演示
        Map<String, Object> data = new HashMap<>();
        
        switch (type) {
            case "git_commit":
                data.put("commit_id", UUID.randomUUID().toString());
                data.put("message", "Fixes #1024: 修复安全漏洞");
                data.put("author", "developer");
                data.put("files", Arrays.asList("src/main.java", "src/security.java"));
                break;
            case "requirement":
                data.put("id", "2048");
                data.put("title", "新增用户认证功能");
                data.put("security_level", "A");
                break;
            case "defect":
                data.put("id", "1024");
                data.put("title", "登录页面XSS漏洞");
                data.put("severity", "high");
                break;
            default:
                System.out.println("未知数据类型: " + type);
                return;
        }
        
        semanticLifter.ingestData(type, data);
    }

    /**
     * 处理更新命令
     */
    private static void handleUpdateCommand(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 3) {
            System.out.println("用法: update <object_id> <attribute> <value>");
            return;
        }
        
        String objectId = parts[0];
        String attribute = parts[1];
        String value = parts[2];
        
        engine.getStateMonitor().updateObjectAttribute(objectId, attribute, value);
    }

    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("可用命令:");
        System.out.println("  start                           - 启动动力学决策网络引擎");
        System.out.println("  stop                            - 停止引擎");
        System.out.println("  load <type> <file>              - 加载配置文件");
        System.out.println("    类型: ontology, actions, rules");
        System.out.println("  ingest <type>                   - 摄入外部数据并语义提升");
        System.out.println("    类型: git_commit, requirement, defect");
        System.out.println("  update <obj_id> <attr> <value>  - 更新对象属性(触发规则)");
        System.out.println("  status                          - 显示引擎状态");
        System.out.println("  objects                         - 显示所有本体对象");
        System.out.println("  rules                           - 显示所有规则");
        System.out.println("  demo                            - 运行演示场景");
        System.out.println("  help                            - 显示此帮助信息");
        System.out.println("  exit/quit                       - 退出系统");
        System.out.println();
    }

    /**
     * 运行演示场景
     */
    private static void runDemo() {
        System.out.println("\n========================================");
        System.out.println("运行演示场景");
        System.out.println("========================================\n");
        
        // 确保引擎已启动
        if (!engine.getStateMonitor().getAllObjects().isEmpty() && 
            !engine.getRules().isEmpty()) {
            
            System.out.println("场景: 需求安全等级提升触发自动化流程\n");
            
            // 查找需求对象
            OntologyObject reqObj = null;
            for (OntologyObject obj : engine.getStateMonitor().getAllObjects()) {
                if ("requirement".equals(obj.getType())) {
                    reqObj = obj;
                    break;
                }
            }
            
            if (reqObj != null) {
                System.out.println("1. 更新需求安全等级: " + reqObj.getId());
                engine.getStateMonitor().updateObjectAttribute(
                    reqObj.getId(), "security_level", "A"
                );
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("\n2. 演示完成,查看上述输出了解规则触发情况");
            } else {
                System.out.println("未找到需求对象,请先加载配置或摄入数据");
            }
        } else {
            System.out.println("请先启动引擎并加载配置:");
            System.out.println("  1. start");
            System.out.println("  2. load ontology config/ontology.yml");
            System.out.println("  3. load actions config/actions.yml");
            System.out.println("  4. load rules config/rules.yml");
            System.out.println("  5. demo");
        }
        
        System.out.println();
    }
}
