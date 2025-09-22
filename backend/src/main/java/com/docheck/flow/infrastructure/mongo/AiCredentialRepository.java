package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiCredentialRepository extends MongoRepository<AiCredentialDocument, String> {

    List<AiCredentialDocument> findByProviderId(String providerId);

    List<AiCredentialDocument> findByProviderIdAndEnabledTrue(String providerId);

    @Query("{ 'providerId': ?0, 'enabled': true }")
    List<AiCredentialDocument> findActiveByProviderId(String providerId);

    long countByProviderId(String providerId);

    long countByProviderIdAndEnabledTrue(String providerId);
}