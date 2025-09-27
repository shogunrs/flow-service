package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Lead;

import java.util.List;
import java.util.Optional;

public interface LeadRepository {
    Lead save(Lead lead);

    Optional<Lead> findByIdAndOrganizationId(String id, String organizationId);

    List<Lead> findAllByOrganizationId(String organizationId);

    void deleteByIdAndOrganizationId(String id, String organizationId);

    boolean existsByEmailAndOrganizationId(String email, String organizationId);
}
