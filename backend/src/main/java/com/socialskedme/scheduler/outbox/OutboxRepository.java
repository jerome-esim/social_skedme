package com.socialskedme.scheduler.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Fetches up to {@code limit} pending/failed events (attempts < 3) using
     * SKIP LOCKED so that concurrent processor instances don't double-process.
     */
    @Query(
        value = """
            SELECT * FROM outbox_events
            WHERE status IN ('pending', 'failed')
              AND attempts < 3
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<OutboxEvent> findPendingWithLock(@Param("limit") int limit);
}
