package com.docheck.flow.infrastructure.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("system_health_components")
public class MonitoredComponentDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String componentKey;

    private String name;
    private String description;
    private String statusTag;
    private String defaultSeverity;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
