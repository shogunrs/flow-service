package com.docheck.flow.infrastructure.mongo;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@Document(collection = "ai_providers")
public class AiProviderDocument {
    @Id
    String id;

    @Indexed(unique = true)
    String name;

    @Indexed(unique = true)
    String slug;

    String baseUrl;
    String docsUrl;
    String notes;
    boolean enabled;

    @Indexed
    Instant createdAt;
    Instant updatedAt;
}