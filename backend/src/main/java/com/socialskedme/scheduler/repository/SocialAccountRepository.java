package com.socialskedme.scheduler.repository;

import com.socialskedme.scheduler.model.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, UUID> {
    List<SocialAccount> findByUserId(UUID userId);
    List<SocialAccount> findByUserIdAndPlatform(UUID userId, String platform);
    Optional<SocialAccount> findByIdAndUserId(UUID id, UUID userId);
}
