package com.docheck.flow.application.service;

import com.docheck.flow.domain.model.Status;
import com.docheck.flow.infrastructure.mongo.MongoStatusRepository;
import com.docheck.flow.infrastructure.mongo.StatusDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class StatusService {

    private final MongoStatusRepository repository;

    public StatusService(MongoStatusRepository repository) {
        this.repository = repository;
    }

    public List<Status> findAll() {
        return repository.findByOrderByCreatedAtAsc().stream()
                .map(this::toModel)
                .toList();
    }

    public Optional<Status> findById(String id) {
        return repository.findById(id).map(this::toModel);
    }

    public Status create(String name, String color) {
        return create(name, color, "ESTEIRA"); // Default category
    }

    public Status create(String name, String color, String category) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Status com este nome já existe");
        }

        StatusDocument document = new StatusDocument(name, color, category);
        StatusDocument saved = repository.save(document);
        return toModel(saved);
    }

    public Status update(String id, String name, String color) {
        return update(id, name, color, null); // Keep existing category
    }

    public Status update(String id, String name, String color, String category) {
        Optional<StatusDocument> existing = repository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Status não encontrado");
        }

        if (repository.existsByNameAndIdNot(name, id)) {
            throw new IllegalArgumentException("Status com este nome já existe");
        }

        StatusDocument document = existing.get();
        document.setName(name);
        document.setColor(color);
        if (category != null) {
            document.setCategory(category);
        }
        document.setUpdatedAt(Instant.now());

        StatusDocument saved = repository.save(document);
        return toModel(saved);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Status não encontrado");
        }
        repository.deleteById(id);
    }

    private Status toModel(StatusDocument document) {
        Status status = new Status();
        status.setId(document.getId());
        status.setName(document.getName());
        status.setColor(document.getColor());
        status.setCategory(document.getCategory() != null ? document.getCategory() : "ESTEIRA");
        status.setCreatedAt(document.getCreatedAt());
        status.setUpdatedAt(document.getUpdatedAt());
        return status;
    }
}