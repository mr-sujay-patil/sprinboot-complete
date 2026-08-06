package com.ai.service.dto;

import jakarta.validation.constraints.NotBlank;

// WHY: Record for request DTO — immutable, concise, auto-generates constructor/getters.
// Validation ensures the AI endpoint doesn't waste API calls on empty prompts.
public record ChatRequest(
    @NotBlank(message = "Prompt cannot be blank")
    String prompt
) {}
