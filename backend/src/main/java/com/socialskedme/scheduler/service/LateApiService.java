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
        // ── Mock mode: no API key configured (local dev) ──────────────────
        String apiKey = lateApiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return mockSchedule(payloadJson);
        }

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
        headers.setBearerAuth(apiKey);
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

    /**
     * Creates a Late profile and returns the OAuth connect URL for the given platform.
     * The frontend opens this URL so the user can authorize access.
     */
    public ConnectUrlResult createProfileAndGetConnectUrl(String platform, String accountName) throws Exception {
        String apiKey = lateApiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return mockConnectUrl(platform);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. Create a Late profile
        Map<String, Object> profileBody = new HashMap<>();
        profileBody.put("name", accountName != null && !accountName.isBlank() ? accountName : platform + " account");
        profileBody.put("description", "Connected via SkedMe");

        String profileUrl = lateApiConfig.getBaseUrl() + "/api/v1/profiles";
        ResponseEntity<Map> profileResponse = restTemplate.postForEntity(
                profileUrl,
                new HttpEntity<>(profileBody, headers),
                Map.class
        );

        if (!profileResponse.getStatusCode().is2xxSuccessful() || profileResponse.getBody() == null) {
            throw new RuntimeException("Failed to create Late profile: " + profileResponse.getStatusCode());
        }
        String profileId = (String) profileResponse.getBody().get("id");
        log.info("Created Late profile: {}", profileId);

        // 2. Get the OAuth connect URL for the platform
        String connectUrl = lateApiConfig.getBaseUrl() + "/api/v1/connect/" + platform + "?profileId=" + profileId;
        ResponseEntity<Map> connectResponse = restTemplate.exchange(
                connectUrl,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!connectResponse.getStatusCode().is2xxSuccessful() || connectResponse.getBody() == null) {
            throw new RuntimeException("Failed to get connect URL: " + connectResponse.getStatusCode());
        }

        String oauthUrl = (String) connectResponse.getBody().get("url");
        log.info("Got connect URL for platform={} profileId={}", platform, profileId);
        return new ConnectUrlResult(oauthUrl, profileId);
    }

    public record ConnectUrlResult(String connectUrl, String profileId) {}

    private ConnectUrlResult mockConnectUrl(String platform) {
        String fakeProfileId = "mock_prof_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        log.warn("[MOCK] LATE_API_KEY not set — returning mock connect URL for platform={}", platform);
        return new ConnectUrlResult("https://example.com/mock-oauth?platform=" + platform, fakeProfileId);
    }

    /**
     * Mock mode — used when LATE_API_KEY is not set.
     * Simulates a successful Late API call so the full flow can be tested locally.
     * Posts reach status 'scheduled'; they won't actually be published.
     */
    private String mockSchedule(String payloadJson) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {});
        String fakeId = "mock_late_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        log.warn("[MOCK] LATE_API_KEY not set — simulating Late API. postId={} fakeId={}",
                payload.get("postId"), fakeId);
        return fakeId;
    }
}
