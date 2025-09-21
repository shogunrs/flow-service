package com.docheck.flow.api;

import com.docheck.flow.api.dto.DocumentApprovalDecisionRequest;
import com.docheck.flow.api.dto.DocumentFileDTO;
import com.docheck.flow.application.service.DocumentApprovalService;
import com.docheck.flow.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/document-approvals")
public class DocumentApprovalController {

    private final DocumentApprovalService approvalService;

    public DocumentApprovalController(DocumentApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/{entityType}/{entityId}/files")
    public ResponseEntity<?> listFiles(@PathVariable("entityType") String entityType,
                                       @PathVariable("entityId") String entityId) {
        try {
            List<DocumentFileDTO> data = approvalService.listFiles(entityType, entityId).stream()
                    .map(ref -> DocumentFileDTO.builder()
                            .fileType(ref.getFileType())
                            .filename(ref.getFilename())
                            .status(ref.getStatus() != null ? ref.getStatus().name() : User.DocumentReviewStatus.PENDING.name())
                            .uploadedAt(ref.getUploadedAt())
                            .fileSize(ref.getFileSize())
                            .contentType(ref.getContentType())
                            .reviewerId(ref.getReviewerId())
                            .reviewerName(ref.getReviewerName())
                            .reviewedAt(ref.getReviewedAt())
                            .reviewNotes(ref.getReviewNotes())
                            .downloadPath(String.format("/api/v1/document-approvals/%s/%s/%s/download",
                                    entityType, entityId, ref.getFileType()))
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/{entityType}/{entityId}/{fileType}")
    public ResponseEntity<?> decide(@PathVariable("entityType") String entityType,
                                     @PathVariable("entityId") String entityId,
                                     @PathVariable("fileType") String fileType,
                                     @RequestBody DocumentApprovalDecisionRequest body) {
        try {
            approvalService.decide(entityType, entityId, fileType, body);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{entityType}/{entityId}/{fileType}/download")
    public ResponseEntity<?> download(@PathVariable("entityType") String entityType,
                                       @PathVariable("entityId") String entityId,
                                       @PathVariable("fileType") String fileType) {
        try {
            Map<String, Object> data = approvalService.generateDownload(entityType, entityId, fileType);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
