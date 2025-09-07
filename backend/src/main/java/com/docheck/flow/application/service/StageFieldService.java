package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.StageFieldRepository;
import com.docheck.flow.domain.model.StageField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StageFieldService {
    private final StageFieldRepository repo;
    private final EventPublisher publisher;

    public StageFieldService(StageFieldRepository repo, EventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public List<StageField> listByStage(String stageId) {
        return repo.findByStageIdOrderByOrder(stageId);
    }

    @Transactional
    public List<StageField> replaceForStage(String stageId, List<StageField> incoming) {
        repo.deleteByStageId(stageId);
        List<StageField> toSave = new ArrayList<>();
        int i = 0;
        for (StageField f : incoming) {
            StageField nf = new StageField(null, stageId,
                    f.getLabel(), f.getType(), f.isRequired(), f.getPlaceholder(), f.getOptions(), i++,
                    Instant.now(), Instant.now());
            toSave.add(nf);
        }
        List<StageField> saved = repo.saveAll(toSave);
        try { publisher.publish("stage.fields.updated", Map.of("stageId", stageId, "count", saved.size())); } catch (Exception ignored) {}
        return saved;
    }
}

