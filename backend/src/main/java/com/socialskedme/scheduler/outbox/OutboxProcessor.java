package com.socialskedme.scheduler.outbox;

import com.socialskedme.scheduler.model.Post;
import com.socialskedme.scheduler.repository.PostRepository;
import com.socialskedme.scheduler.service.LateApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private static final int BATCH_SIZE   = 10;
    private static final int MAX_ATTEMPTS = 3;

    private final OutboxRepository outboxRepository;
    private final PostRepository   postRepository;
    private final LateApiService   lateApiService;

    /**
     * Polls outbox_events every 10 seconds.
     * Uses FOR UPDATE SKIP LOCKED so multiple instances don't collide.
     */
    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void process() {
        List<OutboxEvent> events = outboxRepository.findPendingWithLock(BATCH_SIZE);

        if (events.isEmpty()) return;

        log.debug("OutboxProcessor: processing {} events", events.size());

        for (OutboxEvent event : events) {
            event.setStatus("processing");
            event.setAttempts(event.getAttempts() + 1);
            outboxRepository.save(event);

            try {
                String latePostId = lateApiService.schedulePost(event.getPayload());

                Post post = postRepository.findById(event.getAggregateId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Post not found: " + event.getAggregateId()));

                post.setStatus("scheduled");
                post.setLatePostId(latePostId);
                postRepository.save(post);

                event.setStatus("sent");
                event.setProcessedAt(LocalDateTime.now());
                log.info("Outbox event {} processed → latePostId={}", event.getId(), latePostId);

            } catch (Exception ex) {
                log.error("Outbox event {} failed (attempt {}): {}",
                        event.getId(), event.getAttempts(), ex.getMessage());

                event.setLastError(ex.getMessage());
                event.setStatus(event.getAttempts() >= MAX_ATTEMPTS ? "failed" : "pending");

                if ("failed".equals(event.getStatus())) {
                    // Mark the corresponding post as failed too
                    postRepository.findById(event.getAggregateId()).ifPresent(p -> {
                        p.setStatus("failed");
                        p.setErrorMessage("Outbox exhausted: " + ex.getMessage());
                        postRepository.save(p);
                    });
                }
            }

            outboxRepository.save(event);
        }
    }
}
