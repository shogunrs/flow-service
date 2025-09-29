package com.docheck.flow.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Map<String, Object> details,
        // Campos de controle de acesso
        String createdBy,
        String organizationId,
        Boolean isPublic,
        Set<String> visibleToUsers,
        List<ExternalNotificationDTO> externalNotifications
) {}

