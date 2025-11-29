package com.softwarefactory.ontology.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 动作 - 可在本体对象上执行的操作
 */
public class Action {
    private String name;
    private String description;
    private String targetObjectType;
    private Map<String, String> parameters;
    private ActionExecutor executor;

    public Action() {
        this.parameters = new HashMap<>();
    }

    public Action(String name, String description, String targetObjectType) {
        this();
        this.name = name;
        this.description = description;
        this.targetObjectType = targetObjectType;
    }

    public void addParameter(String paramName, String paramType) {
        this.parameters.put(paramName, paramType);
    }

    public void setExecutor(ActionExecutor executor) {
        this.executor = executor;
    }

    public void execute(OntologyObject target, Map<String, Object> params) {
        if (executor != null) {
            executor.execute(target, params);
        } else {
            System.out.println("执行动作: " + name + " 在对象: " + target.getId());
        }
    }

    // Getters and Setters
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

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public ActionExecutor getExecutor() {
        return executor;
    }

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", targetObjectType='" + targetObjectType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * 动作执行器接口
     */
    @FunctionalInterface
    public interface ActionExecutor {
        void execute(OntologyObject target, Map<String, Object> params);
    }
}
