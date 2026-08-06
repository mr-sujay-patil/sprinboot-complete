package com.task.service.service;

import com.task.service.dto.TaskRequest;
import com.task.service.dto.TaskResponse;
import com.task.service.entity.Task;
import com.task.service.entity.TaskStatus;
import com.task.service.exception.TaskNotFoundException;
import com.task.service.mapper.TaskMapper;
import com.task.service.repository.TaskCustomRepository;
import com.task.service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCustomRepository taskCustomRepository;
    private final TaskMapper taskMapper;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.info("Fetching all tasks with pagination: {}", pageable);
        // WHY: page.map() preserves pagination metadata (total elements, total pages, current page) while transforming content. This is why we return Page<TaskResponse> not List.
        return taskRepository.findAll(pageable)
                .map(taskMapper::toResponse);
    }

    public TaskResponse getTaskById(Long id) {
        log.info("Fetching task by id: {}", id);
        return taskRepository.findById(id)
                .map(taskMapper::toResponse)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task: {}", request.title());
        // WHY: The mapper creates a fresh entity without an ID — JPA assigns it on save.
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with id: {}", id);
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        taskMapper.updateEntity(existingTask, request);
        // WHY: save() on an entity with an existing ID performs UPDATE, not INSERT
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        // WHY: Check before delete to give a meaningful 404 instead of silent no-op
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    public Page<TaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable) {
        log.info("Fetching tasks by status: {} with pagination: {}", status, pageable);
        return taskRepository.findByStatus(status, pageable)
                .map(taskMapper::toResponse);
    }

    public List<TaskResponse> getTasksByStatusEntityManager(TaskStatus status) {
        log.info("Fetching tasks by status using EntityManager: {}", status);
        // WHY: This endpoint exists purely to demonstrate EntityManager usage. In practice, use the repository method above.
        return taskCustomRepository.findByStatusUsingEntityManager(status)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }
}
