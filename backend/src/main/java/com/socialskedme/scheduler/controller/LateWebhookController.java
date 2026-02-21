package com.socialskedme.scheduler.controller;

import com.socialskedme.scheduler.config.LateApiConfig;
import com.socialskedme.scheduler.dto.LateWebhookEvent;
import com.socialskedme.scheduler.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/webhooks/late")
@RequiredArgsConstructor
@Slf4j
public class LateWebhookController {

    private final PostService postService;
    private final LateApiConfig lateApiConfig;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Late-Signature", required = false) String signature
    ) {
        if (!verifySignature(rawBody, signature)) {
            log.warn("Late webhook: invalid signature");
            return ResponseEntity.status(401).build();
        }

        LateWebhookEvent event;
        try {
            event = parseEvent(rawBody);
        } catch (Exception e) {
            log.error("Late webhook: failed to parse body", e);
            return ResponseEntity.badRequest().build();
        }

        log.info("Late webhook received: type={} postId={}", event.getType(), event.getPostId());

        switch (event.getType()) {
            case "post.published" -> postService.markPublished(event.getPostId());
            case "post.failed"    -> postService.markFailed(event.getPostId(), event.getError());
            default               -> log.warn("Late webhook: unknown event type={}", event.getType());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Validates the HMAC-SHA256 signature sent by Late.
     * Skip verification if no webhook secret is configured (dev mode).
     */
    private boolean verifySignature(String body, String signature) {
        String secret = lateApiConfig.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            log.debug("Webhook signature verification skipped (no secret configured)");
            return true;
        }
        if (signature == null) return false;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(computed);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature verification error", e);
            return false;
        }
    }

    private LateWebhookEvent parseEvent(String rawBody) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(rawBody, LateWebhookEvent.class);
    }
}
