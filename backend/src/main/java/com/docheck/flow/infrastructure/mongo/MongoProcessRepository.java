package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.domain.model.Process;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoProcessRepository implements ProcessRepository {
    private final SpringDataProcessRepository repo;

    public MongoProcessRepository(SpringDataProcessRepository repo) { this.repo = repo; }

    private static Process toDomain(ProcessDocument d) {
        if (d == null) return null;
        Process.ProcessType type = d.type != null
                ? d.type
                : (d.isFinanceiro ? Process.ProcessType.FINANCIAL : Process.ProcessType.GENERIC);
        Set<String> allowed = d.allowedUserIds != null ? new HashSet<>(d.allowedUserIds) : Collections.emptySet();
        return new Process(d.id, d.externalId, d.name, d.active, type, d.createdAt, d.updatedAt, allowed);
    }
    private static ProcessDocument toDoc(Process p) {
        ProcessDocument d = new ProcessDocument();
        d.id = p.getId();
        d.externalId = p.getExternalId();
        d.name = p.getName();
        d.active = p.isActive();
        d.type = p.getType();
        d.isFinanceiro = p.isFinanceiro();
        d.createdAt = p.getCreatedAt();
        d.updatedAt = p.getUpdatedAt();
        d.allowedUserIds = new HashSet<>(p.getAllowedUserIds());
        return d;
    }

    @Override public Process save(Process p) { return toDomain(repo.save(toDoc(p))); }
    @Override public Optional<Process> findByExternalId(String externalId) { return repo.findByExternalId(externalId).map(MongoProcessRepository::toDomain); }
    @Override public List<Process> findAll() { return repo.findAll().stream().map(MongoProcessRepository::toDomain).collect(Collectors.toList()); }
    @Override public void deleteByExternalId(String externalId) { repo.deleteByExternalId(externalId); }
    @Override public boolean existsByExternalId(String externalId) { return repo.existsByExternalId(externalId); }
}
