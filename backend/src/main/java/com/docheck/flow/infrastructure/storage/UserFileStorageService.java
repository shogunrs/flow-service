package com.docheck.flow.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserFileStorageService {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String userBucket;
    private final String publicUrlPrefix;

    public UserFileStorageService(S3Client s3, S3Presigner presigner,
            @Value("${app.storage.s3.user-bucket:flow-users}") String userBucket,
            @Value("${app.files.public-base-url:}") String publicBaseUrl) {
        this.s3 = s3;
        this.presigner = presigner;
        this.userBucket = userBucket;
        this.publicUrlPrefix = resolvePublicUrlPrefix(publicBaseUrl);
        ensureBucket();
    }

    private void ensureBucket() {
        if (s3 == null)
            return; // storage disabled
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(userBucket).build());
        } catch (NoSuchBucketException e) {
            try {
                s3.createBucket(CreateBucketRequest.builder().bucket(userBucket).build());
            } catch (Exception ignored) {
            }
        } catch (AwsServiceException | SdkClientException ignored) {
        }
    }

    public Map<String, Object> presignUserFileUpload(String userId, String filename, String contentType,
            FileType fileType) {
        if (presigner == null)
            throw new IllegalStateException("S3 storage disabled");

        String safe = sanitize(filename == null ? "file" : filename);
        String userFolder = "users/" + sanitize(userId);
        String typeFolder = fileType.getFolder();
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        // Estrutura: users/{userId}/{tipo}/{timestamp}_{filename}
        String key = userFolder + "/" + typeFolder + "/" + timestamp + "_" + safe;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(userBucket)
                .key(key)
                .contentType(contentType == null ? "application/octet-stream" : contentType)
                .build();

        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(put)
                .build();

        URL url = presigner.presignPutObject(req).url();

        Map<String, Object> out = new HashMap<>();
        out.put("uploadUrl", url.toString());
        out.put("objectKey", key);
        out.put("bucket", userBucket);
        out.put("publicUrl", buildPublicUrl(key));

        return out;
    }

    public Map<String, Object> presignTemporaryFileUpload(String sessionId, String filename, String contentType,
            FileType fileType) {
        if (presigner == null)
            throw new IllegalStateException("S3 storage disabled");

        String safe = sanitize(filename == null ? "file" : filename);
        String tempFolder = "temp/" + sanitize(sessionId);
        String typeFolder = fileType.getFolder();
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        // Estrutura: temp/{sessionId}/{tipo}/{timestamp}_{filename}
        String key = tempFolder + "/" + typeFolder + "/" + timestamp + "_" + safe;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(userBucket)
                .key(key)
                .contentType(contentType == null ? "application/octet-stream" : contentType)
                .build();

        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(put)
                .build();

        URL url = presigner.presignPutObject(req).url();

        Map<String, Object> out = new HashMap<>();
        out.put("uploadUrl", url.toString());
        out.put("objectKey", key);
        out.put("bucket", userBucket);
        out.put("publicUrl", buildPublicUrl(key));
        out.put("isTemporary", true);

        return out;
    }

    public Map<String, Object> presignUserFileDownload(String objectKey) {
        if (presigner == null)
            throw new IllegalStateException("S3 storage disabled");

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(userBucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(get)
                .build();

        URL url = presigner.presignGetObject(req).url();

        Map<String, Object> out = new HashMap<>();
        out.put("downloadUrl", url.toString());
        out.put("objectKey", objectKey);
        out.put("bucket", userBucket);

        return out;
    }

    public Map<String, Object> migrateTemporaryFileToUser(String userId, String tempObjectKey, String filename,
            FileType fileType) {
        if (s3 == null)
            throw new IllegalStateException("S3 storage disabled");

        String safe = sanitize(filename == null ? "file" : filename);
        String userFolder = "users/" + sanitize(userId);
        String typeFolder = fileType.getFolder();
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        // Nova estrutura: users/{userId}/{tipo}/{timestamp}_{filename}
        String newKey = userFolder + "/" + typeFolder + "/" + timestamp + "_" + safe;

        try {
            // Copiar arquivo temporário para localização definitiva
            s3.copyObject(request -> request
                    .sourceBucket(userBucket)
                    .sourceKey(tempObjectKey)
                    .destinationBucket(userBucket)
                    .destinationKey(newKey));

            // Excluir arquivo temporário
            s3.deleteObject(request -> request
                    .bucket(userBucket)
                    .key(tempObjectKey));

            Map<String, Object> out = new HashMap<>();
            out.put("objectKey", newKey);
            out.put("bucket", userBucket);
            out.put("publicUrl", buildPublicUrl(newKey));
            out.put("migrated", true);

            return out;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao migrar arquivo temporário: " + e.getMessage(), e);
        }
    }

    private String buildPublicUrl(String objectKey) {
        return publicUrlPrefix + objectKey;
    }

    private static String resolvePublicUrlPrefix(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "/api/v1/files/";
        }

        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + "/api/v1/files/";
    }

    private static String sanitize(String name) {
        String n = name.trim();
        n = n.replaceAll("[^A-Za-z0-9._-]", "-");
        return n.length() == 0 ? "file" : n;
    }

    public enum FileType {
        PROFILE_PHOTO("profile-photos"),
        ADDRESS_PROOF("address-proofs"),
        DOCUMENT("documents"),
        CONTRATO_SOCIAL("contratos-sociais"),
        CARTAO_CNPJ("cartoes-cnpj"),
        FACE_RECOGNITION("face-recognition");

        private final String folder;

        FileType(String folder) {
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }
    }
}