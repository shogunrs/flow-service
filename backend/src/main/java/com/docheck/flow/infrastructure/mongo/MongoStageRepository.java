package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Stage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoStageRepository implements StageRepository {
    private final SpringDataStageRepository repo;

    public MongoStageRepository(SpringDataStageRepository repo) { this.repo = repo; }

    private static Stage toDomain(StageDocument d) {
        if (d == null) return null;
        return new Stage(d.id, d.processExternalId, d.title, d.slaDays, d.color, d.order, d.createdAt, d.updatedAt);
    }
    private static StageDocument toDoc(Stage s) {
        StageDocument d = new StageDocument();
        d.id = s.getId();
        d.processExternalId = s.getProcessKey();
        d.title = s.getTitle();
        d.slaDays = s.getSlaDays();
        d.color = s.getColor();
        d.order = s.getOrder();
        d.createdAt = s.getCreatedAt();
        d.updatedAt = s.getUpdatedAt();
        return d;
    }

    @Override public List<Stage> findByProcessKeyOrderByOrder(String processExternalId) {
        return repo.findByProcessExternalIdOrderByOrderAsc(processExternalId).stream().map(MongoStageRepository::toDomain).collect(Collectors.toList());
    }
    @Override public void deleteByProcessKey(String processExternalId) { repo.deleteByProcessExternalId(processExternalId); }
    @Override public Stage save(Stage s) { return toDomain(repo.save(toDoc(s))); }
    @Override public List<Stage> saveAll(List<Stage> stages) { return repo.saveAll(stages.stream().map(MongoStageRepository::toDoc).collect(Collectors.toList())).stream().map(MongoStageRepository::toDomain).collect(Collectors.toList()); }
}
