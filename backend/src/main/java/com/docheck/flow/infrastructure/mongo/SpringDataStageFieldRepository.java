package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataStageFieldRepository extends MongoRepository<StageFieldDocument, String> {
    List<StageFieldDocument> findByStageIdOrderByOrderAsc(String stageId);
    void deleteByStageId(String stageId);
}

