package com.docheck.flow.api;

import com.docheck.flow.api.dto.ProposalDTO;
import com.docheck.flow.api.dto.UpdateAccessControlDTO;
import com.docheck.flow.api.mapper.ProposalMapper;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.application.service.ProposalAccessService;
import com.docheck.flow.application.service.ProposalService;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.Proposal;
import com.docheck.flow.domain.model.User;
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
    private final ProposalAccessService accessService;
    private final UserService userService;

    public ProposalController(ProposalService service, StageRepository stageRepository,
                             ProposalAccessService accessService, UserService userService) {
        this.service = service;
        this.stageRepository = stageRepository;
        this.accessService = accessService;
        this.userService = userService;
    }

    @GetMapping
    public List<ProposalDTO> list(@PathVariable("processKey") String processKey,
                                 @RequestHeader(value = "X-User-ID", required = false) String currentUserId) {
        List<Proposal> proposals = service.listByProcess(processKey);

        // Se não há usuário logado, retorna todas (backward compatibility)
        if (currentUserId == null || currentUserId.isBlank()) {
            return proposals.stream().map(ProposalMapper::toDto).toList();
        }

        // Filtra propostas que o usuário pode ver
        User currentUser = userService.get(currentUserId).orElse(null);
        if (currentUser == null) {
            return proposals.stream().map(ProposalMapper::toDto).toList();
        }

        return proposals.stream()
                .filter(proposal -> accessService.canViewProposal(proposal, currentUser))
                .map(ProposalMapper::toDto)
                .toList();
    }

    public record CreateRequest(
            @NotBlank String name,
            Double amount,
            @NotBlank String stageId,
            String status,
            Map<String, Object> details,
            // Campos de controle de acesso
            Boolean isPublic,
            java.util.Set<String> visibleToUsers,
            java.util.List<com.docheck.flow.api.dto.ExternalNotificationDTO> externalNotifications
    ) {}

    @PostMapping
    public ResponseEntity<ProposalDTO> create(@PathVariable("processKey") String processKey,
                                              @Valid @RequestBody CreateRequest req,
                                              @RequestHeader(value = "X-User-ID", required = false) String currentUserId,
                                              @RequestHeader(value = "X-Organization-ID", required = false) String organizationId) {
        System.out.println("=== DEBUG CREATE PROPOSAL ===");
        System.out.println("User ID: " + currentUserId);
        System.out.println("Organization ID: " + organizationId);
        System.out.println("Request isPublic: " + req.isPublic());
        System.out.println("Request visibleToUsers: " + req.visibleToUsers());
        System.out.println("Request externalNotifications: " + req.externalNotifications());

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

        // Campos de controle de acesso
        p.setCreatedBy(currentUserId);
        p.setOrganizationId(organizationId);
        p.setIsPublic(req.isPublic() != null ? req.isPublic() : true); // Default público
        p.setVisibleToUsers(req.visibleToUsers() != null ? req.visibleToUsers() : java.util.Set.of());
        if (req.externalNotifications() != null) {
            p.setExternalNotifications(
                com.docheck.flow.api.mapper.ExternalNotificationMapper.toEntityList(req.externalNotifications())
            );
        }

        // Garantir que o criador sempre esteja na lista de acesso para propostas restritas
        accessService.ensureCreatorInAccessList(p);

        Proposal saved = service.create(processKey, p);

        System.out.println("=== DEBUG AFTER SAVE ===");
        System.out.println("Saved ID: " + saved.getId());
        System.out.println("Saved createdBy: " + saved.getCreatedBy());
        System.out.println("Saved organizationId: " + saved.getOrganizationId());
        System.out.println("Saved isPublic: " + saved.getIsPublic());
        System.out.println("Saved visibleToUsers: " + saved.getVisibleToUsers());
        System.out.println("=============================");

        return ResponseEntity.created(URI.create("/api/v1/processes/" + processKey + "/proposals/" + saved.getId()))
                .body(ProposalMapper.toDto(saved));
    }

    private static ProposalDTO toDto(Proposal p) {
        return ProposalMapper.toDto(p);
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

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable("processKey") String processKey) {
        List<Proposal> proposals = service.listByProcess(processKey);

        // Filtrar apenas propostas ativas (não arquivadas)
        List<Proposal> activeProposals = proposals.stream()
                .filter(p -> !p.isArchived())
                .toList();

        // Calcular total monetário
        double totalAmount = activeProposals.stream()
                .filter(p -> p.getAmount() != null)
                .mapToDouble(Proposal::getAmount)
                .sum();

        // Contar propostas por estágio
        Map<String, Long> proposalsByStage = activeProposals.stream()
                .filter(p -> p.getStageId() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Proposal::getStageId,
                        java.util.stream.Collectors.counting()
                ));

        // Calcular valor médio
        double averageAmount = activeProposals.stream()
                .filter(p -> p.getAmount() != null)
                .mapToDouble(Proposal::getAmount)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = Map.of(
                "totalProposals", activeProposals.size(),
                "totalAmount", totalAmount,
                "averageAmount", averageAmount,
                "proposalsByStage", proposalsByStage,
                "processKey", processKey
        );

        return ResponseEntity.ok(stats);
    }

    // Access Control Endpoints
    @PutMapping("/{proposalId}/access")
    public ResponseEntity<ProposalDTO> updateAccessControl(@PathVariable("processKey") String processKey,
                                                           @PathVariable("proposalId") String proposalId,
                                                           @RequestBody UpdateAccessControlDTO updateDto,
                                                           @RequestHeader(value = "X-User-ID", required = false) String currentUserId) {
        try {
            // Buscar a proposta
            Proposal proposal = service.listByProcess(processKey).stream()
                    .filter(p -> proposalId.equals(p.getId()))
                    .findFirst()
                    .orElse(null);

            if (proposal == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar permissão
            User currentUser = currentUserId != null ? userService.get(currentUserId).orElse(null) : null;
            if (currentUser == null || !accessService.canEditAccessControl(proposal, currentUser)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }

            // Atualizar controle de acesso
            ProposalMapper.updateAccessControl(proposal, updateDto);

            // Garantir que o criador sempre esteja na lista de acesso para propostas restritas
            accessService.ensureCreatorInAccessList(proposal);

            // Salvar
            Proposal saved = service.save(proposal);

            return ResponseEntity.ok(ProposalMapper.toDto(saved));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{proposalId}/access")
    public ResponseEntity<Map<String, Object>> getAccessControl(@PathVariable("processKey") String processKey,
                                                               @PathVariable("proposalId") String proposalId,
                                                               @RequestHeader(value = "X-User-ID", required = false) String currentUserId) {
        try {
            // Buscar a proposta
            Proposal proposal = service.listByProcess(processKey).stream()
                    .filter(p -> proposalId.equals(p.getId()))
                    .findFirst()
                    .orElse(null);

            if (proposal == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar permissão de visualização
            User currentUser = currentUserId != null ? userService.get(currentUserId).orElse(null) : null;
            if (currentUser == null || !accessService.canViewProposal(proposal, currentUser)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }

            // Retornar informações de acesso
            Map<String, Object> accessInfo = Map.of(
                    "isPublic", proposal.getIsPublic() != null ? proposal.getIsPublic() : true,
                    "visibleToUsers", proposal.getVisibleToUsers() != null ? proposal.getVisibleToUsers() : java.util.Set.of(),
                    "externalNotifications", proposal.getExternalNotifications() != null ?
                            com.docheck.flow.api.mapper.ExternalNotificationMapper.toDtoList(proposal.getExternalNotifications()) :
                            java.util.List.of(),
                    "canEdit", accessService.canEditAccessControl(proposal, currentUser)
            );

            return ResponseEntity.ok(accessInfo);
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
