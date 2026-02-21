package com.socialskedme.scheduler.repository;

import com.socialskedme.scheduler.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserIdOrderByScheduledAtDesc(UUID userId, Pageable pageable);
    Optional<Post> findByIdAndUserId(UUID id, UUID userId);
}
