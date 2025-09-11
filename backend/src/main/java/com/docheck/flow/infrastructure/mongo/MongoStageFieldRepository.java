package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.StageFieldRepository;
import com.docheck.flow.domain.model.StageField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoStageFieldRepository implements StageFieldRepository {
    private final SpringDataStageFieldRepository repo;

    public MongoStageFieldRepository(SpringDataStageFieldRepository repo) { this.repo = repo; }

    private static StageField toDomain(StageFieldDocument d) {
        if (d == null) return null;
        return new StageField(d.id, d.stageId, d.processExternalId, d.label, d.type, d.required, d.placeholder, d.options, d.order, d.createdAt, d.updatedAt);
    }
    private static StageFieldDocument toDoc(StageField f) {
        StageFieldDocument d = new StageFieldDocument();
        d.id = f.getId();
        d.stageId = f.getStageId();
        d.processExternalId = f.getProcessExternalId();
        d.label = f.getLabel();
        d.type = f.getType();
        d.required = f.isRequired();
        d.placeholder = f.getPlaceholder();
        d.options = f.getOptions();
        d.order = f.getOrder();
        d.createdAt = f.getCreatedAt();
        d.updatedAt = f.getUpdatedAt();
        return d;
    }

    @Override public List<StageField> findByStageIdOrderByOrder(String stageId) {
        return repo.findByStageIdOrderByOrderAsc(stageId).stream().map(MongoStageFieldRepository::toDomain).collect(Collectors.toList());
    }
    @Override public List<StageField> findByStageId(String stageId) {
        return repo.findByStageId(stageId).stream().map(MongoStageFieldRepository::toDomain).collect(Collectors.toList());
    }
    @Override public List<StageField> findByProcessExternalId(String processExternalId) {
        return repo.findByProcessExternalId(processExternalId).stream().map(MongoStageFieldRepository::toDomain).collect(Collectors.toList());
    }
    @Override public void deleteByStageId(String stageId) { repo.deleteByStageId(stageId); }
    @Override public void deleteByProcessExternalId(String processExternalId) { repo.deleteByProcessExternalId(processExternalId); }
    @Override public StageField save(StageField field) { return toDomain(repo.save(toDoc(field))); }
    @Override public List<StageField> saveAll(List<StageField> fields) { return repo.saveAll(fields.stream().map(MongoStageFieldRepository::toDoc).collect(Collectors.toList())).stream().map(MongoStageFieldRepository::toDomain).collect(Collectors.toList()); }
}

