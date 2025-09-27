package com.docheck.flow.api.dto;

import com.docheck.flow.domain.model.Lead;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LeadDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Lead.LeadOrigin origin;
    private Lead.LeadType type;
    private Instant createdAt;
    private Instant updatedAt;
}
