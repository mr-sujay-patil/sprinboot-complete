package com.task.service.exception;

// WHY: Custom exception for domain-specific error handling. Extending RuntimeException
// makes it unchecked, so it doesn't pollute method signatures with throws clauses.
// The GlobalExceptionHandler will catch this and return a clean 404 response.
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
    }
}
