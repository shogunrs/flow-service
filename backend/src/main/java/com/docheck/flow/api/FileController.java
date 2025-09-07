package com.docheck.flow.api;

import com.docheck.flow.infrastructure.storage.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> presignDownload(@RequestParam("key") String key) {
        return ResponseEntity.ok(storage.presignDownload(key));
    }
}

