package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Process;
import java.util.List;
import java.util.Optional;

public interface ProcessRepository {
    Process save(Process p);
    Optional<Process> findByKey(String key);
    List<Process> findAll();
    void deleteByKey(String key);
    boolean existsByKey(String key);
}

