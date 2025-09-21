package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.SystemHealthConfigDTO;
import com.docheck.flow.api.dto.SystemHealthEventRequest;
import com.docheck.flow.api.dto.SystemHealthEventSummaryDTO;
import com.docheck.flow.domain.model.SystemHealthEvent;
import com.docheck.flow.infrastructure.mongo.MonitoredComponentDocument;
import com.docheck.flow.infrastructure.mongo.MonitoredComponentRepository;
import com.docheck.flow.infrastructure.mongo.SystemHealthConfigDocument;
import com.docheck.flow.infrastructure.mongo.SystemHealthConfigRepository;
import com.docheck.flow.infrastructure.mongo.SystemHealthEventDocument;
import com.docheck.flow.infrastructure.mongo.SystemHealthEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemHealthEventService {
    private static final String CONFIG_ID = "default";

    private final SystemHealthEventRepository repository;
    private final SystemHealthConfigRepository configRepository;
    private final MonitoredComponentRepository componentRepository;

    public SystemHealthEventService(SystemHealthEventRepository repository,
                                    SystemHealthConfigRepository configRepository,
                                    MonitoredComponentRepository componentRepository) {
        this.repository = repository;
        this.configRepository = configRepository;
        this.componentRepository = componentRepository;
    }

    public SystemHealthEvent recordEvent(SystemHealthEventRequest request) {
        Instant now = Instant.now();
        Instant occurredAt = Optional.ofNullable(request.getOccurredAt()).orElse(now);

        final MonitoredComponentDocument componentDoc;
        if (request.getComponent() != null) {
            componentDoc = componentRepository.findByComponentKey(request.getComponent().trim().toLowerCase())
                    .filter(MonitoredComponentDocument::isEnabled)
                    .orElse(null);
        } else {
            componentDoc = null;
        }

        final String resolvedSeverity = Optional.ofNullable(request.getSeverity())
                .map(String::toUpperCase)
                .orElseGet(() -> componentDoc != null ? componentDoc.getDefaultSeverity() : "INFO");

        final String resolvedStatusTag = Optional.ofNullable(request.getStatusTag())
                .orElseGet(() -> componentDoc != null ? componentDoc.getStatusTag() : null);

        SystemHealthEventDocument document = SystemHealthEventDocument.builder()
                .component(request.getComponent())
                .statusTag(resolvedStatusTag)
                .severity(resolvedSeverity)
                .message(request.getMessage())
                .details(request.getDetails())
                .context(request.getContext())
                .occurredAt(occurredAt)
                .createdAt(now)
                .acknowledged(false)
                .build();

        SystemHealthEventDocument saved = repository.save(document);
        return toDomain(saved);
    }

    public List<SystemHealthEvent> list(Optional<Boolean> acknowledged, Optional<String> severity, Optional<Instant> from) {
        if (acknowledged.isPresent() && !acknowledged.get()) {
            return repository.findByAcknowledgedFalseOrderByOccurredAtDesc().stream()
                    .map(SystemHealthEventService::toDomain)
                    .collect(Collectors.toList());
        }
        if (severity.isPresent()) {
            return repository.findBySeverityOrderByOccurredAtDesc(severity.get().toUpperCase()).stream()
                    .map(SystemHealthEventService::toDomain)
                    .collect(Collectors.toList());
        }
        if (from.isPresent()) {
            return repository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(from.get()).stream()
                    .map(SystemHealthEventService::toDomain)
                    .collect(Collectors.toList());
        }
        return repository.findAll().stream()
                .sorted(Comparator.comparing(SystemHealthEventDocument::getOccurredAt).reversed())
                .map(SystemHealthEventService::toDomain)
                .collect(Collectors.toList());
    }

    public SystemHealthEvent acknowledge(String eventId, String reviewerId, String reviewerName, String notes) {
        SystemHealthEventDocument document = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (document.isAcknowledged()) {
            return toDomain(document);
        }
        document.setAcknowledged(true);
        document.setAcknowledgedAt(Instant.now());
        document.setAcknowledgedBy(Optional.ofNullable(reviewerName).orElse(reviewerId));
        document.setAcknowledgementNotes(notes);
        return toDomain(repository.save(document));
    }

    public SystemHealthEventSummaryDTO buildSummary() {
        List<SystemHealthEvent> events = list(Optional.empty(), Optional.empty(), Optional.empty());
        Map<String, Long> bySeverity = events.stream()
                .collect(Collectors.groupingBy(event -> Optional.ofNullable(event.getSeverity()).orElse("INFO"), Collectors.counting()));

        long unacknowledged = events.stream().filter(e -> !e.isAcknowledged()).count();

        Map<String, SystemHealthEvent> latestByComponent = events.stream()
                .collect(Collectors.groupingBy(SystemHealthEvent::getComponent,
                        Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(SystemHealthEvent::getOccurredAt)),
                                optional -> optional.orElse(null))));

        return SystemHealthEventSummaryDTO.builder()
                .bySeverity(bySeverity)
                .unacknowledgedCount(unacknowledged)
                .latestPerComponent(latestByComponent)
                .build();
    }

    public SystemHealthConfigDTO getConfig() {
        return toConfigDto(loadConfig());
    }

    public SystemHealthConfigDTO updateConfig(SystemHealthConfigDTO request) {
        SystemHealthConfigDocument document = configRepository.findById(CONFIG_ID)
                .orElseGet(() -> SystemHealthConfigDocument.builder()
                        .id(CONFIG_ID)
                        .build());
        document.setMonitoredStatusTags(request.getMonitoredStatusTags());
        document.setUpdatedAt(Instant.now());
        return toConfigDto(configRepository.save(document));
    }

    private SystemHealthConfigDocument loadConfig() {
        return configRepository.findById(CONFIG_ID)
                .orElseGet(() -> configRepository.save(SystemHealthConfigDocument.builder()
                        .id(CONFIG_ID)
                        .monitoredStatusTags(List.of())
                        .updatedAt(Instant.now())
                        .build()));
    }

    private static SystemHealthEvent toDomain(SystemHealthEventDocument document) {
        if (document == null) {
            return null;
        }
        return SystemHealthEvent.builder()
                .id(document.getId())
                .component(document.getComponent())
                .statusTag(document.getStatusTag())
                .severity(document.getSeverity())
                .message(document.getMessage())
                .details(document.getDetails())
                .context(document.getContext())
                .occurredAt(document.getOccurredAt())
                .createdAt(document.getCreatedAt())
                .acknowledged(document.isAcknowledged())
                .acknowledgedBy(document.getAcknowledgedBy())
                .acknowledgedAt(document.getAcknowledgedAt())
                .acknowledgementNotes(document.getAcknowledgementNotes())
                .build();
    }

    private static SystemHealthConfigDTO toConfigDto(SystemHealthConfigDocument document) {
        return SystemHealthConfigDTO.builder()
                .monitoredStatusTags(document.getMonitoredStatusTags() != null ? document.getMonitoredStatusTags() : List.of())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
