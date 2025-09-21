package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.DocumentApprovalDecisionRequest;
import com.docheck.flow.domain.model.User;
import com.docheck.flow.infrastructure.storage.UserFileStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentApprovalService {
    private final UserService userService;
    private final UserFileStorageService storageService;

    public DocumentApprovalService(UserService userService, UserFileStorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    public List<User.UserFileReference> listFiles(String entityType, String entityId) {
        Entity entity = resolveEntity(entityType);
        switch (entity) {
            case USER:
                User user = userService.get(entityId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                return user.getAllFiles();
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    public void decide(String entityType, String entityId, String fileType,
                       DocumentApprovalDecisionRequest body) {
        Entity entity = resolveEntity(entityType);
        switch (entity) {
            case USER:
                User.DocumentReviewStatus status = User.DocumentReviewStatus.valueOf(body.getStatus().toUpperCase());
                userService.updateFileStatus(entityId, fileType.toUpperCase(), status,
                        body.getReviewerId(), body.getReviewerName(), body.getReviewNotes());
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    public Map<String, Object> generateDownload(String entityType, String entityId, String fileType) {
        Entity entity = resolveEntity(entityType);
        switch (entity) {
            case USER:
                User.UserFileReference ref = Optional.ofNullable(
                        userService.getUserFileByType(entityId, fileType.toUpperCase()))
                        .orElseThrow(() -> new IllegalArgumentException("File not found for type " + fileType));
                Map<String, Object> data = new HashMap<>(storageService.presignUserFileDownload(ref.getObjectKey()));
                data.put("filename", ref.getFilename());
                data.put("contentType", ref.getContentType());
                data.put("fileSize", ref.getFileSize());
                return data;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    private Entity resolveEntity(String name) {
        if (name == null) {
            throw new IllegalArgumentException("entityType must not be null");
        }
        try {
            return Entity.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown entityType: " + name);
        }
    }

    private enum Entity {
        USER
    }
}
