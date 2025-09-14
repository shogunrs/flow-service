package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoStatusRepository extends MongoRepository<StatusDocument, String> {
    List<StatusDocument> findByOrderByCreatedAtAsc();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
}