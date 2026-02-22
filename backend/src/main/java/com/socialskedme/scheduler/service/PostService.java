package com.socialskedme.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialskedme.scheduler.dto.CreatePostRequest;
import com.socialskedme.scheduler.dto.UpdatePostRequest;
import com.socialskedme.scheduler.model.Post;
import com.socialskedme.scheduler.model.SocialAccount;
import com.socialskedme.scheduler.outbox.OutboxEvent;
import com.socialskedme.scheduler.outbox.OutboxRepository;
import com.socialskedme.scheduler.repository.PostRepository;
import com.socialskedme.scheduler.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final OutboxRepository outboxRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final MediaUploadService mediaUploadService;
    private final ObjectMapper objectMapper;

    /**
     * Creates the post and the outbox event atomically in a single transaction.
     */
    @Transactional
    public Post createPost(CreatePostRequest request, UUID userId) throws Exception {
        // Convert scheduledAt from user's timezone to UTC
        ZoneId tz = ZoneId.of(request.getTimezone() != null ? request.getTimezone() : "Europe/Paris");
        var scheduledAtUtc = request.getScheduledAt()
                .atZone(tz)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .caption(request.getCaption())
                .hashtags(request.getHashtags())
                .videoUrl(request.getVideoUrl())
                .videoFilename(request.getVideoFilename())
                .platforms(request.getPlatforms())
                .scheduledAt(scheduledAtUtc)
                .timezone(request.getTimezone())
                .status("pending")
                .build();

        postRepository.save(post);

        // Resolve Late account IDs for each platform
        List<Map<String, String>> platformAccounts = buildPlatformAccounts(
                request.getPlatforms(), userId
        );

        // Build outbox payload
        String payload = buildOutboxPayload(post, platformAccounts);

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("POST")
                .aggregateId(post.getId())
                .eventType("POST_SCHEDULE_REQUESTED")
                .payload(payload)
                .status("pending")
                .attempts(0)
                .build();

        outboxRepository.save(event);

        log.info("Post created id={} with outbox event, status=pending", post.getId());
        return post;
    }

    public Page<Post> getPosts(UUID userId, Pageable pageable) {
        return postRepository.findByUserIdOrderByScheduledAtDesc(userId, pageable);
    }

    public Post getPost(UUID postId, UUID userId) {
        return postRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));
    }

    @Transactional
    public Post updatePost(UUID postId, UpdatePostRequest request, UUID userId) {
        Post post = getPost(postId, userId);

        if (!Set.of("draft", "pending").contains(post.getStatus())) {
            throw new IllegalStateException("Cannot edit a post with status: " + post.getStatus());
        }

        if (request.getTitle() != null)         post.setTitle(request.getTitle());
        if (request.getCaption() != null)       post.setCaption(request.getCaption());
        if (request.getHashtags() != null)      post.setHashtags(request.getHashtags());
        if (request.getVideoUrl() != null)      post.setVideoUrl(request.getVideoUrl());
        if (request.getVideoFilename() != null) post.setVideoFilename(request.getVideoFilename());
        if (request.getPlatforms() != null)     post.setPlatforms(request.getPlatforms());
        if (request.getTimezone() != null)      post.setTimezone(request.getTimezone());
        if (request.getScheduledAt() != null) {
            String tz = request.getTimezone() != null ? request.getTimezone() : post.getTimezone();
            var utc = request.getScheduledAt()
                    .atZone(ZoneId.of(tz))
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
            post.setScheduledAt(utc);
        }

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = getPost(postId, userId);

        if (Set.of("scheduled", "published").contains(post.getStatus())) {
            throw new IllegalStateException("Cannot delete a " + post.getStatus() + " post");
        }

        // Delete the video from S3/MinIO before removing the DB record
        if (!post.isVideoPurged()) {
            mediaUploadService.deleteByUrl(post.getVideoUrl());
        }

        postRepository.delete(post);
    }

    @Transactional
    public void markPublished(String latePostId) {
        postRepository.findAll().stream()
                .filter(p -> latePostId.equals(p.getLatePostId()))
                .findFirst()
                .ifPresent(p -> {
                    p.setStatus("published");
                    postRepository.save(p);
                    log.info("Post published latePostId={}", latePostId);
                });
    }

    @Transactional
    public void markFailed(String latePostId, String error) {
        postRepository.findAll().stream()
                .filter(p -> latePostId.equals(p.getLatePostId()))
                .findFirst()
                .ifPresent(p -> {
                    p.setStatus("failed");
                    p.setErrorMessage(error);
                    postRepository.save(p);
                    log.warn("Post failed latePostId={} error={}", latePostId, error);
                });
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Map<String, String>> buildPlatformAccounts(List<String> platforms, UUID userId) {
        return platforms.stream().map(platform -> {
            List<SocialAccount> accounts = socialAccountRepository
                    .findByUserIdAndPlatform(userId, platform);
            if (accounts.isEmpty()) {
                throw new IllegalArgumentException(
                        "No connected account for platform: " + platform +
                        ". Please connect your account first in /api/accounts."
                );
            }
            SocialAccount acc = accounts.get(0);
            return Map.of("platform", platform, "accountId", acc.getLateAccountId());
        }).collect(Collectors.toList());
    }

    private String buildOutboxPayload(Post post, List<Map<String, String>> platformAccounts) throws Exception {
        String fullCaption = (post.getCaption() != null ? post.getCaption() : "")
                + (post.getHashtags() != null ? " " + post.getHashtags() : "");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("postId",       post.getId().toString());
        payload.put("caption",      fullCaption.trim());
        payload.put("videoUrl",     post.getVideoUrl());
        payload.put("platforms",    platformAccounts);
        payload.put("scheduledFor", post.getScheduledAt().toString() + "Z");

        return objectMapper.writeValueAsString(payload);
    }
}
