package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.domain.model.Process;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoProcessRepository implements ProcessRepository {
    private final SpringDataProcessRepository repo;

    public MongoProcessRepository(SpringDataProcessRepository repo) { this.repo = repo; }

    private static Process toDomain(ProcessDocument d) {
        if (d == null) return null;
        return new Process(d.id, d.key, d.name, d.active, d.createdAt, d.updatedAt);
    }
    private static ProcessDocument toDoc(Process p) {
        ProcessDocument d = new ProcessDocument();
        d.id = p.getId();
        d.key = p.getKey();
        d.name = p.getName();
        d.active = p.isActive();
        d.createdAt = p.getCreatedAt();
        d.updatedAt = p.getUpdatedAt();
        return d;
    }

    @Override public Process save(Process p) { return toDomain(repo.save(toDoc(p))); }
    @Override public Optional<Process> findByKey(String key) { return repo.findByKey(key).map(MongoProcessRepository::toDomain); }
    @Override public List<Process> findAll() { return repo.findAll().stream().map(MongoProcessRepository::toDomain).collect(Collectors.toList()); }
    @Override public void deleteByKey(String key) { repo.deleteByKey(key); }
    @Override public boolean existsByKey(String key) { return repo.existsByKey(key); }
}

