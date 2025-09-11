package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataStageRepository extends MongoRepository<StageDocument, String> {
    List<StageDocument> findByProcessExternalIdOrderByOrderAsc(String processExternalId);
    List<StageDocument> findByProcessExternalId(String processExternalId);
    void deleteByProcessExternalId(String processExternalId);
}
