package com.softwarefactory.ontology.engine;

import com.softwarefactory.ontology.model.Action;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动力学决策网络引擎 - 核心编排引擎
 */
public class DynamicNetworkEngine {
    private StateMonitor stateMonitor;
    private Map<String, DynamicRule> rules;
    private Map<String, Action> actionRegistry;
    private boolean running;

    public DynamicNetworkEngine() {
        this.stateMonitor = new StateMonitor();
        this.rules = new ConcurrentHashMap<>();
        this.actionRegistry = new ConcurrentHashMap<>();
        this.running = false;
    }

    /**
     * 启动引擎
     */
    public void start() {
        if (!running) {
            running = true;
            
            // 注册状态监听器,接收状态变更事件并评估规则
            stateMonitor.addListener(event -> {
                evaluateRules(event);
            });
            
            System.out.println("========================================");
            System.out.println("动力学决策网络引擎已启动");
            System.out.println("========================================");
        }
    }

    /**
     * 停止引擎
     */
    public void stop() {
        running = false;
        System.out.println("动力学决策网络引擎已停止");
    }

    /**
     * 注册规则
     */
    public void registerRule(DynamicRule rule) {
        rules.put(rule.getRuleId(), rule);
        System.out.println("注册规则: " + rule.getName() + " [优先级: " + rule.getPriority() + "]");
    }

    /**
     * 注册动作
     */
    public void registerAction(Action action) {
        actionRegistry.put(action.getName(), action);
        System.out.println("注册动作: " + action.getName() + " [" + action.getTargetObjectType() + "]");
    }

    /**
     * 评估所有规则
     */
    private void evaluateRules(StateChangeEvent event) {
        if (!running) {
            return;
        }

        System.out.println("\n>>> 评估规则触发条件 <<<");
        
        // 按优先级排序规则
        List<DynamicRule> sortedRules = new ArrayList<>(rules.values());
        sortedRules.sort(Comparator.comparingInt(DynamicRule::getPriority).reversed());
        
        for (DynamicRule rule : sortedRules) {
            if (rule.evaluate(event)) {
                rule.execute(event, stateMonitor, actionRegistry);
            }
        }
        
        System.out.println();
    }

    /**
     * 获取状态监视器
     */
    public StateMonitor getStateMonitor() {
        return stateMonitor;
    }

    /**
     * 获取所有规则
     */
    public Map<String, DynamicRule> getRules() {
        return rules;
    }

    /**
     * 获取动作注册表
     */
    public Map<String, Action> getActionRegistry() {
        return actionRegistry;
    }

    /**
     * 显示引擎状态
     */
    public void displayStatus() {
        System.out.println("\n========================================");
        System.out.println("引擎状态");
        System.out.println("========================================");
        System.out.println("运行状态: " + (running ? "运行中" : "已停止"));
        System.out.println("已注册对象: " + stateMonitor.getAllObjects().size());
        System.out.println("已注册规则: " + rules.size());
        System.out.println("已注册动作: " + actionRegistry.size());
        System.out.println("========================================\n");
    }

    /**
     * 显示所有对象
     */
    public void displayObjects() {
        System.out.println("\n========================================");
        System.out.println("本体对象列表");
        System.out.println("========================================");
        for (var obj : stateMonitor.getAllObjects()) {
            System.out.println(obj.getId() + " [" + obj.getType() + "] - " + obj.getName());
            System.out.println("  属性: " + obj.getAttributes());
        }
        System.out.println("========================================\n");
    }

    /**
     * 显示所有规则
     */
    public void displayRules() {
        System.out.println("\n========================================");
        System.out.println("规则列表");
        System.out.println("========================================");
        for (var rule : rules.values()) {
            System.out.println(rule.getRuleId() + " - " + rule.getName());
            System.out.println("  描述: " + rule.getDescription());
            System.out.println("  优先级: " + rule.getPriority());
            System.out.println("  状态: " + (rule.isEnabled() ? "启用" : "禁用"));
        }
        System.out.println("========================================\n");
    }
}
