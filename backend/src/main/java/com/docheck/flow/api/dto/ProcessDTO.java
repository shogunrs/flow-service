package com.docheck.flow.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProcessDTO(
        @NotBlank String key,
        @NotBlank String name,
        boolean active,
        String type,
        boolean isFinanceiro,
        List<String> allowedUserIds
) {}
