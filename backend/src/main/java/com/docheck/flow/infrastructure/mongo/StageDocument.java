package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("stages")
public class StageDocument {
    @Id public String id;
    @Indexed public String processKey;
    public String title;
    public Integer slaDays;
    public String color;
    @Indexed public Integer order;
    public Instant createdAt;
    public Instant updatedAt;
}

