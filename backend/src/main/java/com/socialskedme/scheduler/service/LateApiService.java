package com.socialskedme.scheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialskedme.scheduler.config.LateApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the getlate.dev API to schedule a post.
 * Late handles the actual publication at scheduledFor time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LateApiService {

    private final RestTemplate restTemplate;
    private final LateApiConfig lateApiConfig;
    private final ObjectMapper objectMapper;

    /**
     * Schedules a post via Late API.
     *
     * @param payloadJson  JSON from the outbox event containing:
     *                     postId, caption, videoUrl, platforms[], scheduledFor
     * @return Late post ID
     */
    public String schedulePost(String payloadJson) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(
                payloadJson, new TypeReference<>() {}
        );

        String caption      = (String) payload.get("caption");
        String videoUrl     = (String) payload.get("videoUrl");
        String scheduledFor = (String) payload.get("scheduledFor");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> platforms = (List<Map<String, String>>) payload.get("platforms");

        // Build the Late API request body
        Map<String, Object> lateRequest = new HashMap<>();
        lateRequest.put("content",      caption);
        lateRequest.put("mediaUrls",    List.of(videoUrl));
        lateRequest.put("scheduledFor", scheduledFor);
        lateRequest.put("platforms",    platforms);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(lateApiConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = lateApiConfig.getBaseUrl() + "/v1/posts";
        log.info("Calling Late API: POST {} scheduledFor={}", url, scheduledFor);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(lateRequest, headers),
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String latePostId = (String) response.getBody().get("id");
            log.info("Late API accepted post, latePostId={}", latePostId);
            return latePostId;
        }

        throw new RuntimeException("Late API returned non-2xx: " + response.getStatusCode());
    }
}
