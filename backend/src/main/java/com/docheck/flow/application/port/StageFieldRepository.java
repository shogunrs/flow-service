package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.StageField;

import java.util.List;

public interface StageFieldRepository {
    List<StageField> findByStageIdOrderByOrder(String stageId);
    void deleteByStageId(String stageId);
    StageField save(StageField field);
    List<StageField> saveAll(List<StageField> fields);
}

