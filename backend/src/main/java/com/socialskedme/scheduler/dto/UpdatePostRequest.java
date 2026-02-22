package com.socialskedme.scheduler.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdatePostRequest {
    private String title;
    private String caption;
    private String hashtags;
    private String videoUrl;
    private String videoFilename;
    private List<String> platforms;
    private LocalDateTime scheduledAt;
    private String timezone;
}
