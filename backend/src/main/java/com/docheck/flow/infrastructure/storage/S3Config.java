package com.docheck.flow.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Configuration;

import software.amazon.awssdk.utils.StringUtils;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${app.storage.s3.enabled:true}")
    boolean enabled;
    @Value("${app.storage.s3.endpoint:http://localhost:9000}")
    String endpoint;
    @Value("${app.storage.s3.region:us-east-1}")
    String region;
    @Value("${app.storage.s3.accessKey:minioadmin}")
    String accessKey;
    @Value("${app.storage.s3.secretKey:minioadmin}")
    String secretKey;

    @Bean
    public S3Client s3Client() {
        if (!enabled)
            return null;
        S3Configuration s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        S3ClientBuilder b = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(s3cfg)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        if (StringUtils.isNotBlank(endpoint))
            b = b.endpointOverride(URI.create(endpoint));
        return b.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        if (!enabled)
            return null;
        S3Configuration s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        S3Presigner.Builder b = S3Presigner.builder()
                .region(Region.of(region))
                .serviceConfiguration(s3cfg)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        if (StringUtils.isNotBlank(endpoint))
            b = b.endpointOverride(URI.create(endpoint));
        return b.build();
    }
}
