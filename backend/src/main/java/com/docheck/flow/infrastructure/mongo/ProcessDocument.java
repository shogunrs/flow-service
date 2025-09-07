package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("processes")
public class ProcessDocument {
    @Id
    public String id;
    @Indexed(unique = true)
    public String externalId;
    public String name;
    public boolean active;
    public Instant createdAt;
    public Instant updatedAt;
}
