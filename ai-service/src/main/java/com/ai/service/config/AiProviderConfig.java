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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiProviderConfig {

    private final AiProviderProperties properties;

    @Bean
    public OpenAiApi openAiApi() {
        AiProviderProperties.ProviderConfig active = properties.active();
        log.info("Initializing OpenAiApi with provider: {}", properties.activeProvider());
        
        // WHY: OpenAiApi is the low-level HTTP client. By setting a custom base-url, 
        // we redirect API calls to any OpenAI-compatible endpoint (OpenRouter, local Ollama, etc.)
        return new OpenAiApi(active.baseUrl(), active.apiKey());
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        AiProviderProperties.ProviderConfig active = properties.active();
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(active.model())
                .build();
                
        // WHY: OpenAiChatModel implements Spring AI's ChatModel interface. 
        // By configuring it with different base URLs and models, the same code works across any OpenAI-compatible provider.
        return new OpenAiChatModel(openAiApi, options);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        // WHY: ChatClient is Spring AI's high-level fluent API (similar to RestClient). 
        // It wraps the ChatModel and provides a clean .prompt().user().call().content() chain.
        return ChatClient.create(chatModel);
    }
}
