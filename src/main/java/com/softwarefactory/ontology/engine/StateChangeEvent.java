package com.softwarefactory.ontology.engine;

import com.softwarefactory.ontology.model.OntologyObject;

import java.util.*;

/**
 * 状态变更事件
 */
public class StateChangeEvent {
    private String eventId;
    private OntologyObject object;
    private String attributeName;
    private Object oldValue;
    private Object newValue;
    private Date timestamp;

    public StateChangeEvent(OntologyObject object, String attributeName, Object oldValue, Object newValue) {
        this.eventId = UUID.randomUUID().toString();
        this.object = object;
        this.attributeName = attributeName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = new Date();
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public OntologyObject getObject() {
        return object;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "objectId='" + object.getId() + '\'' +
                ", attribute='" + attributeName + '\'' +
                ", oldValue=" + oldValue +
                ", newValue=" + newValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
