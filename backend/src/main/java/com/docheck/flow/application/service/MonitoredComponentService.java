package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.MonitoredComponentDTO;
import com.docheck.flow.api.dto.MonitoredComponentRequest;

import com.docheck.flow.infrastructure.mongo.MonitoredComponentDocument;
import com.docheck.flow.infrastructure.mongo.MonitoredComponentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonitoredComponentService {
    private final MonitoredComponentRepository repository;

    public MonitoredComponentService(MonitoredComponentRepository repository) {
        this.repository = repository;
    }

    public List<MonitoredComponentDTO> list() {
        return repository.findAll().stream()
                .map(MonitoredComponentService::toDto)
                .collect(Collectors.toList());
    }

    public MonitoredComponentDTO create(MonitoredComponentRequest request) {
        validateRequest(request);
        Instant now = Instant.now();

        MonitoredComponentDocument document = MonitoredComponentDocument.builder()
                .name(request.getName())
                .componentKey(normalizeKey(request.getComponentKey()))
                .description(request.getDescription())
                .statusTag(request.getStatusTag())
                .defaultSeverity(Optional.ofNullable(request.getDefaultSeverity()).orElse("WARN").toUpperCase())
                .enabled(Optional.ofNullable(request.getEnabled()).orElse(Boolean.TRUE))
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toDto(repository.save(document));
    }

    public MonitoredComponentDTO update(String id, MonitoredComponentRequest request) {
        MonitoredComponentDocument existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Component not found"));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getComponentKey() != null) {
            existing.setComponentKey(normalizeKey(request.getComponentKey()));
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getStatusTag() != null) {
            existing.setStatusTag(request.getStatusTag());
        }
        if (request.getDefaultSeverity() != null) {
            existing.setDefaultSeverity(request.getDefaultSeverity().toUpperCase());
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        existing.setUpdatedAt(Instant.now());
        return toDto(repository.save(existing));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private static String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("componentKey must not be empty");
        }
        return value.trim().toLowerCase();
    }

    private static void validateRequest(MonitoredComponentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        if (request.getComponentKey() == null || request.getComponentKey().isBlank()) {
            throw new IllegalArgumentException("componentKey must not be empty");
        }
    }

    private static MonitoredComponentDTO toDto(MonitoredComponentDocument document) {
        return MonitoredComponentDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .componentKey(document.getComponentKey())
                .description(document.getDescription())
                .statusTag(document.getStatusTag())
                .defaultSeverity(document.getDefaultSeverity())
                .enabled(document.isEnabled())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
