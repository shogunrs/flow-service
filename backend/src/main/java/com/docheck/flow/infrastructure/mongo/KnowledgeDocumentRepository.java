package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.domain.model.KnowledgeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends MongoRepository<KnowledgeDocument, String> {
    List<KnowledgeDocument> findByAgentId(String agentId);
    List<KnowledgeDocument> findByStatusIn(List<KnowledgeDocument.ProcessingStatus> statuses);
}
