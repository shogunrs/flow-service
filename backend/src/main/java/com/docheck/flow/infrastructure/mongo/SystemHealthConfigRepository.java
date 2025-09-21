package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemHealthConfigRepository extends MongoRepository<SystemHealthConfigDocument, String> {
}
