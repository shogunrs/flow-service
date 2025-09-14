package com.docheck.flow.api.dto;

import java.time.Instant;

public record StatusDTO(
        String id,
        String name,
        String color,
        String category,
        Instant createdAt,
        Instant updatedAt
) {}