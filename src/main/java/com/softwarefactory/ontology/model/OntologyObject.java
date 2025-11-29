package com.softwarefactory.ontology.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 本体对象 - 系统的基础数据单元
 */
public class OntologyObject {
    private String id;
    private String type;
    private Map<String, Object> attributes;
    
    public OntologyObject() {
        this.attributes = new HashMap<>();
    }
    
    public OntologyObject(String id, String type) {
        this.id = id;
        this.type = type;
        this.attributes = new HashMap<>();
    }
    
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
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    @Override
    public String toString() {
        return "OntologyObject{id='" + id + "', type='" + type + "', attributes=" + attributes + "}";
    }
}
