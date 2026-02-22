package com.socialskedme.scheduler.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(name = "late_account_id", nullable = false)
    private String lateAccountId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @PrePersist
    void onCreate() {
        if (connectedAt == null) connectedAt = LocalDateTime.now();
    }
}
