package com.task.service.repository;

import com.task.service.entity.Task;
import com.task.service.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// WHY: JpaRepository provides standard CRUD and pagination operations out of the box. 
// Extending it removes the need to write boilerplate data access code.
// Comment: For 95% of use cases, Spring Data's derived queries and JpaRepository methods are sufficient.
public interface TaskRepository extends JpaRepository<Task, Long> {

    // WHY: Spring Data automatically implements this method based on its name (find By Status).
    // The Pageable parameter automatically integrates database-level pagination and sorting.
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
