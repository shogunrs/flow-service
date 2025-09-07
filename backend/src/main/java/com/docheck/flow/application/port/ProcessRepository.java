package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Process;
import java.util.List;
import java.util.Optional;

public interface ProcessRepository {
    Process save(Process p);
    Optional<Process> findByExternalId(String externalId);
    List<Process> findAll();
    void deleteByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
