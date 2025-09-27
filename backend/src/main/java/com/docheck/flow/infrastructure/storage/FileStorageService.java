package com.docheck.flow.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // Adicionado
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
import software.amazon.awssdk.core.sync.RequestBody; // Adicionado

import java.io.IOException; // Adicionado
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileStorageService {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public FileStorageService(S3Client s3, S3Presigner presigner,
                              @Value("${app.storage.s3.bucket:flow-files}") String bucket) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
        ensureBucket();
    }

    private void ensureBucket() {
        if (s3 == null) return; // storage disabled
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            try { s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build()); } catch (Exception ignored) {}
        } catch (AwsServiceException | SdkClientException ignored) {}
    }

    public Map<String, Object> presignUpload(String filename, String contentType, String prefix) {
        if (presigner == null) throw new IllegalStateException("S3 storage disabled");
        String safe = sanitize(filename == null ? "file" : filename);
        String key = (prefix != null && !prefix.isBlank() ? prefix.replaceAll("/+", "/") + "/" : "")
                + Instant.now().toEpochMilli() + "_" + safe;
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType == null ? "application/octet-stream" : contentType)
                .build();
        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(put)
                .build();
        URL url = presigner.presignPutObject(req).url();
        Map<String, Object> out = new HashMap<>();
        out.put("url", url.toString());
        out.put("objectKey", key);
        return out;
    }

    // NOVO MÉTODO: Faz o upload direto de um MultipartFile para o S3/MinIO
    public String uploadFile(MultipartFile file, String prefix) throws IOException {
        if (s3 == null) throw new IllegalStateException("S3 storage disabled");

        String safeFileName = sanitize(file.getOriginalFilename());
        String key = (prefix != null && !prefix.isBlank() ? prefix.replaceAll("/+", "/") + "/" : "")
                + Instant.now().toEpochMilli() + "_" + safeFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .contentLength(file.getSize())
                .build();

        s3.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key; // Retorna a chave (caminho) do objeto armazenado
    }

    public Map<String, Object> presignDownload(String key) {
        if (presigner == null) throw new IllegalStateException("S3 storage disabled");
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(get)
                .build();
        URL url = presigner.presignGetObject(req).url();
        Map<String, Object> out = new HashMap<>();
        out.put("url", url.toString());
        out.put("objectKey", key);
        return out;
    }

    public InputStream getFileStream(String key) {
        if (s3 == null) throw new IllegalStateException("S3 storage disabled");
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return s3.getObject(getObjectRequest);
    }

    private static String sanitize(String name) {
        String n = name.trim();
        n = n.replaceAll("[^A-Za-z0-9._-]", "-");
        return n.length() == 0 ? "file" : n;
    }
}