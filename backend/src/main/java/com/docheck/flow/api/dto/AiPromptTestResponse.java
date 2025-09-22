package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptTestResponse {
    private boolean success;
    private String message;
    private String response;
    private int tokens;
    private double cost;
    private int latencyMs;
    private String providerId;
    private String modelName;
    private Instant testedAt;
    private String errorDetails;
}