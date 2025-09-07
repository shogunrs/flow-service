package com.docheck.flow.api.dto;

import java.time.Instant;
import java.util.Map;

public record ProposalDTO(
        String id,
        String name,
        Double amount,
        String stageId,
        String status,
        Boolean archived,
        Instant stageEnteredAt,
        Instant createdAt,
        Instant updatedAt,
        Map<String, Object> details
) {}

