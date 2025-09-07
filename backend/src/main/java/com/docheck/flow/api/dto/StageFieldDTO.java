package com.docheck.flow.api.dto;

import java.util.List;

public record StageFieldDTO(
        String id,
        String stageId,
        String label,
        String type,
        Boolean required,
        String placeholder,
        List<String> options,
        Integer order
) {}

