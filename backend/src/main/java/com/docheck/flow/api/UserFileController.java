package com.docheck.flow.api;

import com.docheck.flow.api.dto.FileUploadRequestDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.infrastructure.storage.UserFileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserFileController {

    private final UserFileStorageService userFileStorageService;
    private final UserService userService;

    public UserFileController(UserFileStorageService userFileStorageService, UserService userService) {
        this.userFileStorageService = userFileStorageService;
        this.userService = userService;
    }

    @PostMapping("/{userId}/files/upload-url")
    public ResponseEntity<?> getUploadUrl(@PathVariable("userId") String userId,
                                        @Valid @RequestBody FileUploadRequestDTO request) {
        try {
            // Validar e converter o tipo de arquivo
            UserFileStorageService.FileType fileType;
            try {
                fileType = UserFileStorageService.FileType.valueOf(request.fileType().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid file type. Allowed types: PROFILE_PHOTO, ADDRESS_PROOF, DOCUMENT")
                );
            }

            // Gerar URL de upload presignada
            Map<String, Object> uploadData = userFileStorageService.presignUserFileUpload(
                userId,
                request.filename(),
                request.contentType(),
                fileType
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", uploadData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error generating upload URL: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{userId}/files/{objectKey}/download-url")
    public ResponseEntity<?> getDownloadUrl(@PathVariable("userId") String userId,
                                          @PathVariable("objectKey") String objectKey) {
        try {
            // Verificar se a objectKey pertence ao usuário (segurança básica)
            if (!objectKey.startsWith("users/" + userId + "/")) {
                return ResponseEntity.status(403).body(
                    Map.of("error", "Access denied to this file")
                );
            }

            Map<String, Object> downloadData = userFileStorageService.presignUserFileDownload(objectKey);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", downloadData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error generating download URL: " + e.getMessage())
            );
        }
    }

    @PostMapping("/{userId}/files/profile-photo/upload-url")
    public ResponseEntity<?> getProfilePhotoUploadUrl(@PathVariable("userId") String userId,
                                                     @RequestParam(value = "filename") String filename,
                                                     @RequestParam(value = "contentType") String contentType) {
        try {
            Map<String, Object> uploadData = userFileStorageService.presignUserFileUpload(
                userId,
                filename,
                contentType,
                UserFileStorageService.FileType.PROFILE_PHOTO
            );

            return ResponseEntity.ok(uploadData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error generating profile photo upload URL: " + e.getMessage())
            );
        }
    }

    @PostMapping("/{userId}/files/address-proof/upload-url")
    public ResponseEntity<?> getAddressProofUploadUrl(@PathVariable("userId") String userId,
                                                     @RequestParam(value = "filename") String filename,
                                                     @RequestParam(value = "contentType") String contentType) {
        try {
            Map<String, Object> uploadData = userFileStorageService.presignUserFileUpload(
                userId,
                filename,
                contentType,
                UserFileStorageService.FileType.ADDRESS_PROOF
            );

            return ResponseEntity.ok(uploadData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error generating address proof upload URL: " + e.getMessage())
            );
        }
    }

    @PostMapping("/{userId}/files/confirm-upload")
    public ResponseEntity<?> confirmFileUpload(@PathVariable("userId") String userId,
                                             @RequestBody Map<String, Object> confirmData,
                                             HttpServletRequest request) {
        try {
            String objectKey = (String) confirmData.get("objectKey");
            String publicUrl = (String) confirmData.get("publicUrl");
            String filename = (String) confirmData.get("filename");
            String fileType = (String) confirmData.get("fileType");
            String contentType = (String) confirmData.get("contentType");
            Long fileSize = ((Number) confirmData.get("fileSize")).longValue();
            Boolean isMobileUpload = (Boolean) confirmData.getOrDefault("isMobileUpload", false);

            // Capturar IP e localização
            String ipAddress = getClientIpAddress(request);
            String location = (String) confirmData.get("location"); // Enviado pelo frontend

            // Salvar referência do arquivo no banco
            userService.addFileReference(
                userId, objectKey, publicUrl, filename, fileType,
                contentType, fileSize, ipAddress, location, isMobileUpload
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "File reference saved successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error saving file reference: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{userId}/files")
    public ResponseEntity<?> getUserFiles(@PathVariable("userId") String userId) {
        try {
            var files = userService.getUserFiles(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", files
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error retrieving user files: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{userId}/files/{fileType}")
    public ResponseEntity<?> getUserFileByType(@PathVariable("userId") String userId,
                                              @PathVariable("fileType") String fileType) {
        try {
            var file = userService.getUserFileByType(userId, fileType.toUpperCase());
            if (file != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", file
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error retrieving user file: " + e.getMessage())
            );
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}