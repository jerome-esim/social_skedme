package com.socialskedme.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePostRequest {

    private String title;

    private String caption;

    private String hashtags;

    @NotBlank(message = "videoUrl is required")
    private String videoUrl;

    private String videoFilename;

    @NotEmpty(message = "At least one platform is required")
    private List<String> platforms;

    @NotNull(message = "scheduledAt is required")
    private LocalDateTime scheduledAt;

    private String timezone = "Europe/Paris";
}
