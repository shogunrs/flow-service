package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.StageFieldRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.StageField;
import com.docheck.flow.domain.model.Stage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StageFieldService {
    private final StageFieldRepository repo;
    private final StageRepository stageRepo;
    private final EventPublisher publisher;

    public StageFieldService(StageFieldRepository repo, StageRepository stageRepo, EventPublisher publisher) {
        this.repo = repo;
        this.stageRepo = stageRepo;
        this.publisher = publisher;
    }

    public List<StageField> listByStage(String stageId) {
        return repo.findByStageIdOrderByOrder(stageId);
    }

    @Transactional
    public List<StageField> replaceForStage(String stageId, List<StageField> incoming) {
        repo.deleteByStageId(stageId);

        // 🎯 BUSCAR PROCESSO EXTERNAL ID DO STAGE (HERANÇA!)
        Stage stage = stageRepo.findById(stageId)
            .orElseThrow(() -> new RuntimeException("Stage não encontrado: " + stageId));
        String processExternalId = stage.getProcessKey();

        List<StageField> toSave = new ArrayList<>();
        int i = 0;
        for (StageField f : incoming) {
            StageField nf = new StageField(null, stageId, processExternalId,
                    f.getLabel(), f.getType(), f.isRequired(), f.getPlaceholder(), f.getOptions(), i++,
                    Instant.now(), Instant.now());
            toSave.add(nf);
        }
        List<StageField> saved = repo.saveAll(toSave);
        try {
            publisher.publish("stage.fields.updated", Map.of("stageId", stageId, "count", saved.size()));
        } catch (Exception ignored) {
        }
        return saved;
    }
}
