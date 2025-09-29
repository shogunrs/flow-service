package com.docheck.flow.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Proposal {
    private String id;
    private String processExternalId;
    private String name;
    private Double amount;
    private String stageId;
    private String status;
    private boolean archived;
    private Instant stageEnteredAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Map<String, Object> details;
    // answers per stageId
    private Map<String, Map<String, Object>> forms;

    // Controle de acesso
    private String createdBy;
    private String organizationId;
    private Boolean isPublic;
    private Set<String> visibleToUsers;
    private List<ExternalNotification> externalNotifications;

    public Proposal() {}

    public Proposal(String id, String processExternalId, String name, Double amount, String stageId, String status,
                    boolean archived, Instant stageEnteredAt, Instant createdAt, Instant updatedAt,
                    Map<String, Object> details,
                    Map<String, Map<String, Object>> forms) {
        this.id = id;
        this.processExternalId = processExternalId;
        this.name = name;
        this.amount = amount;
        this.stageId = stageId;
        this.status = status;
        this.archived = archived;
        this.stageEnteredAt = stageEnteredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.details = details;
        this.forms = forms;
        // Valores padrão para controle de acesso
        this.isPublic = true; // Por padrão, público para backward compatibility
        this.visibleToUsers = Set.of();
        this.externalNotifications = List.of();
    }

    // Construtor completo incluindo campos de acesso
    public Proposal(String id, String processExternalId, String name, Double amount, String stageId, String status,
                    boolean archived, Instant stageEnteredAt, Instant createdAt, Instant updatedAt,
                    Map<String, Object> details, Map<String, Map<String, Object>> forms,
                    String createdBy, String organizationId, Boolean isPublic,
                    Set<String> visibleToUsers, List<ExternalNotification> externalNotifications) {
        this.id = id;
        this.processExternalId = processExternalId;
        this.name = name;
        this.amount = amount;
        this.stageId = stageId;
        this.status = status;
        this.archived = archived;
        this.stageEnteredAt = stageEnteredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.details = details;
        this.forms = forms;
        this.createdBy = createdBy;
        this.organizationId = organizationId;
        this.isPublic = isPublic;
        this.visibleToUsers = visibleToUsers;
        this.externalNotifications = externalNotifications;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProcessExternalId() { return processExternalId; }
    public void setProcessExternalId(String processExternalId) { this.processExternalId = processExternalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStageId() { return stageId; }
    public void setStageId(String stageId) { this.stageId = stageId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public Instant getStageEnteredAt() { return stageEnteredAt; }
    public void setStageEnteredAt(Instant stageEnteredAt) { this.stageEnteredAt = stageEnteredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    public Map<String, Map<String, Object>> getForms() { return forms; }
    public void setForms(Map<String, Map<String, Object>> forms) { this.forms = forms; }

    // Getters e setters para controle de acesso
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public boolean isPublic() { return isPublic != null ? isPublic : true; }
    public Set<String> getVisibleToUsers() { return visibleToUsers; }
    public void setVisibleToUsers(Set<String> visibleToUsers) { this.visibleToUsers = visibleToUsers; }
    public List<ExternalNotification> getExternalNotifications() { return externalNotifications; }
    public void setExternalNotifications(List<ExternalNotification> externalNotifications) { this.externalNotifications = externalNotifications; }
}
