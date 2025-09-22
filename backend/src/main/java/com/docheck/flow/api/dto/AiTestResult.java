package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AiTestResult {
    private boolean success;
    private String message;
    private long latencyMs;
    private Instant testedAt;
    private String providerId;
    private String credentialId;
}