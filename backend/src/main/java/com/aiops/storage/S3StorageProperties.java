package com.aiops.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.s3")
public class S3StorageProperties {

    private String endpoint = "http://localhost:9000";
    private String bucket = "aiops-documents";
    private String region = "ap-south-1";
    private String accessKey = "aiops-minio-admin";
    private String secretKey = "aiops-minio-secret-32chars";
    private int presignedUrlExpirationMinutes = 60;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public int getPresignedUrlExpirationMinutes() { return presignedUrlExpirationMinutes; }
    public void setPresignedUrlExpirationMinutes(int presignedUrlExpirationMinutes) {
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }
}
