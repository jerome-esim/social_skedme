package com.socialskedme.scheduler.service;

import com.socialskedme.scheduler.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadService {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.endpoint:}")
    private String endpoint;

    @Value("${app.s3.region}")
    private String region;

    public UploadResponse upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String key = UUID.randomUUID() + "/" + originalName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String videoUrl = buildUrl(key);
        log.info("Uploaded video to {}", videoUrl);
        return new UploadResponse(videoUrl, originalName, file.getSize());
    }

    /**
     * Deletes a video from S3/MinIO given its full URL.
     * Silently ignores errors (e.g. file already deleted).
     */
    public void deleteByUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) return;
        try {
            String key = extractKey(videoUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("Deleted video from S3: key={}", key);
        } catch (Exception e) {
            log.warn("Failed to delete video from S3 url={}: {}", videoUrl, e.getMessage());
        }
    }

    /**
     * Extracts the S3 object key from a full URL.
     * Works for both MinIO (http://host/bucket/key) and AWS (https://bucket.s3.region.amazonaws.com/key).
     */
    private String extractKey(String url) {
        // MinIO: http://localhost:9000/scheduler-videos/uuid/file.mp4  → uuid/file.mp4
        // AWS:   https://bucket.s3.region.amazonaws.com/uuid/file.mp4  → uuid/file.mp4
        String prefix = "/" + bucket + "/";
        int idx = url.indexOf(prefix);
        if (idx >= 0) {
            return url.substring(idx + prefix.length());
        }
        // Fallback: strip everything up to and including the first '/' after the host+bucket
        throw new IllegalArgumentException("Cannot extract S3 key from URL: " + url);
    }

    private String buildUrl(String key) {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint + "/" + bucket + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
