package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MonitoredComponentRepository extends MongoRepository<MonitoredComponentDocument, String> {
    Optional<MonitoredComponentDocument> findByComponentKey(String componentKey);
}
