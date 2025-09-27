package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.domain.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);
    
    private final ProcessRepository repo;
    private final StageRepository stageRepo;
    private final EventPublisher publisher;
    private final StatusService statusService;
    private final UserRepository userRepository;

    public ProcessService(ProcessRepository repo, StageRepository stageRepo, EventPublisher publisher,
                          StatusService statusService, UserRepository userRepository) {
        this.repo = repo;
        this.stageRepo = stageRepo;
        this.publisher = publisher;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    public List<Process> list() { return repo.findAll(); }

    public Optional<Process> get(String externalId) { return repo.findByExternalId(externalId); }

    @Transactional
    public Process create(String externalId, String name) {
        return create(externalId, name, Process.ProcessType.GENERIC, Collections.emptySet());
    }

    @Transactional
    public Process create(String externalId, String name, boolean isFinanceiro) {
        return create(externalId, name, isFinanceiro ? Process.ProcessType.FINANCIAL : Process.ProcessType.GENERIC, Collections.emptySet());
    }

    @Transactional
    public Process create(String externalId, String name, Process.ProcessType type) {
        return create(externalId, name, type, Collections.emptySet());
    }

    @Transactional
    public Process create(String externalId, String name, Process.ProcessType type, Collection<String> allowedUserIds) {
        String ex = (externalId == null || externalId.isBlank()) ? java.util.UUID.randomUUID().toString() : externalId;
        if (repo.existsByExternalId(ex)) throw new IllegalArgumentException("process id already exists");
        Process.ProcessType resolvedType = type == null ? Process.ProcessType.GENERIC : type;
        if (resolvedType == Process.ProcessType.LEAD_QUALIFICATION) {
            try {
                statusService.ensureLeadStatuses();
            } catch (Exception ignored) {
            }
        }
        Set<String> allowed = resolveAllowedUserIds(allowedUserIds);
        Process p = new Process(null, ex, name, true, resolvedType, Instant.now(), Instant.now(), allowed);
        Process saved = repo.save(p);
        publisher.publish("process.created", Map.of(
                "id", ex,
                "name", name,
                "type", resolvedType.name(),
                "isFinanceiro", resolvedType == Process.ProcessType.FINANCIAL
        ));
        return saved;
    }

    @Transactional
    public Process updateName(String externalId, String name) {
        return updateSettings(externalId, name, null);
    }

    @Transactional
    public Process updateSettings(String externalId, String name, Collection<String> allowedUserIds) {
        Process p = repo.findByExternalId(externalId).orElseThrow();
        if (name != null && !name.isBlank()) {
            p.setName(name);
        }
        if (allowedUserIds != null) {
            p.setAllowedUserIds(resolveAllowedUserIds(allowedUserIds));
        }
        p.setUpdatedAt(Instant.now());
        Process saved = repo.save(p);
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", externalId);
        if (name != null && !name.isBlank()) {
            payload.put("name", name);
        }
        if (allowedUserIds != null) {
            payload.put("allowedUserIds", new ArrayList<>(saved.getAllowedUserIds()));
        }
        publisher.publish("process.updated", payload);
        return saved;
    }

    @Transactional
    public void delete(String externalId) {
        // LEGACY METHOD - Usar ProcessCascadeService.deleteProcessWithCascade() para cascade completo
        
        log.warn("⚠️ Usando método de deleção legacy para processo: {}. " + 
                 "Recomendado usar ProcessCascadeService para cascade completo com backup.", externalId);
        
        // Remove etapas associadas antes de deletar o processo (limpeza básica)
        try { 
            stageRepo.deleteByProcessKey(externalId); 
            log.debug("Deletados estágios do processo {}", externalId);
        } catch (Exception e) { 
            log.warn("Erro ao deletar estágios do processo {}: {}", externalId, e.getMessage());
        }
        
        // Deletar processo principal
        repo.deleteByExternalId(externalId);
        
        // Publicar evento básico
        publisher.publish("process.deleted", Map.of(
            "id", externalId,
            "method", "legacy",
            "cascadeComplete", false,
            "timestamp", Instant.now()
        ));
        
        log.info("✅ Processo {} deletado (método legacy)", externalId);
    }

    /**
     * Deleção com cascade completo - MÉTODO RECOMENDADO
     * Delega para ProcessCascadeService que implementa backup automático e cascade total
     */
    @Transactional
    public ProcessCascadeService.CascadeDeleteResult deleteWithCascade(String externalId, boolean createBackup) {
        // Este método deveria ser implementado injetando ProcessCascadeService
        // Por compatibilidade, vou manter aqui a assinatura mas marcar como deprecated
        
        throw new UnsupportedOperationException(
            "Use ProcessCascadeService.deleteProcessWithCascade() diretamente para cascade completo"
        );
    }

    private Set<String> resolveAllowedUserIds(Collection<String> requestedIds) {
        if (requestedIds == null) {
            return Collections.emptySet();
        }
        return requestedIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .filter(userRepository::existsById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
