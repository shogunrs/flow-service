package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.StatusCategory;

import java.util.List;
import java.util.Optional;

public interface StatusCategoryRepository {

    List<StatusCategory> findAll();

    Optional<StatusCategory> findById(String id);

    Optional<StatusCategory> findByName(String name);

    List<StatusCategory> findByNameContaining(String name);

    Optional<StatusCategory> findByStatusId(String statusId);

    StatusCategory save(StatusCategory category);

    void deleteById(String id);

    boolean existsById(String id);

    boolean existsByName(String name);
}