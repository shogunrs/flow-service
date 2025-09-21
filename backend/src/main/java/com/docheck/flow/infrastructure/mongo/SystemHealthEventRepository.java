package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SystemHealthEventRepository extends MongoRepository<SystemHealthEventDocument, String> {
    List<SystemHealthEventDocument> findByAcknowledgedFalseOrderByOccurredAtDesc();
    List<SystemHealthEventDocument> findBySeverityOrderByOccurredAtDesc(String severity);
    List<SystemHealthEventDocument> findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(Instant from);
}
