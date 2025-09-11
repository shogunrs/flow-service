package com.docheck.flow.application.service;

import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DataMigrationService {
    
    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private StageRepository stageRepo;
    
    public static class MigrationResult {
        private final boolean success;
        private final long updatedStageFields;
        private final long processedStages;
        private final long executionTimeMs;
        private final String error;
        
        public MigrationResult(boolean success, long updatedStageFields, long processedStages, 
                             long executionTimeMs, String error) {
            this.success = success;
            this.updatedStageFields = updatedStageFields;
            this.processedStages = processedStages;
            this.executionTimeMs = executionTimeMs;
            this.error = error;
        }
        
        public boolean success() { return success; }
        public long updatedStageFields() { return updatedStageFields; }
        public long processedStages() { return processedStages; }
        public long executionTimeMs() { return executionTimeMs; }
        public String error() { return error; }
    }
    
    /**
     * Migra dados existentes para incluir processExternalId nos StageFields
     */
    @Transactional
    public MigrationResult migrateStageFieldsWithProcessExternalId() {
        long startTime = System.currentTimeMillis();
        long updatedFields = 0;
        long processedStages = 0;
        
        try {
            log.info("🔄 Iniciando migração de dados: adicionando processExternalId aos StageFields");
            
            // 1. Encontrar todos os StageFields que não têm processExternalId
            Query findStageFieldsWithoutProcess = new Query(Criteria.where("processExternalId").exists(false));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stageFields = (List<Map<String, Object>>) (List<?>) 
                mongoTemplate.find(findStageFieldsWithoutProcess, Map.class, "stage_fields");
            
            log.info("📊 Encontrados {} StageFields sem processExternalId", stageFields.size());
            
            // 2. Para cada StageField, encontrar o Stage e buscar o processKey
            for (Map<String, Object> stageField : stageFields) {
                String stageFieldId = (String) stageField.get("_id");
                String stageId = (String) stageField.get("stageId");
                
                if (stageId != null) {
                    // Buscar Stage para obter processKey
                    Query stageQuery = new Query(Criteria.where("_id").is(stageId));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stageDoc = (Map<String, Object>) mongoTemplate.findOne(stageQuery, Map.class, "stages");
                    
                    if (stageDoc != null) {
                        String processExternalId = (String) stageDoc.get("processExternalId");
                        
                        if (processExternalId != null) {
                            // Atualizar o StageField com processExternalId
                            Query updateQuery = new Query(Criteria.where("_id").is(stageFieldId));
                            Update update = new Update().set("processExternalId", processExternalId);
                            
                            mongoTemplate.updateFirst(updateQuery, update, "stage_fields");
                            updatedFields++;
                            
                            log.debug("✅ StageField {} atualizado com processExternalId: {}", 
                                stageFieldId, processExternalId);
                        } else {
                            log.warn("⚠️ Stage {} não tem processExternalId definido", stageId);
                        }
                        processedStages++;
                    } else {
                        log.warn("⚠️ Stage {} não encontrado para StageField {}", stageId, stageFieldId);
                    }
                } else {
                    log.warn("⚠️ StageField {} não tem stageId definido", stageFieldId);
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("✅ Migração concluída em {}ms:", executionTime);
            log.info("   📊 {} StageFields atualizados", updatedFields);
            log.info("   🏗️ {} Stages processados", processedStages);
            
            return new MigrationResult(true, updatedFields, processedStages, executionTime, null);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("❌ Erro durante migração: {}", e.getMessage(), e);
            return new MigrationResult(false, updatedFields, processedStages, executionTime, e.getMessage());
        }
    }
    
    /**
     * Verifica quantos StageFields ainda precisam ser migrados
     */
    public long countStageFieldsNeedingMigration() {
        Query query = new Query(Criteria.where("processExternalId").exists(false));
        return mongoTemplate.count(query, "stage_fields");
    }
    
    /**
     * Verifica se todos os StageFields têm processExternalId
     */
    public boolean isMigrationComplete() {
        return countStageFieldsNeedingMigration() == 0;
    }
    
    /**
     * Valida a integridade dos dados após migração
     */
    public Map<String, Object> validateDataIntegrity() {
        try {
            // Contar total de StageFields
            long totalStageFields = mongoTemplate.count(new Query(), "stage_fields");
            
            // Contar StageFields com processExternalId
            Query withProcessId = new Query(Criteria.where("processExternalId").exists(true).ne(null));
            long fieldsWithProcessId = mongoTemplate.count(withProcessId, "stage_fields");
            
            // Contar StageFields órfãos (stageId que não existe)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allFields = (List<Map<String, Object>>) (List<?>) 
                mongoTemplate.findAll(Map.class, "stage_fields");
            
            long orphanedFields = 0;
            for (Map<String, Object> field : allFields) {
                String stageId = (String) field.get("stageId");
                if (stageId != null) {
                    Query stageQuery = new Query(Criteria.where("_id").is(stageId));
                    boolean stageExists = mongoTemplate.exists(stageQuery, "stages");
                    if (!stageExists) {
                        orphanedFields++;
                    }
                }
            }
            
            return Map.of(
                "totalStageFields", totalStageFields,
                "fieldsWithProcessId", fieldsWithProcessId,
                "fieldsWithoutProcessId", totalStageFields - fieldsWithProcessId,
                "orphanedFields", orphanedFields,
                "migrationComplete", fieldsWithProcessId == totalStageFields,
                "dataIntegrityOk", orphanedFields == 0
            );
            
        } catch (Exception e) {
            log.error("Erro ao validar integridade: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }
}