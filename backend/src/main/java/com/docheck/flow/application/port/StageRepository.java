package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Stage;

import java.util.List;

public interface StageRepository {
    List<Stage> findByProcessKeyOrderByOrder(String processKey);
    void deleteByProcessKey(String processKey);
    Stage save(Stage s);
    List<Stage> saveAll(List<Stage> stages);
}

