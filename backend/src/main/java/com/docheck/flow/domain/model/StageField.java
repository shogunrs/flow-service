package com.docheck.flow.domain.model;

import java.time.Instant;
import java.util.List;

public class StageField {
    private String id;
    private String stageId; // reference to StageDocument.id
    private String processExternalId; // reference to Process.externalId - HERANÇA DO PROCESSO
    private String label;
    private String type; // text, number, select, file, date, etc
    private boolean required;
    private String placeholder;
    private List<String> options; // for select-like fields
    private Integer order;
    private Instant createdAt;
    private Instant updatedAt;

    public StageField() {}

    public StageField(String id, String stageId, String processExternalId, String label, String type, boolean required, String placeholder,
                      List<String> options, Integer order, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.stageId = stageId;
        this.processExternalId = processExternalId;
        this.label = label;
        this.type = type;
        this.required = required;
        this.placeholder = placeholder;
        this.options = options;
        this.order = order;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStageId() { return stageId; }
    public void setStageId(String stageId) { this.stageId = stageId; }
    public String getProcessExternalId() { return processExternalId; }
    public void setProcessExternalId(String processExternalId) { this.processExternalId = processExternalId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

