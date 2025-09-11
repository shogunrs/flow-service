package com.docheck.flow.application.service;

import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.application.port.ProposalRepository;
import com.docheck.flow.application.port.StageFieldRepository;
import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.domain.model.Stage;
import com.docheck.flow.domain.model.StageField;
import com.docheck.flow.domain.model.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço especializado em operações de deleção cascade para processos
 * com backup automático e auditoria completa.
 */
@Service
public class ProcessCascadeService {
    private static final Logger log = LoggerFactory.getLogger(ProcessCascadeService.class);

    private final ProcessRepository processRepo;
    private final StageRepository stageRepo;
    private final ProposalRepository proposalRepo;
    private final StageFieldRepository stageFieldRepo;
    private final EventPublisher publisher;
    private final MongoTemplate mongoTemplate;

    public ProcessCascadeService(
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
     * Resultado da operação de deleção cascade
     */
    public record CascadeDeleteResult(
            boolean success,
            String processKey,
            ProcessBackup backup,
            Map<String, Long> deletedCounts,
            List<String> errors,
            long totalDeletions,
            long executionTimeMs) {
    }

    /**
     * Backup completo de um processo e todos os dados relacionados
     */
    public record ProcessBackup(
            com.docheck.flow.domain.model.Process process,
            List<Stage> stages,
            List<StageField> stageFields,
            List<Proposal> proposals,
            Map<String, Object> additionalCollections,
            Instant backupTimestamp,
            String backupId) {
    }

    /**
     * Deleta um processo com cascade completo, backup automático e auditoria
     */
    @Transactional
    public CascadeDeleteResult deleteProcessWithCascade(String processKey, boolean createBackup) {
        long startTime = System.currentTimeMillis();
        String backupId = UUID.randomUUID().toString();
        List<String> errors = new ArrayList<>();
        Map<String, Long> deletedCounts = new HashMap<>();
        final ProcessBackup[] backupRef = {null};

        try {
            log.info("🗑️ Iniciando deleção cascade para processo: {}", processKey);

            // 1. Verificar se processo existe
            Optional<com.docheck.flow.domain.model.Process> processOpt = processRepo.findByExternalId(processKey);
            if (processOpt.isEmpty()) {
                errors.add("Processo não encontrado: " + processKey);
                return new CascadeDeleteResult(false, processKey, null, deletedCounts, errors, 0,
                        System.currentTimeMillis() - startTime);
            }

            com.docheck.flow.domain.model.Process process = processOpt.get();

            // 2. Criar backup completo se solicitado
            if (createBackup) {
                log.info("💾 Criando backup completo do processo {}", processKey);
                backupRef[0] = createProcessBackup(process, backupId);

                // Salvar backup em collection separada para recovery
                saveBackupToMongo(backupRef[0]);

                log.info("✅ Backup criado: {} itens salvos", getTotalBackupItems(backupRef[0]));
            }

            // 3. Executar deleções em cascade com contagem
            deletedCounts = executeProcessCascadeDeletion(processKey, process.getId());

            // 4. Publicar evento de auditoria
            publishCascadeDeleteEvent(processKey, backupRef[0], deletedCounts);

            long executionTime = System.currentTimeMillis() - startTime;
            long totalDeletions = deletedCounts.values().stream().mapToLong(Long::longValue).sum();

            log.info("✅ Deleção cascade concluída para processo {} em {}ms. Total: {} registros deletados",
                    processKey, executionTime, totalDeletions);

            return new CascadeDeleteResult(true, processKey, backupRef[0], deletedCounts, errors,
                    totalDeletions, executionTime);

        } catch (Exception e) {
            log.error("❌ Erro durante deleção cascade do processo {}: {}", processKey, e.getMessage(), e);
            errors.add("Erro interno: " + e.getMessage());

            // Se falhou, tentar restaurar backup se existir
            if (backupRef[0] != null) {
                log.warn("🔄 Tentando restaurar backup devido ao erro...");
                CompletableFuture.runAsync(() -> attemptBackupRestore(backupRef[0]));
            }

            return new CascadeDeleteResult(false, processKey, backupRef[0], deletedCounts, errors, 0,
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Cria backup completo de um processo
     */
    private ProcessBackup createProcessBackup(com.docheck.flow.domain.model.Process process, String backupId) {
        String processKey = process.getExternalId();

        // Buscar todos os dados relacionados
        List<Stage> stages = stageRepo.findByProcessKey(processKey);
        List<String> stageIds = stages.stream().map(Stage::getId).toList();

        List<StageField> stageFields = new ArrayList<>();
        for (String stageId : stageIds) {
            stageFields.addAll(stageFieldRepo.findByStageId(stageId));
        }

        List<Proposal> proposals = proposalRepo.findByProcessExternalId(processKey);

        // Buscar dados de collections adicionais que possam existir
        Map<String, Object> additionalCollections = new HashMap<>();

        // Files relacionados ao processo
        try {
            Query fileQuery = Query.query(Criteria.where("processKey").is(processKey));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> files = (List<Map<String, Object>>) (List<?>) mongoTemplate.find(fileQuery, Map.class, "process_files");
            if (!files.isEmpty()) {
                additionalCollections.put("files", files);
            }
        } catch (Exception e) {
            log.warn("Erro ao fazer backup de arquivos: {}", e.getMessage());
        }

        // Activity logs
        try {
            Query logQuery = Query.query(Criteria.where("processKey").is(processKey));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> logs = (List<Map<String, Object>>) (List<?>) mongoTemplate.find(logQuery, Map.class, "activity_logs");
            if (!logs.isEmpty()) {
                additionalCollections.put("activity_logs", logs);
            }
        } catch (Exception e) {
            log.warn("Erro ao fazer backup de logs: {}", e.getMessage());
        }

        // Analytics data
        try {
            Query analyticsQuery = Query.query(Criteria.where("processKey").is(processKey));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> analytics = (List<Map<String, Object>>) (List<?>) mongoTemplate.find(analyticsQuery, Map.class, "process_analytics");
            if (!analytics.isEmpty()) {
                additionalCollections.put("analytics", analytics);
            }
        } catch (Exception e) {
            log.warn("Erro ao fazer backup de analytics: {}", e.getMessage());
        }

        return new ProcessBackup(
                process,
                stages,
                stageFields,
                proposals,
                additionalCollections,
                Instant.now(),
                backupId);
    }

    /**
     * Executa as deleções em cascade com contagem precisa
     */
    private Map<String, Long> executeProcessCascadeDeletion(String processKey, String processId) {
        Map<String, Long> counts = new HashMap<>();

        // 1. Buscar todos os stages do processo
        List<Stage> stages = stageRepo.findByProcessKey(processKey);
        List<String> stageIds = stages.stream().map(Stage::getId).toList();

        log.debug("📊 Encontrados {} estágios para deletar", stages.size());

        // 2. Deletar stage fields de todos os stages
        long stageFieldsDeleted = 0;
        for (String stageId : stageIds) {
            List<StageField> fields = stageFieldRepo.findByStageId(stageId);
            if (!fields.isEmpty()) {
                stageFieldRepo.deleteByStageId(stageId);
                stageFieldsDeleted += fields.size();
                log.debug("🗑️ Deletados {} campos do estágio {}", fields.size(), stageId);
            }
        }
        counts.put("stage_fields", stageFieldsDeleted);

        // 3. Deletar todas as proposals do processo
        List<Proposal> proposals = proposalRepo.findByProcessExternalId(processKey);
        if (!proposals.isEmpty()) {
            // Em cada proposal pode haver form data embeddados, eles vão junto
            proposalRepo.deleteByProcessExternalId(processKey);
            counts.put("proposals", (long) proposals.size());
            log.debug("🗑️ Deletadas {} propostas", proposals.size());
        } else {
            counts.put("proposals", 0L);
        }

        // 4. Deletar collections relacionadas usando MongoTemplate
        counts.put("files", deleteFromCollection("process_files", "processKey", processKey));
        counts.put("activity_logs", deleteFromCollection("activity_logs", "processKey", processKey));
        counts.put("process_analytics", deleteFromCollection("process_analytics", "processKey", processKey));
        counts.put("process_settings", deleteFromCollection("process_settings", "processKey", processKey));
        counts.put("process_cache", deleteFromCollection("process_cache", "processKey", processKey));
        counts.put("process_notifications", deleteFromCollection("process_notifications", "processKey", processKey));

        // 5. Deletar estágios
        if (!stages.isEmpty()) {
            stageRepo.deleteByProcessKey(processKey);
            counts.put("stages", (long) stages.size());
            log.debug("🗑️ Deletados {} estágios", stages.size());
        } else {
            counts.put("stages", 0L);
        }

        // 6. Finalmente deletar o processo principal
        processRepo.deleteByExternalId(processKey);
        counts.put("processes", 1L);
        log.debug("🗑️ Processo principal deletado");

        return counts;
    }

    /**
     * Deleta registros de uma collection por critério
     */
    private long deleteFromCollection(String collectionName, String fieldName, String fieldValue) {
        try {
            Query query = Query.query(Criteria.where(fieldName).is(fieldValue));
            long count = mongoTemplate.count(query, collectionName);

            if (count > 0) {
                mongoTemplate.remove(query, collectionName);
                log.debug("🗑️ Deletados {} registros de {}", count, collectionName);
                return count;
            }

            return 0;
        } catch (Exception e) {
            log.warn("⚠️ Erro ao deletar de {}: {}", collectionName, e.getMessage());
            return 0;
        }
    }

    /**
     * Salva backup no MongoDB para recovery
     */
    private void saveBackupToMongo(ProcessBackup backup) {
        try {
            Map<String, Object> backupDoc = new HashMap<>();
            backupDoc.put("_id", backup.backupId());
            backupDoc.put("processKey", backup.process().getExternalId());
            backupDoc.put("processName", backup.process().getName());
            backupDoc.put("backupTimestamp", backup.backupTimestamp());
            backupDoc.put("process", backup.process());
            backupDoc.put("stages", backup.stages());
            backupDoc.put("stageFields", backup.stageFields());
            backupDoc.put("proposals", backup.proposals());
            backupDoc.put("additionalCollections", backup.additionalCollections());
            backupDoc.put("totalItems", getTotalBackupItems(backup));
            backupDoc.put("version", "1.0");

            mongoTemplate.save(backupDoc, "process_backups");
            log.info("💾 Backup salvo na collection process_backups com ID: {}", backup.backupId());

        } catch (Exception e) {
            log.error("❌ Erro ao salvar backup no MongoDB: {}", e.getMessage(), e);
        }
    }

    /**
     * Conta total de itens no backup
     */
    private int getTotalBackupItems(ProcessBackup backup) {
        if (backup == null)
            return 0;

        int total = 1; // processo principal
        total += backup.stages().size();
        total += backup.stageFields().size();
        total += backup.proposals().size();

        for (Object value : backup.additionalCollections().values()) {
            if (value instanceof Collection<?> collection) {
                total += collection.size();
            }
        }

        return total;
    }

    /**
     * Publica evento de auditoria da deleção cascade
     */
    private void publishCascadeDeleteEvent(String processKey, ProcessBackup backup, Map<String, Long> deletedCounts) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("processKey", processKey);
        eventData.put("deletedCounts", deletedCounts);
        eventData.put("totalDeletions", deletedCounts.values().stream().mapToLong(Long::longValue).sum());
        eventData.put("backupCreated", backup != null);

        if (backup != null) {
            eventData.put("backupId", backup.backupId());
            eventData.put("backupTimestamp", backup.backupTimestamp());
            eventData.put("backupItems", getTotalBackupItems(backup));
        }

        eventData.put("timestamp", Instant.now());

        publisher.publish("process.cascade.deleted", eventData);
    }

    /**
     * Tentativa de restaurar backup em caso de erro
     */
    private void attemptBackupRestore(ProcessBackup backup) {
        try {
            log.info("🔄 Tentando restaurar processo {} do backup {}",
                    backup.process().getExternalId(), backup.backupId());

            // Restaurar processo principal
            processRepo.save(backup.process());

            // Restaurar estágios
            for (Stage stage : backup.stages()) {
                stageRepo.save(stage);
            }

            // Restaurar stage fields
            for (StageField field : backup.stageFields()) {
                stageFieldRepo.save(field);
            }

            // Restaurar propostas
            for (Proposal proposal : backup.proposals()) {
                proposalRepo.save(proposal);
            }

            // Restaurar collections adicionais
            for (Map.Entry<String, Object> entry : backup.additionalCollections().entrySet()) {
                if (entry.getValue() instanceof Collection<?> items) {
                    for (Object item : items) {
                        mongoTemplate.save(item, entry.getKey());
                    }
                }
            }

            log.info("✅ Backup restaurado com sucesso");

        } catch (Exception e) {
            log.error("❌ Falha ao restaurar backup: {}", e.getMessage(), e);
        }
    }

    /**
     * Lista todos os backups disponíveis
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listBackups() {
        try {
            return (List<Map<String, Object>>) (List<?>) mongoTemplate.findAll(Map.class, "process_backups");
        } catch (Exception e) {
            log.error("Erro ao listar backups: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Busca um backup específico
     */
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> getBackup(String backupId) {
        try {
            Query query = Query.query(Criteria.where("_id").is(backupId));
            Map<String, Object> backup = (Map<String, Object>) mongoTemplate.findOne(query, Map.class, "process_backups");
            return Optional.ofNullable(backup);
        } catch (Exception e) {
            log.error("Erro ao buscar backup {}: {}", backupId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Remove backups antigos (limpeza de manutenção)
     */
    public long cleanupOldBackups(int daysToKeep) {
        try {
            Instant cutoffDate = Instant.now().minusSeconds(daysToKeep * 24L * 60 * 60);
            Query query = Query.query(Criteria.where("backupTimestamp").lt(cutoffDate));

            long count = mongoTemplate.count(query, "process_backups");
            if (count > 0) {
                mongoTemplate.remove(query, "process_backups");
                log.info("🧹 Removidos {} backups antigos (>{} dias)", count, daysToKeep);
            }

            return count;
        } catch (Exception e) {
            log.error("Erro ao limpar backups antigos: {}", e.getMessage());
            return 0;
        }
    }
}