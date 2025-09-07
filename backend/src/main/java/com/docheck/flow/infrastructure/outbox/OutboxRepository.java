package com.docheck.flow.infrastructure.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends MongoRepository<OutboxDocument, String> {
    List<OutboxDocument> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
    long countByPublishedAtIsNullAndCreatedAtBefore(Instant ts);
}

