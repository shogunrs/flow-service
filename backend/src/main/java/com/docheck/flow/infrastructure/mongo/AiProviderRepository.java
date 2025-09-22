package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiProviderRepository extends MongoRepository<AiProviderDocument, String> {

    Optional<AiProviderDocument> findBySlug(String slug);

    Optional<AiProviderDocument> findByName(String name);

    List<AiProviderDocument> findByEnabledTrue();

    @Query("{ 'enabled': true }")
    List<AiProviderDocument> findAllActive();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}