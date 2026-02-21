package com.socialskedme.scheduler.service;

import com.socialskedme.scheduler.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
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

    private String buildUrl(String key) {
        // MinIO / custom endpoint
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint + "/" + bucket + "/" + key;
        }
        // AWS S3
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
