package com.docheck.flow.api;

import com.docheck.flow.infrastructure.storage.UserFileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// TEMPORARIAMENTE DESABILITADO - Problema com reflection de parâmetros
//@RestController
//@RequestMapping("/api/v1/temp")
public class TemporaryFileController {

    private final UserFileStorageService userFileStorageService;

    public TemporaryFileController(UserFileStorageService userFileStorageService) {
        this.userFileStorageService = userFileStorageService;
    }

    @PostMapping("/files/upload-url")
    public ResponseEntity<?> getTemporaryFileUploadUrl(@RequestParam(value = "filename") String filename,
            @RequestParam(value = "contentType") String contentType,
            @RequestParam(value = "fileType") String fileType,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        try {
            // Gerar sessionId se não fornecido
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            // Validar tipo de arquivo
            UserFileStorageService.FileType type;
            switch (fileType.toUpperCase()) {

                case "DOCUMENT":
                    type = UserFileStorageService.FileType.DOCUMENT;
                    break;
                case "PROFILE_PHOTO":
                    type = UserFileStorageService.FileType.PROFILE_PHOTO;
                    break;
                case "ADDRESS_PROOF":
                    type = UserFileStorageService.FileType.ADDRESS_PROOF;
                case "CONTRATO_SOCIAL":
                    type = UserFileStorageService.FileType.CONTRATO_SOCIAL;
                    break;
                case "CARTAO_CNPJ":
                    type = UserFileStorageService.FileType.CARTAO_CNPJ;
                    break;
                case "FACE_RECOGNITION":
                    type = UserFileStorageService.FileType.FACE_RECOGNITION;
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            Map.of("error",
                                    "Invalid file type. Allowed: IDENTITY, RG, CNH, DOCUMENT, PROFILE_PHOTO, ADDRESS_PROOF"));
            }

            {

            }

            Map<String, Object> uploadData = userFileStorageService.presignTemporaryFileUpload(
                    sessionId,
                    filename,
                    contentType,
                    type);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", uploadData,
                    "sessionId", sessionId,
                    "uploadType", "temporary"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error generating temporary upload URL: " + e.getMessage()));
        }
    }

    @PostMapping("/files/migrate")
    public ResponseEntity<?> migrateTemporaryFile(@RequestParam(value = "userId") String userId,
            @RequestParam(value = "tempObjectKey") String tempObjectKey,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "fileType") String fileType) {
        try {
            // Validar tipo de arquivo
            UserFileStorageService.FileType type;
            switch (fileType.toUpperCase()) {
                case "IDENTITY":
                case "RG":
                case "CNH":
                case "DOCUMENT":
                    type = UserFileStorageService.FileType.DOCUMENT;
                    break;
                case "PROFILE_PHOTO":
                    type = UserFileStorageService.FileType.PROFILE_PHOTO;
                    break;
                case "ADDRESS_PROOF":
                    type = UserFileStorageService.FileType.ADDRESS_PROOF;
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            Map.of("error",
                                    "Invalid file type. Allowed: IDENTITY, RG, CNH, DOCUMENT, PROFILE_PHOTO, ADDRESS_PROOF"));
            }

            Map<String, Object> migratedData = userFileStorageService.migrateTemporaryFileToUser(
                    userId,
                    tempObjectKey,
                    filename,
                    type);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", migratedData,
                    "message", "File migrated successfully from temporary to user storage"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error migrating temporary file: " + e.getMessage()));
        }
    }
}