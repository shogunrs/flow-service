package com.docheck.flow.api;

import com.docheck.flow.api.dto.ProcessDTO;
import com.docheck.flow.api.mapper.ProcessMapper;
import com.docheck.flow.application.service.ProcessService;
import com.docheck.flow.application.service.ProcessCascadeService;
import com.docheck.flow.application.service.OrphanDataCleanupService;
import com.docheck.flow.domain.model.Process;
import com.docheck.flow.application.port.ProposalRepository;
import com.docheck.flow.application.port.StageFieldRepository;
import com.docheck.flow.infrastructure.storage.FileStorageService;
import com.docheck.flow.domain.model.Proposal;
import com.docheck.flow.domain.model.StageField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/processes")
public class ProcessController {
    private final ProcessService service;
    private final ProcessCascadeService cascadeService;
    private final OrphanDataCleanupService cleanupService;
    private final ProposalRepository proposalRepository;
    private final StageFieldRepository stageFieldRepository;
    private final FileStorageService fileStorageService;

    public ProcessController(ProcessService service, ProcessCascadeService cascadeService, 
                           OrphanDataCleanupService cleanupService, ProposalRepository proposalRepository,
                           StageFieldRepository stageFieldRepository, FileStorageService fileStorageService) { 
        this.service = service;
        this.cascadeService = cascadeService;
        this.cleanupService = cleanupService;
        this.proposalRepository = proposalRepository;
        this.stageFieldRepository = stageFieldRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<ProcessDTO> list() {
        return service.list().stream().map(ProcessMapper::toDto).toList();
    }

    @GetMapping("/{key}")
    public ResponseEntity<ProcessDTO> get(@PathVariable("key") String externalId) {
        return service.get(externalId).map(p -> ResponseEntity.ok(ProcessMapper.toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateRequest(@NotBlank String key, @NotBlank String name, String type, Boolean isFinanceiro) {}

    @PostMapping
    public ResponseEntity<ProcessDTO> create(@Valid @RequestBody CreateRequest req) {
        Process.ProcessType type = Process.ProcessType.fromString(req.type());
        if (type == Process.ProcessType.GENERIC && Boolean.TRUE.equals(req.isFinanceiro())) {
            type = Process.ProcessType.FINANCIAL;
        }
        Process created = service.create(req.key(), req.name(), type);
        return ResponseEntity.created(URI.create("/api/v1/processes/" + created.getExternalId()))
                .body(ProcessMapper.toDto(created));
    }

    public record UpdateNameRequest(@NotBlank String name) {}

    @PutMapping("/{key}")
    public ProcessDTO updateName(@PathVariable("key") String externalId, @Valid @RequestBody UpdateNameRequest req) {
        return ProcessMapper.toDto(service.updateName(externalId, req.name()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable("key") String externalId,
            @RequestParam(value = "backup", defaultValue = "true") boolean createBackup,
            @RequestParam(value = "cascade", defaultValue = "true") boolean useCascade) {
        
        if (useCascade) {
            // Usar deleção cascade completa com backup
            ProcessCascadeService.CascadeDeleteResult result = 
                cascadeService.deleteProcessWithCascade(externalId, createBackup);
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "processKey", result.processKey(), 
                "totalDeletions", result.totalDeletions(),
                "executionTimeMs", result.executionTimeMs(),
                "deletedCounts", result.deletedCounts(),
                "backupCreated", result.backup() != null,
                "backupId", result.backup() != null ? result.backup().backupId() : null,
                "errors", result.errors()
            );
            
            return result.success() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(500).body(response);
        } else {
            // Usar método legacy (compatibilidade)
            service.delete(externalId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "processKey", externalId,
                "method", "legacy",
                "cascadeComplete", false,
                "message", "Processo deletado usando método básico (sem cascade completo)"
            );
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Endpoint para listar backups disponíveis
     */
    @GetMapping("/{key}/backups")
    public ResponseEntity<List<Map<String, Object>>> listBackups(@PathVariable("key") String processKey) {
        List<Map<String, Object>> backups = cascadeService.listBackups().stream()
            .filter(backup -> processKey.equals(backup.get("processKey")))
            .toList();
        
        return ResponseEntity.ok(backups);
    }
    
    /**
     * Endpoint para buscar um backup específico
     */
    @GetMapping("/backups/{backupId}")
    public ResponseEntity<Map<String, Object>> getBackup(@PathVariable("backupId") String backupId) {
        return cascadeService.getBackup(backupId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Endpoint para limpeza de backups antigos (manutenção)
     */
    @DeleteMapping("/backups/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldBackups(
            @RequestParam(value = "daysToKeep", defaultValue = "30") int daysToKeep) {
        
        long cleanedCount = cascadeService.cleanupOldBackups(daysToKeep);
        
        Map<String, Object> response = Map.of(
            "success", true,
            "cleanedBackups", cleanedCount,
            "daysToKeep", daysToKeep,
            "message", "Limpeza de backups concluída"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para analisar dados órfãos no banco (sem deletar)
     */
    @GetMapping("/orphans/analyze")
    public ResponseEntity<Map<String, Object>> analyzeOrphanData() {
        OrphanDataCleanupService.OrphanCleanupResult result = cleanupService.findOrphanDataOnly();
        
        Map<String, Object> response = Map.of(
            "success", result.success(),
            "orphanCounts", result.deletedCounts(), // Na verdade são os counts encontrados
            "orphanedProcessKeys", result.orphanedProcessKeys(),
            "executionTimeMs", result.executionTimeMs(),
            "errors", result.errors(),
            "message", "Análise concluída - dados não foram deletados"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para buscar arquivos de um processo específico
     */
    @GetMapping("/{key}/files")
    public ResponseEntity<List<Map<String, Object>>> getProcessFiles(@PathVariable("key") String processKey) {
        try {
            // Buscar todas as propostas do processo
            List<Proposal> proposals = proposalRepository.findByProcessExternalId(processKey);
            
            if (proposals.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            // Buscar todos os campos do processo para obter labels e tipos
            List<StageField> allStageFields = stageFieldRepository.findByProcessExternalId(processKey);
            
            // Mapear campos por stageId e fieldId para acesso rápido
            Map<String, Map<String, StageField>> fieldsByStageAndId = new HashMap<>();
            for (StageField field : allStageFields) {
                if (field.getStageId() != null && field.getId() != null) {
                    fieldsByStageAndId.computeIfAbsent(field.getStageId(), k -> new HashMap<>())
                                     .put(field.getId(), field);
                }
            }
            
            List<Map<String, Object>> allFiles = new ArrayList<>();
            
            // Processar cada proposta
            for (Proposal proposal : proposals) {
                try {
                    Map<String, Map<String, Object>> forms = proposal.getForms();
                    String proposalId = proposal.getId();
                    Object updatedAt = proposal.getUpdatedAt();
                    String proposalExternalId = proposalId; // Usando proposalId como fallback
                    
                    if (forms != null && !forms.isEmpty()) {
                        // Percorrer todas as stages nos forms
                        for (Map.Entry<String, Map<String, Object>> stageEntry : forms.entrySet()) {
                            String stageId = stageEntry.getKey();
                            Map<String, Object> stageForm = stageEntry.getValue();
                            
                            if (stageForm != null && !stageForm.isEmpty()) {
                                // Buscar os campos desta stage
                                Map<String, StageField> stageFields = fieldsByStageAndId.get(stageId);
                                
                                // Percorrer todos os campos da stage
                                for (Map.Entry<String, Object> fieldEntry : stageForm.entrySet()) {
                                    String fieldId = fieldEntry.getKey();
                                    Object fieldValue = fieldEntry.getValue();
                                    
                                    // Buscar informações do campo no StageField
                                    StageField fieldInfo = null;
                                    if (stageFields != null) {
                                        fieldInfo = stageFields.get(fieldId);
                                    }
                                    
                                    // Verificar se é um campo de arquivo e se tem valor
                                    if (fieldInfo != null && 
                                        "file".equalsIgnoreCase(fieldInfo.getType()) && 
                                        fieldValue instanceof String && 
                                        !((String) fieldValue).trim().isEmpty()) {
                                        
                                        String objectKey = (String) fieldValue;
                                        
                                        // Validar se realmente parece um objectKey (contém path)
                                        if (objectKey.contains("/") && objectKey.length() > 10) {
                                            
                                            // Extrair nome original do arquivo
                                            String[] parts = objectKey.split("/");
                                            String fileNameWithTimestamp = parts[parts.length - 1];
                                            String originalName = extractOriginalFileName(fileNameWithTimestamp);
                                            
                                            // Inferir tipo de conteúdo
                                            String contentType = inferContentType(originalName);
                                            
                                            // Obter informações do campo
                                            String fieldLabel = fieldInfo.getLabel() != null ? 
                                                              fieldInfo.getLabel() : 
                                                              "Campo " + fieldId;
                                            String fieldDescription = ""; // Descrição não disponível no modelo atual
                                            
                                            // Gerar URL de download
                                            String downloadUrl = null;
                                            try {
                                                Map<String, Object> presignResult = fileStorageService.presignDownload(objectKey);
                                                downloadUrl = (String) presignResult.get("url");
                                            } catch (Exception e) {
                                                System.err.println("Erro ao gerar URL de download para: " + objectKey + " - " + e.getMessage());
                                            }
                                            
                                            // Criar objeto de arquivo
                                            Map<String, Object> file = new HashMap<>();
                                            file.put("id", proposalId + "_" + stageId + "_" + fieldId);
                                            file.put("objectKey", objectKey);
                                            file.put("fileName", fileNameWithTimestamp);
                                            file.put("originalName", originalName);
                                            file.put("fileSize", 0); // Não temos essa informação
                                            file.put("contentType", contentType);
                                            file.put("uploadedAt", updatedAt != null ? updatedAt.toString() : null);
                                            file.put("proposalId", proposalId);
                                            file.put("proposalExternalId", proposalExternalId);
                                            file.put("stageId", stageId);
                                            file.put("fieldId", fieldId);
                                            file.put("fieldLabel", fieldLabel);
                                            file.put("fieldDescription", fieldDescription);
                                            file.put("fieldType", fieldInfo.getType());
                                            file.put("downloadUrl", downloadUrl);
                                            file.put("approvalStatus", "pending"); // Status padrão
                                            
                                            allFiles.add(file);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar proposta " + proposal.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Ordenar arquivos por data de upload (mais recente primeiro)
            allFiles.sort((a, b) -> {
                try {
                    String dateA = (String) a.get("uploadedAt");
                    String dateB = (String) b.get("uploadedAt");
                    if (dateA != null && dateB != null) {
                        return dateB.compareTo(dateA); // Ordem decrescente
                    }
                    return 0;
                } catch (Exception e) {
                    return 0;
                }
            });
            
            return ResponseEntity.ok(allFiles);
            
        } catch (Exception e) {
            System.err.println("Erro geral ao buscar arquivos do processo " + processKey + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    
    /**
     * Extrai o nome original removendo timestamp
     */
    private String extractOriginalFileName(String fileNameWithTimestamp) {
        // Remove timestamp no formato: "1757345494592_NOME_ARQUIVO.ext"
        if (fileNameWithTimestamp.matches("^\\d+_(.+)$")) {
            return fileNameWithTimestamp.replaceFirst("^\\d+_", "");
        }
        return fileNameWithTimestamp;
    }
    
    /**
     * Infere o tipo de conteúdo baseado na extensão do arquivo
     */
    private String inferContentType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "csv" -> "text/csv";
            case "xlsx", "xls" -> "application/vnd.ms-excel";
            case "doc", "docx" -> "application/msword";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    /**
     * Endpoint para limpar todos os dados órfãos do banco
     */
    @DeleteMapping("/orphans/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOrphanData() {
        OrphanDataCleanupService.OrphanCleanupResult result = cleanupService.cleanupAllOrphanData();
        
        Map<String, Object> response = Map.of(
            "success", result.success(),
            "totalDeletions", result.deletedCounts().values().stream().mapToLong(Long::longValue).sum(),
            "deletedCounts", result.deletedCounts(),
            "orphanedProcessKeys", result.orphanedProcessKeys(),
            "executionTimeMs", result.executionTimeMs(),
            "errors", result.errors(),
            "message", result.success() ? "Limpeza de órfãos concluída com sucesso" : "Erro na limpeza de órfãos"
        );
        
        return result.success() ? 
            ResponseEntity.ok(response) : 
            ResponseEntity.status(500).body(response);
    }
}
