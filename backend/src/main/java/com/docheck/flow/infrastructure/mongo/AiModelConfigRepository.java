package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiModelConfigRepository extends MongoRepository<AiModelConfigDocument, String> {

    List<AiModelConfigDocument> findByProviderId(String providerId);

    List<AiModelConfigDocument> findByProviderIdAndAvailableTrue(String providerId);

    Optional<AiModelConfigDocument> findByProviderIdAndModelName(String providerId, String modelName);

    @Query("{ 'providerId': ?0, 'defaultModel': true }")
    Optional<AiModelConfigDocument> findDefaultByProviderId(String providerId);

    long countByProviderId(String providerId);

    long countByProviderIdAndAvailableTrue(String providerId);

    boolean existsByProviderIdAndModelName(String providerId, String modelName);
}