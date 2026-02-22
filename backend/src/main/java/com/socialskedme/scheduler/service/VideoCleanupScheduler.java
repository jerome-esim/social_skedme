package com.socialskedme.scheduler.service;

import com.socialskedme.scheduler.model.Post;
import com.socialskedme.scheduler.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deletes videos from S3/MinIO for posts that are older than the configured
 * retention period (default: 30 days after the post was last updated).
 *
 * Only published and failed posts are eligible — draft/pending/scheduled posts
 * keep their video so it can be retried or previewed.
 *
 * The DB record is kept intact; only the S3 object is deleted.
 * video_purged=true ensures we never attempt to delete the same object twice.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoCleanupScheduler {

    private final PostRepository postRepository;
    private final MediaUploadService mediaUploadService;

    @Value("${app.media.video-retention-days:30}")
    private int retentionDays;

    /** Runs once a day at 03:00 UTC. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredVideos() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<Post> candidates = postRepository.findPostsEligibleForVideoPurge(cutoff);

        if (candidates.isEmpty()) {
            log.debug("VideoCleanup: no videos to purge");
            return;
        }

        log.info("VideoCleanup: purging {} video(s) older than {} days", candidates.size(), retentionDays);

        for (Post post : candidates) {
            mediaUploadService.deleteByUrl(post.getVideoUrl());
            post.setVideoPurged(true);
            post.setVideoPurgedAt(LocalDateTime.now());
            postRepository.save(post);
            log.info("VideoCleanup: purged video for postId={}", post.getId());
        }
    }
}
