package com.task.service.controller;

import com.task.service.dto.TaskRequest;
import com.task.service.dto.TaskResponse;
import com.task.service.entity.TaskStatus;
import com.task.service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("REST request to create task: {}", request.title());
        // WHY: 201 CREATED is the correct status for resource creation, not 200 OK.
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     * WHY Pageable: returning unbounded lists is an anti-pattern. Pagination prevents: 
     * (1) memory exhaustion on large datasets, 
     * (2) slow query times, 
     * (3) network payload bloat. 
     * Spring Data's Pageable + Page handle this out of the box with zero boilerplate. 
     * Clients control page/size/sort via query params.
     */
    @GetMapping
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.info("REST request to get all tasks with pageable: {}", pageable);
        return taskService.getAllTasks(pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        log.info("REST request to get task: {}", id);
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        log.info("REST request to update task: {}", id);
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("REST request to delete task: {}", id);
        taskService.deleteTask(id);
        // WHY: 204 No Content is the standard response for successful deletes — no body needed.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public Page<TaskResponse> getTasksByStatus(@PathVariable TaskStatus status, Pageable pageable) {
        log.info("REST request to get tasks by status: {}", status);
        // WHY: Spring auto-converts the path variable string to the TaskStatus enum.
        return taskService.getTasksByStatus(status, pageable);
    }

    @GetMapping("/demo/entitymanager/{status}")
    public List<TaskResponse> getTasksByStatusEntityManager(@PathVariable TaskStatus status) {
        log.info("REST request to get tasks by status (demo entity manager): {}", status);
        // WHY: Demo-only endpoint. See TaskCustomRepository for EntityManager explanation.
        return taskService.getTasksByStatusEntityManager(status);
    }
}
