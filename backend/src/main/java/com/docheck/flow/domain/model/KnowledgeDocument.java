package com.docheck.flow.domain.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "knowledge_documents")
public class KnowledgeDocument {

    @Id
    private String id;

    private String agentId;

    private String originalFileName;

    private String storedFilePath; // Novo campo para o caminho do arquivo no storage

    private ProcessingStatus status;

    private String markdownContent;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum ProcessingStatus {
        QUEUED, PROCESSING, COMPLETED, FAILED
    }
}
