package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.ProposalRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Proposal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ProposalService {
    private final ProposalRepository repo;
    private final EventPublisher publisher;
    private final StageRepository stageRepo;

    public ProposalService(ProposalRepository repo, EventPublisher publisher, StageRepository stageRepo) {
        this.repo = repo;
        this.publisher = publisher;
        this.stageRepo = stageRepo;
    }

    public List<Proposal> listByProcess(String processKey) {
        return repo.findByProcessKey(processKey);
    }

    @Transactional
    public Proposal create(String processKey, Proposal p) {
        Instant now = Instant.now();
        p.setId(null);
        p.setProcessExternalId(processKey);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        if (p.getStageEnteredAt() == null) p.setStageEnteredAt(now);
        Proposal saved = repo.save(p);
        try { publisher.publish("proposal.created", Map.of("processKey", processKey, "proposalId", saved.getId())); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public Proposal updateForms(String processKey, String proposalId, String stageId, java.util.Map<String, Object> values) {
        var opt = repo.findByIdAndProcessKey(proposalId, processKey);
        var now = Instant.now();
        Proposal p = opt.orElseThrow();
        var forms = p.getForms();
        if (forms == null) forms = new java.util.HashMap<>();
        forms.put(stageId, values == null ? java.util.Map.of() : values);
        p.setForms(forms);
        p.setUpdatedAt(now);
        return repo.save(p);
    }

    @Transactional
    public Proposal update(String processKey, String proposalId, String stageId, String status, String name, Double amount) {
        var opt = repo.findByIdAndProcessKey(proposalId, processKey);
        var now = Instant.now();
        Proposal p = opt.orElseThrow();
        boolean moved = false;
        String sanitizedStatus = status != null && !status.isBlank() ? status : null;
        if (stageId != null && !stageId.isBlank() && !stageId.equals(p.getStageId())) {
            p.setStageId(stageId);
            p.setStageEnteredAt(now);
            moved = true;
            String stageDefaultStatus = null;
            try {
                stageDefaultStatus = stageRepo.findById(stageId)
                    .map(stage -> stage.getDefaultStatus())
                    .filter(def -> def != null && !def.isBlank())
                    .orElse(null);
            } catch (Exception ignored) {}

            if (stageDefaultStatus != null) {
                p.setStatus(stageDefaultStatus);
            } else if (sanitizedStatus != null) {
                p.setStatus(sanitizedStatus);
            }
        }
        if (!moved && sanitizedStatus != null) p.setStatus(sanitizedStatus);
        if (name != null && !name.isBlank()) p.setName(name);
        if (amount != null) p.setAmount(amount);
        p.setUpdatedAt(now);
        Proposal saved = repo.save(p);
        try { publisher.publish(moved ? "proposal.moved" : "proposal.updated", java.util.Map.of("processKey", processKey, "proposalId", saved.getId(), "stageId", saved.getStageId())); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public Proposal save(Proposal proposal) {
        proposal.setUpdatedAt(Instant.now());
        return repo.save(proposal);
    }

    @Transactional
    public void delete(String processKey, String proposalId) {
        repo.deleteByIdAndProcessKey(proposalId, processKey);
        try { publisher.publish("proposal.deleted", java.util.Map.of("processKey", processKey, "proposalId", proposalId)); } catch (Exception ignored) {}
    }
}
