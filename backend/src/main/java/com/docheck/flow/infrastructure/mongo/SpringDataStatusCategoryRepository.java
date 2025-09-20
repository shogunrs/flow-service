package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataStatusCategoryRepository extends MongoRepository<StatusCategoryDocument, String> {

    Optional<StatusCategoryDocument> findByName(String name);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<StatusCategoryDocument> findByNameContainingIgnoreCase(String name);

    @Query("{ 'statuses.name': ?0 }")
    List<StatusCategoryDocument> findByStatusName(String statusName);

    @Query("{ 'statuses.id': ?0 }")
    Optional<StatusCategoryDocument> findByStatusId(String statusId);
}