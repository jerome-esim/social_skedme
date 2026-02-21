package com.socialskedme.scheduler.model;

import com.socialskedme.scheduler.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String title;
    private String caption;
    private String hashtags;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "video_filename")
    private String videoFilename;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> platforms;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 100)
    private String timezone;

    @Column(length = 50)
    private String status;

    @Column(name = "late_post_id")
    private String latePostId;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)  createdAt  = LocalDateTime.now();
        if (updatedAt == null)  updatedAt  = LocalDateTime.now();
        if (timezone == null)   timezone   = "Europe/Paris";
        if (status == null)     status     = "draft";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
