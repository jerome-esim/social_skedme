package com.socialskedme.scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LateApiConfig {

    @Value("${app.late.api-key}")
    private String apiKey;

    @Value("${app.late.base-url}")
    private String baseUrl;

    @Value("${app.late.webhook-secret}")
    private String webhookSecret;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey()       { return apiKey; }
    public String getBaseUrl()      { return baseUrl; }
    public String getWebhookSecret(){ return webhookSecret; }
}
