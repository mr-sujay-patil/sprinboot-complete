package com.task.service.dto;

import com.task.service.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

// WHY: Records are concise, immutable structures ideal for carrying data out of the API.
// Comment: Never expose entities directly in API responses — DTOs prevent leaking internal fields (JPA proxies, lazy-loaded collections, future audit fields).
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
