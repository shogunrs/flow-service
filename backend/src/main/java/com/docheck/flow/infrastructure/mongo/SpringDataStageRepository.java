package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataStageRepository extends MongoRepository<StageDocument, String> {
    List<StageDocument> findByProcessKeyOrderByOrderAsc(String processKey);
    void deleteByProcessKey(String processKey);
}

