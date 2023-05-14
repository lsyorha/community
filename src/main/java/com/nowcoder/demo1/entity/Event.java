package com.nowcoder.demo1.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
//    话题名称
    private String topic;
    private int userId;
//    话题的实体类型
    private int entityType;
//    话题的实体id
    private int entityId;
//    话题的实体作者
    private int entityUserId;
//    额外数据存入map
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }
//可使用无参、有参构造实现，但在set时返回可以在set时方便注值
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }
}
