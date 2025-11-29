package com.softwarefactory.ontology.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwarefactory.ontology.model.Action;
import com.softwarefactory.ontology.model.OntologyObject;
import com.softwarefactory.ontology.engine.DynamicRule;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 配置加载器 - 从配置文件加载本体、动作和规则
 */
public class ConfigLoader {
    private ObjectMapper jsonMapper;
    private Yaml yamlMapper;

    public ConfigLoader() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new Yaml();
    }

    /**
     * 加载本体对象配置
     */
    public List<OntologyObject> loadOntologyObjects(String filePath) throws IOException {
        List<OntologyObject> objects = new ArrayList<>();
        
        Map<String, Object> config = loadConfig(filePath);
        
        if (config.containsKey("ontology_objects")) {
            List<Map<String, Object>> objectConfigs = (List<Map<String, Object>>) config.get("ontology_objects");
            
            for (Map<String, Object> objConfig : objectConfigs) {
                OntologyObject obj = parseOntologyObject(objConfig);
                objects.add(obj);
            }
        }
        
        System.out.println("加载了 " + objects.size() + " 个本体对象");
        return objects;
    }

    /**
     * 加载动作配置
     */
    public List<Action> loadActions(String filePath) throws IOException {
        List<Action> actions = new ArrayList<>();
        
        Map<String, Object> config = loadConfig(filePath);
        
        if (config.containsKey("actions")) {
            List<Map<String, Object>> actionConfigs = (List<Map<String, Object>>) config.get("actions");
            
            for (Map<String, Object> actionConfig : actionConfigs) {
                Action action = parseAction(actionConfig);
                actions.add(action);
            }
        }
        
        System.out.println("加载了 " + actions.size() + " 个动作定义");
        return actions;
    }

    /**
     * 加载规则配置
     */
    public List<DynamicRule> loadRules(String filePath) throws IOException {
        List<DynamicRule> rules = new ArrayList<>();
        
        Map<String, Object> config = loadConfig(filePath);
        
        if (config.containsKey("rules")) {
            List<Map<String, Object>> ruleConfigs = (List<Map<String, Object>>) config.get("rules");
            
            for (Map<String, Object> ruleConfig : ruleConfigs) {
                DynamicRule rule = parseRule(ruleConfig);
                rules.add(rule);
            }
        }
        
        System.out.println("加载了 " + rules.size() + " 个规则");
        return rules;
    }

    /**
     * 加载配置文件(支持JSON和YAML)
     */
    private Map<String, Object> loadConfig(String filePath) throws IOException {
        try (InputStream input = new FileInputStream(filePath)) {
            if (filePath.endsWith(".json")) {
                return jsonMapper.readValue(input, Map.class);
            } else if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) {
                return yamlMapper.load(input);
            } else {
                throw new IllegalArgumentException("不支持的配置文件格式: " + filePath);
            }
        }
    }

    /**
     * 解析本体对象配置
     */
    private OntologyObject parseOntologyObject(Map<String, Object> config) {
        String id = (String) config.get("id");
        String type = (String) config.get("type");
        String name = (String) config.get("name");
        
        OntologyObject obj = new OntologyObject(id, type, name);
        
        // 解析属性
        if (config.containsKey("attributes")) {
            Map<String, Object> attributes = (Map<String, Object>) config.get("attributes");
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                obj.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        
        // 解析可用动作
        if (config.containsKey("available_actions")) {
            List<String> actions = (List<String>) config.get("available_actions");
            for (String action : actions) {
                obj.addAction(action);
            }
        }
        
        return obj;
    }

    /**
     * 解析动作配置
     */
    private Action parseAction(Map<String, Object> config) {
        String name = (String) config.get("name");
        String description = (String) config.getOrDefault("description", "");
        String targetType = (String) config.getOrDefault("target_object_type", "*");
        
        Action action = new Action(name, description, targetType);
        
        // 解析参数
        if (config.containsKey("parameters")) {
            Map<String, String> params = (Map<String, String>) config.get("parameters");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                action.addParameter(entry.getKey(), entry.getValue());
            }
        }
        
        // 设置执行器(使用默认实现)
        action.setExecutor(createDefaultExecutor(name));
        
        return action;
    }

    /**
     * 解析规则配置
     */
    private DynamicRule parseRule(Map<String, Object> config) {
        String ruleId = (String) config.get("id");
        String name = (String) config.get("name");
        String description = (String) config.getOrDefault("description", "");
        int priority = ((Number) config.getOrDefault("priority", 0)).intValue();
        
        DynamicRule rule = new DynamicRule(ruleId, name);
        rule.setDescription(description);
        rule.setPriority(priority);
        
        // 解析条件
        if (config.containsKey("condition")) {
            Map<String, Object> condition = (Map<String, Object>) config.get("condition");
            rule.setCondition(createCondition(condition));
        }
        
        // 解析动作
        if (config.containsKey("actions")) {
            List<Map<String, Object>> actions = (List<Map<String, Object>>) config.get("actions");
            for (Map<String, Object> actionConfig : actions) {
                String actionName = (String) actionConfig.get("action");
                String targetId = (String) actionConfig.get("target");
                Map<String, Object> params = (Map<String, Object>) actionConfig.getOrDefault("params", new HashMap<>());
                
                rule.addAction(actionName, targetId, params);
            }
        }
        
        return rule;
    }

    /**
     * 创建条件谓词
     */
    private java.util.function.Predicate<com.softwarefactory.ontology.engine.StateChangeEvent> createCondition(
            Map<String, Object> conditionConfig) {
        
        String objectType = (String) conditionConfig.get("object_type");
        String attributeName = (String) conditionConfig.get("attribute");
        Object expectedValue = conditionConfig.get("value");
        String operator = (String) conditionConfig.getOrDefault("operator", "equals");
        
        return event -> {
            // 检查对象类型
            if (objectType != null && !objectType.equals(event.getObject().getType())) {
                return false;
            }
            
            // 检查属性名
            if (attributeName != null && !attributeName.equals(event.getAttributeName())) {
                return false;
            }
            
            // 检查值
            if (expectedValue != null) {
                Object newValue = event.getNewValue();
                switch (operator) {
                    case "equals":
                        return expectedValue.equals(newValue);
                    case "not_equals":
                        return !expectedValue.equals(newValue);
                    case "greater_than":
                        return compareValues(newValue, expectedValue) > 0;
                    case "less_than":
                        return compareValues(newValue, expectedValue) < 0;
                    default:
                        return expectedValue.equals(newValue);
                }
            }
            
            return true;
        };
    }

    /**
     * 比较值(用于数值比较)
     */
    private int compareValues(Object v1, Object v2) {
        if (v1 instanceof Number && v2 instanceof Number) {
            double d1 = ((Number) v1).doubleValue();
            double d2 = ((Number) v2).doubleValue();
            return Double.compare(d1, d2);
        }
        return 0;
    }

    /**
     * 创建默认动作执行器
     */
    private Action.ActionExecutor createDefaultExecutor(String actionName) {
        return (target, params) -> {
            System.out.println("    [动作执行] " + actionName + " 在对象 " + target.getId());
            System.out.println("    参数: " + params);
            
            // 根据动作名称执行不同的逻辑
            switch (actionName) {
                case "启用强制审查":
                    target.setAttribute("review_required", true);
                    target.setAttribute("review_level", "strict");
                    System.out.println("    -> 已启用强制审查模式");
                    break;
                case "预约测试台":
                    String testPlatform = (String) params.getOrDefault("platform", "default");
                    target.setAttribute("reserved_platform", testPlatform);
                    System.out.println("    -> 已预约测试台: " + testPlatform);
                    break;
                case "发送通知":
                    String message = (String) params.getOrDefault("message", "状态已更新");
                    System.out.println("    -> 通知: " + message);
                    break;
                case "上电":
                    target.setAttribute("power_status", "on");
                    System.out.println("    -> 设备已上电");
                    break;
                case "加载固件":
                    String firmware = (String) params.getOrDefault("firmware_version", "latest");
                    target.setAttribute("firmware", firmware);
                    System.out.println("    -> 已加载固件: " + firmware);
                    break;
                case "代码走查":
                    target.setAttribute("code_review_status", "in_progress");
                    System.out.println("    -> 代码走查已启动");
                    break;
                case "静态分析":
                    target.setAttribute("static_analysis_status", "running");
                    System.out.println("    -> 静态分析已启动");
                    break;
                default:
                    System.out.println("    -> 执行完成");
            }
        };
    }
}
