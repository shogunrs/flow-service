package com.docheck.flow.infrastructure.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("system_health_config")
public class SystemHealthConfigDocument {
    @Id
    private String id;
    private List<String> monitoredStatusTags;
    private Instant updatedAt;
}
