package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmail(String email);

    Optional<UserDocument> findByIdAndOrganizationId(String id, String organizationId);

    List<UserDocument> findAllByOrganizationId(String organizationId);

    void deleteByIdAndOrganizationId(String id, String organizationId);
}
