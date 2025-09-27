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
@Document("leads")
public class LeadDocument {
    @Id
    private String id;

    @Indexed
    private String organizationId;
    private String createdBy;

    private String name;

    @Indexed
    private String email;

    private String phone;
    private String origin;
    private String type;

    private Instant createdAt;
    private Instant updatedAt;
}
