package com.docheck.flow.api.dto;

import com.docheck.flow.domain.model.KnowledgeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocumentDto {
    private String id;
    private String agentId;
    private String originalFileName;
    private String storedFilePath;
    private KnowledgeDocument.ProcessingStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static KnowledgeDocumentDto fromEntity(KnowledgeDocument doc) {
        return new KnowledgeDocumentDto(
                doc.getId(),
                doc.getAgentId(),
                doc.getOriginalFileName(),
                doc.getStoredFilePath(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
