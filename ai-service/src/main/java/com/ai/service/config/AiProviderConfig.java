package com.ai.service.config;

/*
 * WHY THIS ARCHITECTURE:
 * Trade-off: OpenAI-compatible gateway (OpenRouter, OmniRouter) vs native SDKs
 * - Gateway approach: One integration handles many models/providers. Simpler code, single API contract.
 *   Downside: extra hop, gateway may not support all provider-specific features.
 * - Native SDK approach: Direct integration with each provider (Anthropic SDK, Google AI SDK, etc.).
 *   Full feature access but requires separate code/dependency per provider.
 * - For learning/prototyping, the gateway approach is ideal — this is what we use here.
 * 
 * This config class creates the Spring AI ChatClient bean dynamically based on the active provider.
 * To add a new provider: just add a YAML block under ai.providers. No code changes.
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiProviderConfig {

    private final AiProviderProperties properties;

    @Bean
    public RestClient openRouterRestClient() {
        AiProviderProperties.ProviderConfig active = properties.active();
        log.info("Initializing RestClient for OpenRouter at: {}", active.baseUrl());
        
        return RestClient.builder()
                .baseUrl(active.baseUrl())
                .defaultHeader("Authorization", "Bearer " + active.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("HTTP-Referer", "http://localhost:9091")
                .defaultHeader("X-Title", "SpringBoot AI Service")
                .build();
    }
}
