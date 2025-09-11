package com.docheck.flow.api;

import com.docheck.flow.application.service.ProcessCascadeService;
import com.docheck.flow.application.service.OrphanDataCleanupService;
import com.docheck.flow.application.service.DataMigrationService;
import com.docheck.flow.application.service.SimplifiedProcessCascadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    
    @Autowired
    private ProcessCascadeService cascadeService;
    
    @Autowired
    private OrphanDataCleanupService cleanupService;
    
    @Autowired
    private DataMigrationService migrationService;
    
    @Autowired
    private SimplifiedProcessCascadeService simplifiedCascadeService;
    
    /**
     * Endpoint para testar a deleção cascade
     */
    @DeleteMapping("/cascade/{processKey}")
    public ResponseEntity<Map<String, Object>> testCascadeDelete(@PathVariable String processKey) {
        try {
            ProcessCascadeService.CascadeDeleteResult result = 
                cascadeService.deleteProcessWithCascade(processKey, true);
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "processKey", result.processKey(),
                "totalDeletions", result.totalDeletions(),
                "deletedCounts", result.deletedCounts(),
                "executionTimeMs", result.executionTimeMs(),
                "errors", result.errors(),
                "backupCreated", result.backup() != null
            );
            
            return result.success() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(500).body(response);
                
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage(),
                "processKey", processKey
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para analisar dados órfãos
     */
    @GetMapping("/orphans")
    public ResponseEntity<Map<String, Object>> analyzeOrphans() {
        try {
            OrphanDataCleanupService.OrphanCleanupResult result = cleanupService.findOrphanDataOnly();
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "orphanCounts", result.deletedCounts(),
                "orphanedProcessKeys", result.orphanedProcessKeys(),
                "executionTimeMs", result.executionTimeMs(),
                "errors", result.errors()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para limpar dados órfãos
     */
    @DeleteMapping("/orphans")
    public ResponseEntity<Map<String, Object>> cleanupOrphans() {
        try {
            OrphanDataCleanupService.OrphanCleanupResult result = cleanupService.cleanupAllOrphanData();
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "totalDeletions", result.deletedCounts().values().stream().mapToLong(Long::longValue).sum(),
                "deletedCounts", result.deletedCounts(),
                "orphanedProcessKeys", result.orphanedProcessKeys(),
                "executionTimeMs", result.executionTimeMs(),
                "errors", result.errors()
            );
            
            return result.success() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(500).body(response);
                
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para executar migração de dados (adicionar processExternalId aos StageFields)
     */
    @PostMapping("/migration/stage-fields")
    public ResponseEntity<Map<String, Object>> migrateStageFields() {
        try {
            DataMigrationService.MigrationResult result = migrationService.migrateStageFieldsWithProcessExternalId();
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "updatedStageFields", result.updatedStageFields(),
                "processedStages", result.processedStages(),
                "executionTimeMs", result.executionTimeMs(),
                "error", result.error() != null ? result.error() : ""
            );
            
            return result.success() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(500).body(response);
                
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para verificar status da migração
     */
    @GetMapping("/migration/status")
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        try {
            Map<String, Object> integrity = migrationService.validateDataIntegrity();
            long needingMigration = migrationService.countStageFieldsNeedingMigration();
            boolean complete = migrationService.isMigrationComplete();
            
            Map<String, Object> response = Map.of(
                "migrationComplete", complete,
                "stageFieldsNeedingMigration", needingMigration,
                "dataIntegrity", integrity
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para testar CASCATA SIMPLIFICADA (nova versão otimizada)
     */
    @DeleteMapping("/simplified-cascade/{processKey}")
    public ResponseEntity<Map<String, Object>> testSimplifiedCascadeDelete(@PathVariable String processKey) {
        try {
            SimplifiedProcessCascadeService.SimplifiedCascadeResult result = 
                simplifiedCascadeService.deleteProcessWithSimplifiedCascade(processKey);
            
            Map<String, Object> response = Map.of(
                "success", result.success(),
                "processKey", result.processKey(),
                "totalDeletions", result.totalDeletions(),
                "deletedCounts", result.deletedCounts(),
                "executionTimeMs", result.executionTimeMs(),
                "errors", result.errors(),
                "cascadeType", "SIMPLIFIED"
            );
            
            return result.success() ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(500).body(response);
                
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage(),
                "processKey", processKey,
                "cascadeType", "SIMPLIFIED"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para contar dados de um processo (debugging)
     */
    @GetMapping("/count/{processKey}")
    public ResponseEntity<Map<String, Object>> countProcessData(@PathVariable String processKey) {
        try {
            Map<String, Long> counts = simplifiedCascadeService.countDataForProcess(processKey);
            long total = counts.values().stream().mapToLong(Long::longValue).sum();
            
            Map<String, Object> response = Map.of(
                "processKey", processKey,
                "counts", counts,
                "totalRecords", total
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage(),
                "processKey", processKey
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}