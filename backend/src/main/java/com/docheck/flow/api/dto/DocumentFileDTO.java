package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DocumentFileDTO {
    String fileType;
    String filename;
    String status;
    Instant uploadedAt;
    long fileSize;
    String contentType;
    String reviewerId;
    String reviewerName;
    Instant reviewedAt;
    String reviewNotes;
    String downloadPath;
}
