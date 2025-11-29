package com.softwarefactory.ontology.model;

/**
 * 动作定义
 */
public class Action {
    private String name;
    private String description;
    
    public Action() {}
    
    public Action(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public void execute() {
        System.out.println("  → 执行动作: " + name);
    }
    
    @Override
    public String toString() {
        return "Action{name='" + name + "'}";
    }
}
