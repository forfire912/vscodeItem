package com.softwarefactory.ontology.engine;

import com.softwarefactory.ontology.model.OntologyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 状态监听器 - 监听本体对象的状态变化
 */
public class StateMonitor {
    private Map<String, OntologyObject> objects;
    private List<StateChangeListener> listeners;

    public StateMonitor() {
        this.objects = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * 注册对象
     */
    public void registerObject(OntologyObject object) {
        objects.put(object.getId(), object);
        System.out.println("注册对象: " + object.getId() + " [" + object.getType() + "]");
    }

    /**
     * 获取对象
     */
    public OntologyObject getObject(String objectId) {
        return objects.get(objectId);
    }

    /**
     * 获取所有对象
     */
    public List<OntologyObject> getAllObjects() {
        return new ArrayList<>(objects.values());
    }

    /**
     * 更新对象属性并触发事件
     */
    public void updateObjectAttribute(String objectId, String attributeName, Object newValue) {
        OntologyObject object = objects.get(objectId);
        if (object != null) {
            Object oldValue = object.getAttribute(attributeName);
            object.setAttribute(attributeName, newValue);
            
            // 触发状态变更事件
            StateChangeEvent event = new StateChangeEvent(object, attributeName, oldValue, newValue);
            notifyListeners(event);
            
            System.out.println("状态变更: " + object.getId() + "." + attributeName + 
                             " 从 [" + oldValue + "] 变为 [" + newValue + "]");
        }
    }

    /**
     * 添加监听器
     */
    public void addListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知所有监听器
     */
    private void notifyListeners(StateChangeEvent event) {
        for (StateChangeListener listener : listeners) {
            listener.onStateChange(event);
        }
    }

    /**
     * 状态变更监听器接口
     */
    @FunctionalInterface
    public interface StateChangeListener {
        void onStateChange(StateChangeEvent event);
    }
}
