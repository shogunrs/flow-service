package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.ProposalRepository;
import com.docheck.flow.domain.model.Proposal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoProposalRepository implements ProposalRepository {
    private final SpringDataProposalRepository repo;

    public MongoProposalRepository(SpringDataProposalRepository repo) { this.repo = repo; }

    private static Proposal toDomain(ProposalDocument d) {
        if (d == null) return null;
        return new Proposal(d.id, d.processExternalId, d.name, d.amount, d.stageId, d.status, d.archived,
                d.stageEnteredAt, d.createdAt, d.updatedAt, d.details, d.forms);
    }
    private static ProposalDocument toDoc(Proposal p) {
        ProposalDocument d = new ProposalDocument();
        d.id = p.getId();
        d.processExternalId = p.getProcessExternalId();
        d.name = p.getName();
        d.amount = p.getAmount();
        d.stageId = p.getStageId();
        d.status = p.getStatus();
        d.archived = p.isArchived();
        d.stageEnteredAt = p.getStageEnteredAt();
        d.createdAt = p.getCreatedAt();
        d.updatedAt = p.getUpdatedAt();
        d.details = p.getDetails();
        d.forms = p.getForms();
        return d;
    }

    @Override public List<Proposal> findByProcessKey(String processExternalId) {
        return repo.findByProcessExternalIdOrderByCreatedAtDesc(processExternalId).stream().map(MongoProposalRepository::toDomain).collect(Collectors.toList());
    }
    @Override public List<Proposal> findByProcessExternalId(String processExternalId) {
        return repo.findByProcessExternalId(processExternalId).stream().map(MongoProposalRepository::toDomain).collect(Collectors.toList());
    }
    @Override public Proposal save(Proposal p) { return toDomain(repo.save(toDoc(p))); }
    @Override public Optional<Proposal> findByIdAndProcessKey(String id, String processExternalId) {
        return repo.findByIdAndProcessExternalId(id, processExternalId).map(MongoProposalRepository::toDomain);
    }
    @Override public void deleteByIdAndProcessKey(String id, String processExternalId) {
        repo.deleteByIdAndProcessExternalId(id, processExternalId);
    }
    @Override public void deleteByProcessExternalId(String processExternalId) {
        repo.deleteByProcessExternalId(processExternalId);
    }
}
