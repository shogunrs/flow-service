package com.docheck.flow.application.service;

import com.docheck.flow.domain.model.Status;
import com.docheck.flow.infrastructure.mongo.MongoStatusRepository;
import com.docheck.flow.infrastructure.mongo.StatusDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatusService {

    private final MongoStatusRepository repository;

    private record StatusSeed(String name, String color, String category) {}

    private static final String LEAD_CATEGORY = "LEADS";
    private static final List<StatusSeed> DEFAULT_LEAD_STATUSES = List.of(
            new StatusSeed("Novo Lead", "#6366F1", LEAD_CATEGORY),
            new StatusSeed("Contato Realizado", "#22D3EE", LEAD_CATEGORY),
            new StatusSeed("Qualificado", "#10B981", LEAD_CATEGORY),
            new StatusSeed("Em Negociação", "#F59E0B", LEAD_CATEGORY),
            new StatusSeed("Convertido", "#8B5CF6", LEAD_CATEGORY),
            new StatusSeed("Descartado", "#EF4444", LEAD_CATEGORY)
    );

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
        if (repository.existsByNameIgnoreCaseAndCategoryIgnoreCase(name, category)) {
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

    public void ensureLeadStatuses() {
        List<Status> statuses = findAll();
        Set<String> existing = statuses.stream()
                .filter(s -> LEAD_CATEGORY.equalsIgnoreCase(s.getCategory()))
                .map(Status::getName)
                .map(name -> name == null ? null : name.toLowerCase(Locale.ROOT))
                .filter(name -> name != null)
                .collect(Collectors.toSet());

        for (StatusSeed seed : DEFAULT_LEAD_STATUSES) {
            String normalizedName = seed.name().toLowerCase(Locale.ROOT);
            if (!existing.contains(normalizedName)) {
                try {
                    create(seed.name(), seed.color(), seed.category());
                    existing.add(normalizedName);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
