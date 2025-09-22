package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AiProvider {
    String id;
    String name;
    String slug;
    String baseUrl;
    String docsUrl;
    String notes;
    boolean enabled;
    Instant createdAt;
    Instant updatedAt;
}