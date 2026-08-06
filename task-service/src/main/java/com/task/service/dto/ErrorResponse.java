package com.task.service.dto;

import java.time.LocalDateTime;

// WHY: Standardized error responses ensure that frontend applications or API consumers can reliably parse error details.
// Comment: Consistent error structure across all endpoints makes client-side error handling predictable.
public record ErrorResponse(
        // WHY: HTTP status code (e.g., 400, 404, 500)
        int status,
        // WHY: Short description of the error type (e.g., "Bad Request")
        String error,
        // WHY: Detailed message explaining what went wrong
        String message,
        // WHY: Timestamp of when the error occurred to help with debugging and log correlation
        LocalDateTime timestamp
) {
}
