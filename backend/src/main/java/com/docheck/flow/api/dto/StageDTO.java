package com.docheck.flow.api.dto;

public record StageDTO(
        String id,
        String title,
        Integer slaDays,
        String color,
        Integer order
) {}

