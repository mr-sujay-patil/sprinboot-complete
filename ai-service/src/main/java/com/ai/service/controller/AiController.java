package com.ai.service.controller;

import com.ai.service.config.AiProviderProperties;
import com.ai.service.dto.ChatRequest;
import com.ai.service.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final RestClient restClient;
    private final AiProviderProperties properties;

    public record OpenRouterMessage(String role, String content) {}
    public record OpenRouterRequest(String model, List<OpenRouterMessage> messages) {}
    public record OpenRouterChoice(OpenRouterMessage message) {}
    public record OpenRouterResponse(List<OpenRouterChoice> choices) {}

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        
        String truncatedPrompt = request.prompt().length() > 50 
                ? request.prompt().substring(0, 50) + "..." 
                : request.prompt();
                
        log.info("User {} requesting chat with prompt: {} using provider: {}", 
                username, truncatedPrompt, properties.activeProvider());

        try {
            AiProviderProperties.ProviderConfig active = properties.active();
            OpenRouterRequest openRouterRequest = new OpenRouterRequest(
                    active.model(),
                    List.of(new OpenRouterMessage("user", request.prompt()))
            );

            OpenRouterResponse apiResponse = restClient.post()
                    .uri("/chat/completions")
                    .body(openRouterRequest)
                    .retrieve()
                    .body(OpenRouterResponse.class);

            String reply = "";
            if (apiResponse != null && apiResponse.choices() != null && !apiResponse.choices().isEmpty()) {
                reply = apiResponse.choices().get(0).message().content();
            }

            return new ChatResponse(reply, active.model(), properties.activeProvider());
        } catch (Exception e) {
            log.error("AI chat failed", e);
            throw new RuntimeException("AI provider error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "provider", properties.activeProvider(),
                "model", properties.active().model()
        );
    }
}
