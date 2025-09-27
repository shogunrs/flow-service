package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataLeadRepository extends MongoRepository<LeadDocument, String> {

    List<LeadDocument> findAllByOrganizationIdOrderByCreatedAtDesc(String organizationId);

    Optional<LeadDocument> findByIdAndOrganizationId(String id, String organizationId);

    void deleteByIdAndOrganizationId(String id, String organizationId);

    boolean existsByEmailAndOrganizationId(String email, String organizationId);
}
