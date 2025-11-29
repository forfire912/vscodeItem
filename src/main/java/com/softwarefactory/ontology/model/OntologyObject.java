package com.softwarefactory.ontology.model;

import java.util.*;

/**
 * 本体对象 - 研发要素的统一抽象
 * 可表示需求、代码、测试台等任何研发实体
 */
public class OntologyObject {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> attributes;
    private List<String> availableActions;
    private Map<String, Link> links;
    private Date createdAt;
    private Date updatedAt;

    public OntologyObject() {
        this.attributes = new HashMap<>();
        this.availableActions = new ArrayList<>();
        this.links = new HashMap<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public OntologyObject(String id, String type, String name) {
        this();
        this.id = id;
        this.type = type;
        this.name = name;
    }

    // 添加属性
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
        this.updatedAt = new Date();
    }

    // 获取属性
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    // 添加可执行动作
    public void addAction(String actionName) {
        if (!this.availableActions.contains(actionName)) {
            this.availableActions.add(actionName);
        }
    }

    // 添加链接
    public void addLink(Link link) {
        this.links.put(link.getId(), link);
        this.updatedAt = new Date();
    }

    // 移除链接
    public void removeLink(String linkId) {
        this.links.remove(linkId);
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<String> availableActions) {
        this.availableActions = availableActions;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "OntologyObject{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
