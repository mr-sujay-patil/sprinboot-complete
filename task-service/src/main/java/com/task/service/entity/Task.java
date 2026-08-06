package com.task.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// WHY: Marks this class as a JPA entity that maps to a database table
@Entity
// WHY: Explicitly name the table 'tasks' instead of defaulting to the class name 'task'
@Table(name = "tasks")
// WHY: Lombok @Data generates getters, setters, toString, equals, and hashCode automatically
@Data
// WHY: JPA requires a no-args constructor for entity instantiation
@NoArgsConstructor
// WHY: Builder pattern requires an all-args constructor to function properly
@AllArgsConstructor
// WHY: Provides a fluent API for object creation (e.g., Task.builder().title("...").build())
@Builder
public class Task {

    // WHY: Defines this field as the primary key
    @Id
    // WHY: Relies on the database's auto-increment feature to generate unique IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHY: Enforces a non-null constraint and a max length of 100 on the database column
    @Column(nullable = false, length = 100)
    private String title;

    // WHY: Optional description with a longer limit
    @Column(length = 500)
    private String description;

    // WHY: EnumType.STRING is preferred over ORDINAL because adding/reordering enum values won't silently break existing data.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    // WHY: use LocalDate not Date (java.util.Date is legacy, LocalDate is the java.time replacement)
    private LocalDate dueDate;

    // WHY: Audit field, mapped to DB, not updatable once set
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // WHY: Audit field to track the last time this record was modified
    private LocalDateTime updatedAt;

    // WHY: JPA callback executed just before the entity is saved to the DB for the first time
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // WHY: Default status if not provided
        if (this.status == null) {
            this.status = TaskStatus.TODO;
        }
    }

    // WHY: JPA callback executed just before an existing entity is updated in the DB
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
