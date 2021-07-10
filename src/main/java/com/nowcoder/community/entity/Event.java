package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka事件
 */
public class Event {
    //事件主题、背景
    private String topic;
    //事件发出者
    private int userId;
    //事件类型
    private int entityTpye;
    //事件ID
    private int entityId;
    //帖子作者
    private int entityUserId;
    //其他所有额外数据
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityTpye() {
        return entityTpye;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setEntityTpye(int entityTpye) {
        this.entityTpye = entityTpye;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    //修改: 返回当前对象,让其返回以进行后续操作
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
