package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredComponentRequest {
    private String name;
    private String componentKey;
    private String description;
    private String statusTag;
    private String defaultSeverity;
    private Boolean enabled;
}
