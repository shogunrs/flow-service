package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.domain.model.AiProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link AiProvider} entity.
 */
@Repository
public interface AiProviderRepository extends MongoRepository<AiProvider, String> {
}
