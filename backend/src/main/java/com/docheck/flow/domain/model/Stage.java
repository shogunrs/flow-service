package com.docheck.flow.domain.model;

import java.time.Instant;

public class Stage {
    private String id;
    private String processKey;
    private String title;
    private Integer slaDays;
    private String color;
    private Integer order;
    private Instant createdAt;
    private Instant updatedAt;

    public Stage() {}

    public Stage(String id, String processKey, String title, Integer slaDays, String color, Integer order, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.processKey = processKey;
        this.title = title;
        this.slaDays = slaDays;
        this.color = color;
        this.order = order;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

