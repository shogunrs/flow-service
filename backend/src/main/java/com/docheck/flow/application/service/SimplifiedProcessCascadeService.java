package com.docheck.flow.application.service;

import com.docheck.flow.application.port.*;
import com.docheck.flow.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Versão SIMPLIFICADA do cascade deletion usando herança de processExternalId
 * Muito mais simples e eficiente que a versão anterior!
 */
@Service
public class SimplifiedProcessCascadeService {
    private static final Logger log = LoggerFactory.getLogger(SimplifiedProcessCascadeService.class);
    
    private final ProcessRepository processRepo;
    private final StageRepository stageRepo;
    private final ProposalRepository proposalRepo;
    private final StageFieldRepository stageFieldRepo;
    private final EventPublisher publisher;
    private final MongoTemplate mongoTemplate;
    
    public SimplifiedProcessCascadeService(
            ProcessRepository processRepo,
            StageRepository stageRepo, 
            ProposalRepository proposalRepo,
            StageFieldRepository stageFieldRepo,
            EventPublisher publisher,
            MongoTemplate mongoTemplate) {
        this.processRepo = processRepo;
        this.stageRepo = stageRepo;
        this.proposalRepo = proposalRepo;
        this.stageFieldRepo = stageFieldRepo;
        this.publisher = publisher;
        this.mongoTemplate = mongoTemplate;
    }
    
    /**
     * Resultado da operação de deleção cascade SIMPLIFICADA
     */
    public record SimplifiedCascadeResult(
            boolean success,
            String processKey,
            Map<String, Long> deletedCounts,
            List<String> errors,
            long totalDeletions,
            long executionTimeMs) {
    }
    
    /**
     * VERSÃO SIMPLIFICADA: Deleta processo usando herança de processExternalId
     * Muito mais simples que buscar por relacionamentos!
     */
    @Transactional
    public SimplifiedCascadeResult deleteProcessWithSimplifiedCascade(String processKey) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();
        Map<String, Long> deletedCounts = new HashMap<>();
        
