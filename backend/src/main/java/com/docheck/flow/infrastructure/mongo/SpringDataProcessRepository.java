package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataProcessRepository extends MongoRepository<ProcessDocument, String> {
    Optional<ProcessDocument> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
    void deleteByExternalId(String externalId);
}
