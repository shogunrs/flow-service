package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.Proposal;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository {
    List<Proposal> findByProcessKey(String processExternalId);
    List<Proposal> findByProcessExternalId(String processExternalId); // Para cascade service
    Proposal save(Proposal p);
    Optional<Proposal> findByIdAndProcessKey(String id, String processExternalId);
    void deleteByIdAndProcessKey(String id, String processExternalId);
    void deleteByProcessExternalId(String processExternalId); // Para cascade service
}
