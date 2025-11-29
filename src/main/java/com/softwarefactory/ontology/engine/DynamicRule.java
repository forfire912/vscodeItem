package com.softwarefactory.ontology.engine;

import com.softwarefactory.ontology.model.Action;
import com.softwarefactory.ontology.model.OntologyObject;

import java.util.*;
import java.util.function.Predicate;

/**
 * 动力学规则 - 定义"若状态A发生,则执行动作B"
 */
public class DynamicRule {
    private String ruleId;
    private String name;
    private String description;
    private int priority;
    private Predicate<StateChangeEvent> condition;
    private List<RuleAction> actions;
    private boolean enabled;

    public DynamicRule(String ruleId, String name) {
        this.ruleId = ruleId;
        this.name = name;
        this.actions = new ArrayList<>();
        this.enabled = true;
        this.priority = 0;
    }

    /**
     * 设置条件
     */
    public void setCondition(Predicate<StateChangeEvent> condition) {
        this.condition = condition;
    }

    /**
     * 添加要执行的动作
     */
    public void addAction(String actionName, String targetObjectId, Map<String, Object> params) {
        actions.add(new RuleAction(actionName, targetObjectId, params));
    }

    /**
     * 评估规则是否匹配事件
     */
    public boolean evaluate(StateChangeEvent event) {
        return enabled && condition != null && condition.test(event);
    }

    /**
     * 执行规则定义的所有动作
     */
    public void execute(StateChangeEvent event, StateMonitor monitor, Map<String, Action> actionRegistry) {
        System.out.println("触发规则: " + name);
        
        for (RuleAction ruleAction : actions) {
            String targetId = ruleAction.getTargetObjectId();
            
            // 支持动态解析目标对象
            if ("$source".equals(targetId)) {
                targetId = event.getObject().getId();
            }
            
            OntologyObject target = monitor.getObject(targetId);
            if (target != null) {
                Action action = actionRegistry.get(ruleAction.getActionName());
                if (action != null) {
                    System.out.println("  执行动作: " + action.getName() + " -> " + target.getId());
                    action.execute(target, ruleAction.getParams());
                } else {
                    System.out.println("  警告: 未找到动作 " + ruleAction.getActionName());
                }
            } else {
                System.out.println("  警告: 未找到目标对象 " + targetId);
            }
        }
    }

    // Getters and Setters
    public String getRuleId() {
        return ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RuleAction> getActions() {
        return actions;
    }

    /**
     * 规则动作 - 规则触发时要执行的动作
     */
    public static class RuleAction {
        private String actionName;
        private String targetObjectId;
        private Map<String, Object> params;

        public RuleAction(String actionName, String targetObjectId, Map<String, Object> params) {
            this.actionName = actionName;
            this.targetObjectId = targetObjectId;
            this.params = params != null ? params : new HashMap<>();
        }

        public String getActionName() {
            return actionName;
        }

        public String getTargetObjectId() {
            return targetObjectId;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }
}
