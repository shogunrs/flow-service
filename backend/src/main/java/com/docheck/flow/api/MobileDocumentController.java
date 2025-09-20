package com.docheck.flow.api;

import com.docheck.flow.application.service.UserService;
import com.docheck.flow.infrastructure.storage.UserFileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mobile")
public class MobileDocumentController {

    private final UserFileStorageService userFileStorageService;
    private final UserService userService;

    public MobileDocumentController(UserFileStorageService userFileStorageService, UserService userService) {
        this.userFileStorageService = userFileStorageService;
        this.userService = userService;
    }

    @PostMapping("/users/{userId}/documents/upload-url")
    public ResponseEntity<?> getMobileDocumentUploadUrl(@PathVariable("userId") String userId,
            @RequestParam(value = "filename") String filename,
            @RequestParam(value = "contentType") String contentType,
            @RequestParam(value = "documentType") String documentType) {
        try {
            // Validar tipo de documento
            UserFileStorageService.FileType fileType;
            switch (documentType.toUpperCase()) {

                case "CONTRATO_SOCIAL":
                    fileType = UserFileStorageService.FileType.CONTRATO_SOCIAL;
                    break;
                case "IDENTITY":

                case "CARTAO_CNPJ":
                    fileType = UserFileStorageService.FileType.CARTAO_CNPJ;
                    break;
                case "FACE_RECOGNITION":
                    fileType = UserFileStorageService.FileType.FACE_RECOGNITION;
                    break;
                case "PROFILE_PHOTO":
                    fileType = UserFileStorageService.FileType.PROFILE_PHOTO;
                    break;
                case "ADDRESS_PROOF":
                    fileType = UserFileStorageService.FileType.ADDRESS_PROOF;
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            Map.of("error",
                                    "Invalid document type. Allowed: IDENTITY, RG, CPF, FACE_RECOGNITION, PROFILE_PHOTO, ADDRESS_PROOF"));
            }

            Map<String, Object> uploadData = userFileStorageService.presignUserFileUpload(
                    userId,
                    filename,
                    contentType,
                    fileType);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", uploadData,
                    "uploadType", "mobile"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error generating mobile upload URL: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/documents/confirm-upload")
    public ResponseEntity<?> confirmMobileDocumentUpload(@PathVariable("userId") String userId,
            @RequestBody Map<String, Object> confirmData,
            HttpServletRequest request) {
        try {
            String objectKey = (String) confirmData.get("objectKey");
            String publicUrl = (String) confirmData.get("publicUrl");
            String filename = (String) confirmData.get("filename");
            String documentType = (String) confirmData.get("documentType");
            String contentType = (String) confirmData.get("contentType");
            Long fileSize = ((Number) confirmData.get("fileSize")).longValue();
            String location = (String) confirmData.get("location");

            // Capturar IP
            String ipAddress = getClientIpAddress(request);

            // Salvar referência do documento no banco (sempre marcado como mobile)
            userService.addFileReference(
                    userId, objectKey, publicUrl, filename, documentType.toUpperCase(),
                    contentType, fileSize, ipAddress, location, true // isMobileUpload = true
            );

            // Adicionar tags específicas baseadas no tipo de documento
            addDocumentTags(userId, documentType);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mobile document upload confirmed successfully",
                    "documentType", documentType));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error confirming mobile document upload: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}/face-recognition/link")
    public ResponseEntity<?> getFaceRecognitionLink(@PathVariable("userId") String userId) {
        try {
            // Gerar link único para reconhecimento facial via mobile
            String baseUrl = "https://your-domain.com"; // Configurar conforme seu domínio
            String faceRecognitionLink = baseUrl + "/mobile/face-recognition/" + userId;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "faceRecognitionLink", faceRecognitionLink,
                    "qrCode", generateQRCodeData(faceRecognitionLink),
                    "expiresIn", "24h"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error generating face recognition link: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/face-recognition/process")
    public ResponseEntity<?> processFaceRecognition(@PathVariable("userId") String userId,
            @RequestBody Map<String, Object> faceData,
            HttpServletRequest request) {
        try {
            String faceEmbedding = (String) faceData.get("faceEmbedding");
            String location = (String) faceData.get("location");
            String ipAddress = getClientIpAddress(request);

            // Atualizar embedding facial do usuário
            userService.updateFaceEmbedding(userId, faceEmbedding);

            // Atualizar informações de acesso
            userService.updateAccessInfo(userId, ipAddress, location);

            // Adicionar tag de reconhecimento facial completado
            userService.addUserTag(userId, "FACE_RECOGNITION_COMPLETED");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Face recognition completed successfully"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error processing face recognition: " + e.getMessage()));
        }
    }

    private void addDocumentTags(String userId, String documentType) {
        try {
            switch (documentType.toUpperCase()) {
                case "IDENTITY":
                    userService.addUserTag(userId, "IDENTITY_UPLOADED");
                    break;
                case "RG":
                    userService.addUserTag(userId, "RG_UPLOADED");
                    break;
                case "CPF":
                    userService.addUserTag(userId, "CPF_UPLOADED");
                    break;
                case "PROFILE_PHOTO":
                    userService.addUserTag(userId, "PROFILE_PHOTO_UPLOADED");
                    break;
                case "ADDRESS_PROOF":
                    userService.addUserTag(userId, "ADDRESS_PROOF_UPLOADED");
                    break;
                case "FACE_RECOGNITION":
                    userService.addUserTag(userId, "FACE_RECOGNITION_UPLOADED");
                    break;
            }
            userService.addUserTag(userId, "MOBILE_UPLOAD_USED");
        } catch (Exception e) {
            // Log erro mas não falhar o processo principal
            System.err.println("Error adding document tags: " + e.getMessage());
        }
    }

    private String generateQRCodeData(String link) {
        // Implementar geração de QR code - por enquanto retorna os dados
        return "data:image/png;base64,QR_CODE_BASE64_DATA_FOR_" + link;
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