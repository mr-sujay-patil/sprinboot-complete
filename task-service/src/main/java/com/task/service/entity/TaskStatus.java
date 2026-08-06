package com.task.service.entity;

// WHY: Enums provide compile-time safety and restrict values to a predefined set, 
// which is much safer and less error-prone than using raw String constants.
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
