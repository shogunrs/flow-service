package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProposalRepository extends MongoRepository<ProposalDocument, String> {
    List<ProposalDocument> findByProcessExternalIdOrderByCreatedAtDesc(String processExternalId);
    Optional<ProposalDocument> findByIdAndProcessExternalId(String id, String processExternalId);
    void deleteByIdAndProcessExternalId(String id, String processExternalId);
}
