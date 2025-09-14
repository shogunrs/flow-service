package com.docheck.flow.api;

import com.docheck.flow.api.dto.ProposalDTO;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.application.service.ProposalService;
import com.docheck.flow.domain.model.Proposal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/processes/{processKey}/proposals")
public class ProposalController {
    private final ProposalService service;
    private final StageRepository stageRepository;

    public ProposalController(ProposalService service, StageRepository stageRepository) {
        this.service = service;
        this.stageRepository = stageRepository;
    }

    @GetMapping
    public List<ProposalDTO> list(@PathVariable("processKey") String processKey) {
        return service.listByProcess(processKey).stream().map(ProposalController::toDto).toList();
    }

    public record CreateRequest(
            @NotBlank String name,
            Double amount,
            @NotBlank String stageId,
            String status,
            Map<String, Object> details
    ) {}

    @PostMapping
    public ResponseEntity<ProposalDTO> create(@PathVariable("processKey") String processKey,
                                              @Valid @RequestBody CreateRequest req) {
        Proposal p = new Proposal();
        p.setName(req.name());
        p.setAmount(req.amount());
        p.setStageId(req.stageId());
        // Status: usar status da requisição, ou buscar defaultStatus da etapa, ou "Pendente" como último recurso
        String status = req.status();
        if (status == null || status.isBlank()) {
            status = getStageDefaultStatus(req.stageId());
            if (status == null || status.isBlank()) {
                status = "Pendente";
            }
        }
        p.setStatus(status);
        p.setArchived(false);
        p.setStageEnteredAt(Instant.now());
        p.setDetails(req.details());
        Proposal saved = service.create(processKey, p);
        return ResponseEntity.created(URI.create("/api/v1/processes/" + processKey + "/proposals/" + saved.getId()))
                .body(toDto(saved));
    }

    private static ProposalDTO toDto(Proposal p) {
        return new ProposalDTO(
                p.getId(), p.getName(), p.getAmount(), p.getStageId(), p.getStatus(), p.isArchived(),
                p.getStageEnteredAt(), p.getCreatedAt(), p.getUpdatedAt(), p.getDetails()
        );
    }

    // Forms (per-stage answers)
    @GetMapping("/{proposalId}/forms")
    public ResponseEntity<Map<String, Map<String, Object>>> getForms(@PathVariable("processKey") String processKey,
                                                                     @PathVariable("proposalId") String proposalId) {
        return service.listByProcess(processKey).stream()
                .filter(p -> proposalId.equals(p.getId()))
                .findFirst()
                .map(p -> ResponseEntity.ok(p.getForms()))
                .orElse(ResponseEntity.notFound().build());
    }

    public record StageFormRequest(Map<String, Object> values) {}

    @PutMapping("/{proposalId}/forms/{stageId}")
    public ResponseEntity<Map<String, Object>> putStageForm(@PathVariable("processKey") String processKey,
                                                            @PathVariable("proposalId") String proposalId,
                                                            @PathVariable("stageId") String stageId,
                                                            @RequestBody StageFormRequest req) {
        try {
            var saved = service.updateForms(processKey, proposalId, stageId, req.values());
            return ResponseEntity.ok(saved.getForms() == null ? java.util.Map.of() : saved.getForms().get(stageId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update proposal (move stage, change status/name/amount)
    public record UpdateRequest(String stageId, String status, String name, Double amount) {}

    @PutMapping("/{proposalId}")
    public ResponseEntity<ProposalDTO> update(@PathVariable("processKey") String processKey,
                                              @PathVariable("proposalId") String proposalId,
                                              @RequestBody UpdateRequest req) {
        try {
            Proposal saved = service.update(processKey, proposalId, req.stageId(), req.status(), req.name(), req.amount());
            return ResponseEntity.ok(toDto(saved));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{proposalId}")
    public ResponseEntity<Void> delete(@PathVariable("processKey") String processKey,
                                       @PathVariable("proposalId") String proposalId) {
        try {
            service.delete(processKey, proposalId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getStageDefaultStatus(String stageId) {
        if (stageId == null || stageId.isBlank()) {
            return null;
        }
        try {
            return stageRepository.findById(stageId)
                    .map(stage -> stage.getDefaultStatus())
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
