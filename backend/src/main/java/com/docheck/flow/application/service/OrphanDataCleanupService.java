package com.docheck.flow.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrphanDataCleanupService {
    
    private static final Logger log = LoggerFactory.getLogger(OrphanDataCleanupService.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public static class OrphanCleanupResult {
        private final boolean success;
        private final Map<String, Long> deletedCounts;
        private final List<String> orphanedProcessKeys;
        private final long executionTimeMs;
        private final List<String> errors;
        
        public OrphanCleanupResult(boolean success, Map<String, Long> deletedCounts, 
                                 List<String> orphanedProcessKeys, long executionTimeMs, 
                                 List<String> errors) {
            this.success = success;
            this.deletedCounts = deletedCounts;
            this.orphanedProcessKeys = orphanedProcessKeys;
            this.executionTimeMs = executionTimeMs;
            this.errors = errors;
        }
        
        public boolean success() { return success; }
        public Map<String, Long> deletedCounts() { return deletedCounts; }
        public List<String> orphanedProcessKeys() { return orphanedProcessKeys; }
        public long executionTimeMs() { return executionTimeMs; }
        public List<String> errors() { return errors; }
    }
    
    @Transactional
    public OrphanCleanupResult cleanupAllOrphanData() {
        long startTime = System.currentTimeMillis();
        Map<String, Long> deletedCounts = new HashMap<>();
        List<String> orphanedProcessKeys = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            log.info("🧹 Iniciando limpeza de dados órfãos...");
            
            // 1. Encontrar todos os processKeys válidos
            Set<String> validProcessKeys = findValidProcessKeys();
            log.info("✅ Encontrados {} processos válidos", validProcessKeys.size());
            
            // 2. Limpar Stages órfãos
            long deletedStages = cleanupOrphanStages(validProcessKeys, orphanedProcessKeys);
            deletedCounts.put("stages", deletedStages);
            
            // 3. Limpar StageFields órfãos (precisamos dos stages válidos agora)
            Set<String> validStageIds = findValidStageIds();
            long deletedStageFields = cleanupOrphanStageFields(validStageIds, orphanedProcessKeys);
            deletedCounts.put("stageFields", deletedStageFields);
            
            // 4. Limpar Proposals órfãs
            long deletedProposals = cleanupOrphanProposals(validProcessKeys, orphanedProcessKeys);
            deletedCounts.put("proposals", deletedProposals);
            
            // 5. Limpar outras collections relacionadas
            long deletedFiles = cleanupOrphanCollection("process_files", "processKey", validProcessKeys);
            deletedCounts.put("files", deletedFiles);
            
            long deletedLogs = cleanupOrphanCollection("activity_logs", "processKey", validProcessKeys);
            deletedCounts.put("logs", deletedLogs);
            
            long deletedAnalytics = cleanupOrphanCollection("process_analytics", "processKey", validProcessKeys);
            deletedCounts.put("analytics", deletedAnalytics);
            
            long totalDeleted = deletedCounts.values().stream().mapToLong(Long::longValue).sum();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("✅ Limpeza concluída: {} registros órfãos removidos em {}ms", totalDeleted, executionTime);
            log.info("📊 Detalhes: {}", deletedCounts);
            if (!orphanedProcessKeys.isEmpty()) {
                log.warn("🔍 ProcessKeys órfãos encontrados: {}", orphanedProcessKeys.size());
            }
            
            return new OrphanCleanupResult(true, deletedCounts, orphanedProcessKeys, executionTime, errors);
            
        } catch (Exception e) {
            errors.add("Erro na limpeza: " + e.getMessage());
            log.error("❌ Erro na limpeza de órfãos", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return new OrphanCleanupResult(false, deletedCounts, orphanedProcessKeys, executionTime, errors);
        }
    }
    
    private Set<String> findValidProcessKeys() {
        Query query = new Query();
        query.fields().include("externalId");
        
        List<Map> processes = mongoTemplate.find(query, Map.class, "processes");
        return processes.stream()
                .map(p -> (String) p.get("externalId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    private Set<String> findValidStageIds() {
        Query query = new Query();
        query.fields().include("id");
        
        List<Map> stages = mongoTemplate.find(query, Map.class, "stages");
        return stages.stream()
                .map(s -> (String) s.get("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    private long cleanupOrphanStages(Set<String> validProcessKeys, List<String> orphanedProcessKeys) {
        Query findOrphansQuery = new Query();
        if (!validProcessKeys.isEmpty()) {
            findOrphansQuery.addCriteria(Criteria.where("processExternalId").nin(validProcessKeys));
        }
        
        List<Map> orphanStages = mongoTemplate.find(findOrphansQuery, Map.class, "stages");
        
        // Coletar processKeys órfãos únicos
        Set<String> uniqueOrphanKeys = orphanStages.stream()
                .map(s -> (String) s.get("processExternalId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        orphanedProcessKeys.addAll(uniqueOrphanKeys);
        
        if (orphanStages.isEmpty()) {
            log.info("✅ Nenhum Stage órfão encontrado");
            return 0;
        }
        
        long deleted = mongoTemplate.remove(findOrphansQuery, "stages").getDeletedCount();
        log.info("🗑️ Removidos {} Stages órfãos", deleted);
        return deleted;
    }
    
    private long cleanupOrphanStageFields(Set<String> validStageIds, List<String> orphanedProcessKeys) {
        Query findOrphansQuery = new Query();
        if (!validStageIds.isEmpty()) {
            findOrphansQuery.addCriteria(Criteria.where("stageId").nin(validStageIds));
        }
        
        List<Map> orphanFields = mongoTemplate.find(findOrphansQuery, Map.class, "stage_fields");
        
        if (orphanFields.isEmpty()) {
            log.info("✅ Nenhum StageField órfão encontrado");
            return 0;
        }
        
        long deleted = mongoTemplate.remove(findOrphansQuery, "stage_fields").getDeletedCount();
        log.info("🗑️ Removidos {} StageFields órfãos", deleted);
        return deleted;
    }
    
    private long cleanupOrphanProposals(Set<String> validProcessKeys, List<String> orphanedProcessKeys) {
        Query findOrphansQuery = new Query();
        if (!validProcessKeys.isEmpty()) {
            findOrphansQuery.addCriteria(Criteria.where("processExternalId").nin(validProcessKeys));
        }
        
        List<Map> orphanProposals = mongoTemplate.find(findOrphansQuery, Map.class, "proposals");
        
        // Coletar processKeys órfãos únicos de proposals
        Set<String> uniqueOrphanKeys = orphanProposals.stream()
                .map(p -> (String) p.get("processExternalId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        orphanedProcessKeys.addAll(uniqueOrphanKeys);
        
        if (orphanProposals.isEmpty()) {
            log.info("✅ Nenhuma Proposal órfã encontrada");
            return 0;
        }
        
        long deleted = mongoTemplate.remove(findOrphansQuery, "proposals").getDeletedCount();
        log.info("🗑️ Removidas {} Proposals órfãs", deleted);
        return deleted;
    }
    
    private long cleanupOrphanCollection(String collectionName, String keyField, Set<String> validKeys) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            log.debug("Collection '{}' não existe, pulando...", collectionName);
            return 0;
        }
        
        Query findOrphansQuery = new Query();
        if (!validKeys.isEmpty()) {
            findOrphansQuery.addCriteria(Criteria.where(keyField).nin(validKeys));
        }
        
        long deleted = mongoTemplate.remove(findOrphansQuery, collectionName).getDeletedCount();
        if (deleted > 0) {
            log.info("🗑️ Removidos {} registros órfãos de '{}'", deleted, collectionName);
        } else {
            log.debug("✅ Nenhum registro órfão em '{}'", collectionName);
        }
        return deleted;
    }
    
    public OrphanCleanupResult findOrphanDataOnly() {
        long startTime = System.currentTimeMillis();
        Map<String, Long> orphanCounts = new HashMap<>();
        List<String> orphanedProcessKeys = new ArrayList<>();
        
        try {
            Set<String> validProcessKeys = findValidProcessKeys();
            Set<String> validStageIds = findValidStageIds();
            
            // Contar órfãos sem deletar
            orphanCounts.put("stages", countOrphans("stages", "processExternalId", validProcessKeys));
            orphanCounts.put("stageFields", countOrphans("stage_fields", "stageId", validStageIds));
            orphanCounts.put("proposals", countOrphans("proposals", "processExternalId", validProcessKeys));
            
            // Encontrar processKeys órfãos
            Query stageOrphansQuery = new Query(Criteria.where("processExternalId").nin(validProcessKeys));
            List<Map> orphanStages = mongoTemplate.find(stageOrphansQuery, Map.class, "stages");
            orphanedProcessKeys.addAll(orphanStages.stream()
                    .map(s -> (String) s.get("processExternalId"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList()));
            
            Query proposalOrphansQuery = new Query(Criteria.where("processExternalId").nin(validProcessKeys));
            List<Map> orphanProposals = mongoTemplate.find(proposalOrphansQuery, Map.class, "proposals");
            orphanedProcessKeys.addAll(orphanProposals.stream()
                    .map(p -> (String) p.get("processExternalId"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList()));
            
            orphanedProcessKeys = orphanedProcessKeys.stream().distinct().collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            return new OrphanCleanupResult(true, orphanCounts, orphanedProcessKeys, executionTime, new ArrayList<>());
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new OrphanCleanupResult(false, orphanCounts, orphanedProcessKeys, executionTime, List.of(e.getMessage()));
        }
    }
    
    private long countOrphans(String collectionName, String keyField, Set<String> validKeys) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            return 0;
        }
        
        Query query = new Query();
        if (!validKeys.isEmpty()) {
            query.addCriteria(Criteria.where(keyField).nin(validKeys));
        }
        
        return mongoTemplate.count(query, collectionName);
    }
}