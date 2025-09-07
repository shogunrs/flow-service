package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataProcessRepository extends MongoRepository<ProcessDocument, String> {
    Optional<ProcessDocument> findByKey(String key);
    boolean existsByKey(String key);
    void deleteByKey(String key);
}

