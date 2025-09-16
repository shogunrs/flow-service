package com.docheck.flow.api.dto;

public record FaceRecognitionDTO(
        String faceEmbedding,
        String ipAddress,
        String location // "lat,lng"
) {}