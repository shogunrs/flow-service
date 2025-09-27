package com.docheck.flow.api;

import com.docheck.flow.api.dto.KnowledgeDocumentDto;
import com.docheck.flow.application.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // WARNING: For development only
public class RagController {

    private final RagService ragService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadKnowledgeFile(
            @RequestParam("agentId") String agentId,
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files received.");
        }

        for (MultipartFile file : files) {
            ragService.processAndStoreFile(agentId, file);
        }

        return ResponseEntity.ok("Files received successfully. Processing has started.");
    }

    @GetMapping("/processing-status")
    public ResponseEntity<List<KnowledgeDocumentDto>> getProcessingStatus() {
        List<KnowledgeDocumentDto> processingDocs = ragService.getProcessingDocuments().stream()
                .map(KnowledgeDocumentDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(processingDocs);
    }
}
