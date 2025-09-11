package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("stage_fields")
public class StageFieldDocument {
    @Id public String id;
    @Indexed public String stageId; // reference to stages._id
    @Indexed public String processExternalId; // reference to processes.externalId - HERANÇA PROCESSO
    public String label;
    public String type;
    public boolean required;
    public String placeholder;
    public List<String> options;
    @Indexed public Integer order;
    public Instant createdAt;
    public Instant updatedAt;
}

