package com.ai.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

// WHY: Using a Java record for @ConfigurationProperties leverages constructor binding (Spring Boot 3+).
// Records are immutable, which prevents accidental modification of configuration at runtime.
// Spring automatically binds YAML properties under 'ai.*' to these fields.
@ConfigurationProperties(prefix = "ai")
public record AiProviderProperties(
    // WHY: A single property controls which provider is active — provider switching is purely config-driven.
    String activeProvider,
    // WHY: Map structure allows unlimited providers. Key = provider name, Value = provider settings.
    // Adding a new provider means adding a YAML block — zero code changes.
    Map<String, ProviderConfig> providers
) {
    // WHY: Nested record keeps provider-specific settings grouped and type-safe.
    public record ProviderConfig(
        String baseUrl,
        String apiKey,
        String model
    ) {}

    // WHY: Convenience method to get the active provider's config without repeating map lookup everywhere.
    public ProviderConfig active() {
        ProviderConfig config = providers.get(activeProvider);
        if (config == null) {
            throw new IllegalStateException(
                "No AI provider configured for key: '" + activeProvider + "'. Check ai.active-provider in application.yml."
            );
        }
        return config;
    }
}
