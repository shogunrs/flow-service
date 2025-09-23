package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.StatusCategoryRepository;
import com.docheck.flow.domain.model.StatusCategory;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoStatusCategoryRepository implements StatusCategoryRepository {

    private final SpringDataStatusCategoryRepository springRepository;

    public MongoStatusCategoryRepository(SpringDataStatusCategoryRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public List<StatusCategory> findAll() {
        return springRepository.findAll()
                .stream()
                .map(StatusCategoryDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatusCategory> findById(String id) {
        return springRepository.findById(id)
                .map(StatusCategoryDocument::toDomain);
    }

    @Override
    public Optional<StatusCategory> findByName(String name) {
        return springRepository.findByName(name)
                .map(StatusCategoryDocument::toDomain);
    }

    @Override
    public List<StatusCategory> findByNameContaining(String name) {
        return springRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(StatusCategoryDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StatusCategory> findByStatusId(String statusId) {
        return springRepository.findByStatusId(statusId)
                .map(StatusCategoryDocument::toDomain);
    }

    @Override
    public StatusCategory save(StatusCategory category) {
        StatusCategoryDocument document = new StatusCategoryDocument(category);
        StatusCategoryDocument saved = springRepository.save(document);
        return saved.toDomain();
    }

    @Override
    public void deleteById(String id) {
        springRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return springRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return springRepository.findByName(name).isPresent();
    }
}