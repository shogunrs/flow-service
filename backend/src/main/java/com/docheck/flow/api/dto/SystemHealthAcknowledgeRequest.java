package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthAcknowledgeRequest {
    private String reviewerId;
    private String reviewerName;
    private String notes;
}
