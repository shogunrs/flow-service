package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class SystemHealthConfig {
    private String id;
    @Builder.Default
    private List<String> monitoredStatusTags = new ArrayList<>();
    private Instant updatedAt;
}
