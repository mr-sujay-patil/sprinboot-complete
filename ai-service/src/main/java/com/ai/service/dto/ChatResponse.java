package com.ai.service.dto;

// WHY: Separate DTO from Spring AI's own ChatResponse to decouple our API contract
// from the framework's internal response structure. If Spring AI changes, our API doesn't.
public record ChatResponse(
    String response,
    String model,
    String provider
) {}
