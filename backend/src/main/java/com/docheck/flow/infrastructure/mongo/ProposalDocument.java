package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("proposals")
public class ProposalDocument {
    @Id public String id;
    @Indexed public String processExternalId;
    @Indexed public String stageId;
    public String name;
    public Double amount;
    public String status;
    public boolean archived;
    public Instant stageEnteredAt;
    public Instant createdAt;
    public Instant updatedAt;
    public Map<String, Object> details;
    public Map<String, Map<String, Object>> forms;
}
