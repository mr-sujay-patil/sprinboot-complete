package com.ai.service.controller;

import com.ai.service.config.AiProviderProperties;
import com.ai.service.dto.ChatRequest;
import com.ai.service.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final ChatClient chatClient;
    private final AiProviderProperties properties;

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
            // WHY: The ChatClient abstracts away the provider. Whether this is OpenRouter, 
            // OpenAI, or a local LLM, the code is identical. The provider is determined purely by configuration.
            String response = chatClient.prompt()
                    .user(request.prompt())
                    .call()
                    .content();

            return new ChatResponse(response, properties.active().model(), properties.activeProvider());
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
