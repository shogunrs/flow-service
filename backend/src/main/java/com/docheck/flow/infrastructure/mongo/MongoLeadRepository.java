package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.LeadRepository;
import com.docheck.flow.domain.model.Lead;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoLeadRepository implements LeadRepository {

    private final SpringDataLeadRepository repository;

    public MongoLeadRepository(SpringDataLeadRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lead save(Lead lead) {
        LeadDocument doc = toDocument(lead);
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(Instant.now());
        }
        doc.setUpdatedAt(Instant.now());
        if (doc.getEmail() != null) {
            doc.setEmail(doc.getEmail().toLowerCase());
        }
        LeadDocument saved = repository.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<Lead> findByIdAndOrganizationId(String id, String organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId)
                .map(this::toDomain);
    }

    @Override
    public List<Lead> findAllByOrganizationId(String organizationId) {
        return repository.findAllByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByIdAndOrganizationId(String id, String organizationId) {
        repository.deleteByIdAndOrganizationId(id, organizationId);
    }

    @Override
    public boolean existsByEmailAndOrganizationId(String email, String organizationId) {
        return repository.existsByEmailAndOrganizationId(email, organizationId);
    }

    private Lead toDomain(LeadDocument document) {
        if (document == null) {
            return null;
        }
        return Lead.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .createdBy(document.getCreatedBy())
                .name(document.getName())
                .email(document.getEmail())
                .phone(document.getPhone())
                .origin(Lead.LeadOrigin.fromString(document.getOrigin()))
                .type(document.getType() != null ? Lead.LeadType.fromString(document.getType()) : Lead.LeadType.CLIENT)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private LeadDocument toDocument(Lead lead) {
        if (lead == null) {
            return null;
        }
        return LeadDocument.builder()
                .id(lead.getId())
                .organizationId(lead.getOrganizationId())
                .createdBy(lead.getCreatedBy())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .origin(lead.getOrigin() != null ? lead.getOrigin().name() : null)
                .type(lead.getType() != null ? lead.getType().name() : null)
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}
