package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.domain.model.PromptAgent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PromptAgentRepository extends MongoRepository<PromptAgent, String> {
}
