package com.docheck.flow.infrastructure.outbox;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("outbox")
public class OutboxDocument {
    @Id public String id;
    @Indexed public String type;
    public Map<String, Object> payload;
    public Instant createdAt;
    public Instant publishedAt;
}

