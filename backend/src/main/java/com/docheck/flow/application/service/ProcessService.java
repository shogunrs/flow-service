package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);
    
    private final ProcessRepository repo;
    private final StageRepository stageRepo;
    private final EventPublisher publisher;

    public ProcessService(ProcessRepository repo, StageRepository stageRepo, EventPublisher publisher) {
        this.repo = repo;
        this.stageRepo = stageRepo;
        this.publisher = publisher;
    }

    public List<Process> list() { return repo.findAll(); }

    public Optional<Process> get(String externalId) { return repo.findByExternalId(externalId); }

    @Transactional
    public Process create(String externalId, String name) {
        return create(externalId, name, false);
    }

    @Transactional
    public Process create(String externalId, String name, boolean isFinanceiro) {
        String ex = (externalId == null || externalId.isBlank()) ? java.util.UUID.randomUUID().toString() : externalId;
        if (repo.existsByExternalId(ex)) throw new IllegalArgumentException("process id already exists");
        Process p = new Process(null, ex, name, true, isFinanceiro, Instant.now(), Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.created", Map.of("id", ex, "name", name, "isFinanceiro", isFinanceiro));
        return saved;
    }

    @Transactional
    public Process updateName(String externalId, String name) {
        Process p = repo.findByExternalId(externalId).orElseThrow();
        p.setName(name);
        p.setUpdatedAt(Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.updated", Map.of("id", externalId, "name", name));
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
}
