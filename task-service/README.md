# Task Service — JPA & Resource Server Masterclass

`task-service` is a backend resource server microservice responsible for task CRUD operations. It demonstrates Spring Data JPA best practices, validation, layered architectural boundaries, and stateless token validation.

---

## 1. Core Concepts Demonstrated

### A. Resource Server Pattern (Stateless Token Validation)
`task-service` does NOT communicate with a database containing user credentials, nor does it make REST/network calls to `auth-server` to validate requests.
*   **How it works:** The microservice is configured with the same symmetric `jwt.secret` as `auth-server`. 
*   **Verification:** When a request arrives, `JwtAuthenticationFilter` intercepts it, extracts the token, verifies the cryptographic signature locally, parses the user's roles, and updates the `SecurityContext`. If the token is valid, access is granted.

### B. Architectural Boundaries (DTO Pattern)
*   **Entity vs DTO:** The database model `Task.java` is kept isolated. It is never exposed directly in REST controllers.
*   **Input/Output DTOs:** Requests map to `TaskRequest.java` (validated via `@Valid`), and responses map to `TaskResponse.java`.
*   **TaskMapper:** Handles conversion manually without dynamic reflection libraries (like ModelMapper), ensuring full type safety and compilation verification.

### C. Global Exception Boundaries
*   Exceptions (e.g. `ResourceNotFoundException`) thrown anywhere in the service layer are caught by `GlobalExceptionHandler.java` (configured via `@RestControllerAdvice`).
*   This maps exceptions to structured JSON responses, preventing stack traces from leaking to api clients.

---

## 2. Technical Code Studies

### Advanced JPA Techniques
*   **`@PrePersist` / `@PreUpdate`**: Automates auditing fields (`createdAt`, `updatedAt`) without needing complex Spring Data Auditing configurations.
*   **`@Enumerated(EnumType.STRING)`**: Persists enum values as varchar text (e.g., `IN_PROGRESS`) in H2 database instead of integer indexes (`1`). This ensures database schema migrations won't break if new enum constants are added.
*   **EntityManager Integration (`TaskCustomRepositoryImpl.java`)**: Demonstrates how to write custom native queries or advanced JPA criteria searches using direct Hibernate/JPA `EntityManager` calls when standard Spring Data query methods are insufficient.
