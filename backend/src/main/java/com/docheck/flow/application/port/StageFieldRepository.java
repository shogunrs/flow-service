package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.StageField;

import java.util.List;

public interface StageFieldRepository {
    List<StageField> findByStageIdOrderByOrder(String stageId);
    List<StageField> findByStageId(String stageId); // Para cascade service
    List<StageField> findByProcessExternalId(String processExternalId); // Para cascade service SIMPLIFICADO
    void deleteByStageId(String stageId);
    void deleteByProcessExternalId(String processExternalId); // Para cascade service SIMPLIFICADO
    StageField save(StageField field);
    List<StageField> saveAll(List<StageField> fields);
}

