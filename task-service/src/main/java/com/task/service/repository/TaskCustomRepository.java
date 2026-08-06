package com.task.service.repository;

import com.task.service.entity.Task;
import com.task.service.entity.TaskStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

// WHY: @Repository marks this as a Spring Data access component and enables persistence exception translation.
@Repository
public class TaskCustomRepository {

    // WHY: EntityManager is the core JPA interface used to interact with the persistence context and database.
    private final EntityManager entityManager;

    public TaskCustomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
     * WHY EntityManager: When you need dynamic queries, complex joins, or criteria that can't be expressed by derived query method names.
     * WHY NOT here in real code: For this simple case, the repository's findByStatus() does the same thing in one line. This is purely a demonstration.
     * WHEN to use EntityManager: Multi-table joins with conditional predicates, native SQL when JPQL is insufficient, batch operations.
     */
    public List<Task> findByStatusUsingEntityManager(TaskStatus status) {
        // WHY: createQuery uses JPQL (Java Persistence Query Language) which queries against entity objects rather than tables.
        return entityManager.createQuery("SELECT t FROM Task t WHERE t.status = :status", Task.class)
                // WHY: parameter binding prevents SQL injection and allows the DB to cache the query plan.
                .setParameter("status", status)
                .getResultList();
    }
}
