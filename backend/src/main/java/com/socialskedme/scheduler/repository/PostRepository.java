package com.socialskedme.scheduler.repository;

import com.socialskedme.scheduler.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserIdOrderByScheduledAtDesc(UUID userId, Pageable pageable);
    Optional<Post> findByIdAndUserId(UUID id, UUID userId);

    /** Posts whose video should be purged from S3 (published or failed, old enough, not yet purged). */
    @Query("""
        SELECT p FROM Post p
        WHERE p.status IN ('published', 'failed')
          AND p.updatedAt < :cutoff
          AND p.videoPurged = false
          AND p.videoUrl IS NOT NULL
        """)
    List<Post> findPostsEligibleForVideoPurge(@Param("cutoff") LocalDateTime cutoff);
}