        try {
            log.info("🚀 CASCATA SIMPLIFICADA: Deletando processo {} usando herança processExternalId", processKey);
            
            // 1. Verificar se processo existe
            Optional<com.docheck.flow.domain.model.Process> processOpt = processRepo.findByExternalId(processKey);
            if (processOpt.isEmpty()) {
                errors.add("Processo não encontrado: " + processKey);
                return new SimplifiedCascadeResult(false, processKey, deletedCounts, errors, 0, 
                    System.currentTimeMillis() - startTime);
            }
            
            // 2. DELEÇÃO SUPER SIMPLIFICADA usando processExternalId direto! 🎯
            
            // Deletar StageFields usando processExternalId (NOVO CAMPO!)
            List<StageField> stageFields = stageFieldRepo.findByProcessExternalId(processKey);
            if (!stageFields.isEmpty()) {
                stageFieldRepo.deleteByProcessExternalId(processKey);
                deletedCounts.put("stage_fields", (long) stageFields.size());
                log.info("✅ Deletados {} StageFields por processExternalId: {}", stageFields.size(), processKey);
            } else {
                deletedCounts.put("stage_fields", 0L);
            }
            
            // Deletar Stages usando processExternalId
            List<Stage> stages = stageRepo.findByProcessKey(processKey);
            if (!stages.isEmpty()) {
                stageRepo.deleteByProcessKey(processKey);
                deletedCounts.put("stages", (long) stages.size());
                log.info("✅ Deletados {} Stages por processExternalId: {}", stages.size(), processKey);
            } else {
                deletedCounts.put("stages", 0L);
            }
            
            // Deletar Proposals usando processExternalId
            List<Proposal> proposals = proposalRepo.findByProcessExternalId(processKey);
            if (!proposals.isEmpty()) {
                proposalRepo.deleteByProcessExternalId(processKey);
                deletedCounts.put("proposals", (long) proposals.size());
                log.info("✅ Deletadas {} Proposals por processExternalId: {}", proposals.size(), processKey);
            } else {
                deletedCounts.put("proposals", 0L);
            }
            
            // Deletar collections adicionais usando processExternalId/processKey direto
            deletedCounts.put("files", deleteCollectionByProcessKey("process_files", processKey));
            deletedCounts.put("activity_logs", deleteCollectionByProcessKey("activity_logs", processKey));
            deletedCounts.put("process_analytics", deleteCollectionByProcessKey("process_analytics", processKey));
            deletedCounts.put("process_settings", deleteCollectionByProcessKey("process_settings", processKey));
            deletedCounts.put("process_cache", deleteCollectionByProcessKey("process_cache", processKey));
            deletedCounts.put("process_notifications", deleteCollectionByProcessKey("process_notifications", processKey));
            
            // Deletar processo principal
            processRepo.deleteByExternalId(processKey);
            deletedCounts.put("processes", 1L);
            
            // Publicar evento
            publishSimplifiedCascadeDeleteEvent(processKey, deletedCounts);
            
            long executionTime = System.currentTimeMillis() - startTime;
            long totalDeletions = deletedCounts.values().stream().mapToLong(Long::longValue).sum();
            
            log.info("🎉 CASCATA SIMPLIFICADA concluída para {} em {}ms: {} registros deletados", 
                processKey, executionTime, totalDeletions);
                
            return new SimplifiedCascadeResult(true, processKey, deletedCounts, errors, 
                totalDeletions, executionTime);
                
        } catch (Exception e) {
            log.error("❌ Erro na cascata simplificada do processo {}: {}", processKey, e.getMessage(), e);
            errors.add("Erro interno: " + e.getMessage());
            
            return new SimplifiedCascadeResult(false, processKey, deletedCounts, errors, 0,
                System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * MUITO MAIS SIMPLES: deleta direto por processKey/processExternalId
     */
    private long deleteCollectionByProcessKey(String collectionName, String processKey) {
        try {
            Query query = Query.query(Criteria.where("processKey").is(processKey));
            long count = mongoTemplate.count(query, collectionName);
            
            if (count > 0) {
                mongoTemplate.remove(query, collectionName);
                log.debug("🗑️ Deletados {} registros de {} por processKey: {}", count, collectionName, processKey);
                return count;
            }
            
            return 0;
        } catch (Exception e) {
            log.warn("⚠️ Erro ao deletar de {}: {}", collectionName, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Publica evento da cascata simplificada
     */
    private void publishSimplifiedCascadeDeleteEvent(String processKey, Map<String, Long> deletedCounts) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("processKey", processKey);
        eventData.put("deletedCounts", deletedCounts);
        eventData.put("totalDeletions", deletedCounts.values().stream().mapToLong(Long::longValue).sum());
        eventData.put("cascadeType", "SIMPLIFIED");
        eventData.put("timestamp", Instant.now());
        
        publisher.publish("process.simplified.cascade.deleted", eventData);
    }
    
    /**
     * Conta quantos dados existem para um processo (para debugging)
     */
    public Map<String, Long> countDataForProcess(String processKey) {
        Map<String, Long> counts = new HashMap<>();
        
        try {
            // Contar dados usando processExternalId direto
            counts.put("stages", (long) stageRepo.findByProcessKey(processKey).size());
            counts.put("stageFields", (long) stageFieldRepo.findByProcessExternalId(processKey).size());
            counts.put("proposals", (long) proposalRepo.findByProcessExternalId(processKey).size());
            
            // Contar collections adicionais
            counts.put("files", mongoTemplate.count(Query.query(Criteria.where("processKey").is(processKey)), "process_files"));
            counts.put("logs", mongoTemplate.count(Query.query(Criteria.where("processKey").is(processKey)), "activity_logs"));
            counts.put("analytics", mongoTemplate.count(Query.query(Criteria.where("processKey").is(processKey)), "process_analytics"));
            
            return counts;
            
        } catch (Exception e) {
            log.error("Erro ao contar dados para processo {}: {}", processKey, e.getMessage());
            counts.put("error", -1L);
            return counts;
        }
    }
}