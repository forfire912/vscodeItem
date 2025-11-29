package com.softwarefactory.ontology.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 链接 - 表示本体对象之间的关系
 */
public class Link {
    private String id;
    private String type;
    private String sourceObjectId;
    private String targetObjectId;
    private Map<String, Object> properties;
    private Date createdAt;

    public Link() {
        this.properties = new HashMap<>();
        this.createdAt = new Date();
    }

    public Link(String id, String type, String sourceObjectId, String targetObjectId) {
        this();
        this.id = id;
        this.type = type;
        this.sourceObjectId = sourceObjectId;
        this.targetObjectId = targetObjectId;
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
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

    public String getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(String sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public String getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Link{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", source='" + sourceObjectId + '\'' +
                ", target='" + targetObjectId + '\'' +
                '}';
    }
}
