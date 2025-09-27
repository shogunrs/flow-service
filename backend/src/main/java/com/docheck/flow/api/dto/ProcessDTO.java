package com.docheck.flow.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProcessDTO(
        @NotBlank String key,
        @NotBlank String name,
        boolean active,
        String type,
        boolean isFinanceiro
) {}
