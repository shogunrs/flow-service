package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Stage;

import java.util.List;
import java.util.Optional;

public interface StageRepository {
    List<Stage> findByProcessKeyOrderByOrder(String processKey);
    List<Stage> findByProcessKey(String processKey); // Para cascade service
    Optional<Stage> findById(String id);
    void deleteByProcessKey(String processKey);
    void deleteById(String id);
    Stage save(Stage s);
    List<Stage> saveAll(List<Stage> stages);
}

