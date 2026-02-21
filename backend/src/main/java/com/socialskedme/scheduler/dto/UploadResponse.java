package com.socialskedme.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private String videoUrl;
    private String filename;
    private long sizeBytes;
}
