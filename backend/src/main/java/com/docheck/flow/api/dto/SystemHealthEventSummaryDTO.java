package com.docheck.flow.api.dto;

import com.docheck.flow.domain.model.SystemHealthEvent;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class SystemHealthEventSummaryDTO {
    Map<String, Long> bySeverity;
    long unacknowledgedCount;
    Map<String, SystemHealthEvent> latestPerComponent;
}
