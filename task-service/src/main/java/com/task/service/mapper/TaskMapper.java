package com.task.service.mapper;

import com.task.service.dto.TaskRequest;
import com.task.service.dto.TaskResponse;
import com.task.service.entity.Task;
import org.springframework.stereotype.Component;

// Comment at top: Manual mappers are explicit and easy to debug. For large projects with many DTOs, consider MapStruct for compile-time code generation. For learning, manual mapping makes the data flow visible.
// WHY: @Component makes this a Spring bean so it can be injected into services.
@Component
public class TaskMapper {

    // WHY: Maps a JPA Entity to a Data Transfer Object to safely expose data to the client.
    public TaskResponse toResponse(Task entity) {
        if (entity == null) {
            return null;
        }
        return new TaskResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // WHY: Maps incoming request data to a fresh JPA Entity for saving to the database.
    public Task toEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        return Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .dueDate(request.dueDate())
                // WHY: id, createdAt, and updatedAt are managed by JPA/DB, so they aren't mapped from requests.
                .build();
    }

    // WHY: Modifies an existing entity retrieved from the DB using new data from the request.
    public void updateEntity(Task entity, TaskRequest request) {
        if (request == null || entity == null) {
            return;
        }
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(request.status());
        entity.setDueDate(request.dueDate());
        // WHY: We do NOT overwrite id, createdAt, or updatedAt because 'id' is immutable,
        // and 'createdAt'/'updatedAt' are exclusively managed by JPA lifecycle callbacks (@PrePersist/@PreUpdate).
    }
}
