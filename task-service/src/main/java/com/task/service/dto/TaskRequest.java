package com.task.service.dto;

import com.task.service.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// WHY: Records provide immutable data carriers with auto-generated constructors, getters, equals, and hashCode.
// Comment: DTOs decouple API contract from entity structure. If the entity changes (e.g., new audit fields), the API stays stable.
public record TaskRequest(
        // WHY: Ensures the title is not null and contains at least one non-whitespace character.
        @NotBlank 
        // WHY: Prevents the client from sending strings that would exceed the database column size.
        @Size(max = 100) 
        String title,

        @Size(max = 500) 
        String description,

        @NotNull 
        TaskStatus status,

        // WHY: nullable, no validation needed. Can be omitted if there is no deadline.
        LocalDate dueDate
) {
}
