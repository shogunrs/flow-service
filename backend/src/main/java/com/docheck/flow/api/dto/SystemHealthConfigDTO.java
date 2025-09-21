package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthConfigDTO {
    @Builder.Default
    private List<String> monitoredStatusTags = new ArrayList<>();
    private Instant updatedAt;
}
