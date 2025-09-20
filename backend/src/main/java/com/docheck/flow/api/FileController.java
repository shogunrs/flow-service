package com.docheck.flow.api;

import com.docheck.flow.infrastructure.storage.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileStorageService storage;
    public FileController(FileStorageService storage) { this.storage = storage; }

    public record PresignUploadRequest(String filename, String contentType, String prefix) {}

    @PostMapping("/presign-upload")
    public ResponseEntity<Map<String, Object>> presignUpload(@RequestBody PresignUploadRequest body) {
        return ResponseEntity.ok(storage.presignUpload(body.filename(), body.contentType(), body.prefix()));
    }

    @GetMapping("/presign-download")
    public ResponseEntity<Map<String, Object>> presignDownload(@RequestParam(value = "key") String key) {
        return ResponseEntity.ok(storage.presignDownload(key));
    }

    @GetMapping("/users/**")
    public ResponseEntity<InputStreamResource> serveUserFile(HttpServletRequest request) {
        try {
            // Extrair a chave do path da URL
            String requestURI = request.getRequestURI();
            String objectKey = requestURI.substring("/api/v1/files/".length());

            // Obter o arquivo do storage
            InputStream fileStream = storage.getFileStream(objectKey);

            // Determinar o tipo de conteúdo baseado na extensão
            String contentType = determineContentType(objectKey);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache por 1 hora
                    .body(new InputStreamResource(fileStream));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }
}

